import {
  Component,
  ChangeDetectionStrategy,
  input,
  viewChild,
  ElementRef,
  effect,
  afterNextRender,
  Injector,
  inject,
  runInInjectionContext,
} from '@angular/core';
import * as L from 'leaflet';
import { FichajeTablaResponseDto } from '../../../../../shared/models/time-entry.model';
interface MapPointInfo {
  lat: number;
  lng: number;
  type: 'start' | 'end';
  color: string;
  shortLabel: string;
  fullPopupText: string;
  dayKey: string;
}

@Component({
  selector: 'app-employee-locations-map-card',
  templateUrl: './employee-locations-map-card.component.html',
  styleUrl: './employee-locations-map-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmployeeLocationsMapCardComponent {
  public readonly fichajes = input<FichajeTablaResponseDto[]>([]);
  public readonly mapContainer = viewChild.required<ElementRef<HTMLDivElement>>('mapContainer');

  private map?: L.Map;
  private readonly markersLayer = L.layerGroup();
  private readonly routesLayer = L.layerGroup();

  private readonly injector = inject(Injector);

  constructor() {
    afterNextRender(() => {
      runInInjectionContext(this.injector, () => {
        effect(() => {
          const data = this.fichajes();
          const container = this.mapContainer().nativeElement;

          this.ensureMap(container);
          this.renderLocations(data);
        });
      });
    });
  }

  private ensureMap(container: HTMLDivElement): void {
    if (this.map) {
      return;
    }

    this.map = L.map(container, {
      center: [40.4168, -3.7038],
      zoom: 6,
      zoomControl: true,
      scrollWheelZoom: false,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
      maxZoom: 19,
    }).addTo(this.map);

    this.markersLayer.addTo(this.map);
    this.routesLayer.addTo(this.map);
  }

  private renderLocations(data: FichajeTablaResponseDto[]): void {
    if (!this.map) {
      return;
    }

    this.markersLayer.clearLayers();
    this.routesLayer.clearLayers();

    const allCoordinates: L.LatLngTuple[] = [];
    const dayRoutes = new Map<string, L.LatLngTuple[]>();

    const pointsByCoordinate = new Map<string, MapPointInfo[]>();

    for (const fichaje of data) {
      const dayKey = this.getDayKey(fichaje);

      if (!dayRoutes.has(dayKey)) {
        dayRoutes.set(dayKey, []);
      }
      const routeCoordinates = dayRoutes.get(dayKey)!;

      const start = this.extractPointInfo(fichaje, 'start');
      const end = this.extractPointInfo(fichaje, 'end');

      if (start) {
        allCoordinates.push([start.lat, start.lng]);
        routeCoordinates.push([start.lat, start.lng]);
        this.addPointToGroup(start, pointsByCoordinate);
      }

      if (end) {
        allCoordinates.push([end.lat, end.lng]);
        routeCoordinates.push([end.lat, end.lng]);
        this.addPointToGroup(end, pointsByCoordinate);
      }
    }

    for (const routeCoordinates of dayRoutes.values()) {
      if (routeCoordinates.length > 1) {
        L.polyline(routeCoordinates, {
          color: '#000026',
          weight: 3,
          opacity: 0.6,
          dashArray: '5, 10',
        }).addTo(this.routesLayer);
      }
    }

    this.renderSpiderifiedMarkers(pointsByCoordinate);

    if (allCoordinates.length > 0) {
      if (allCoordinates.length === 1) {
        this.map.setView(allCoordinates[0], 14);
      } else {
        this.map.fitBounds(L.latLngBounds(allCoordinates), { padding: [40, 40] });
      }
      requestAnimationFrame(() => this.map?.invalidateSize());
    }
  }

  private addPointToGroup(point: MapPointInfo, mapGroup: Map<string, MapPointInfo[]>): void {
    const key = `${point.lat.toFixed(6)},${point.lng.toFixed(6)}`;
    if (!mapGroup.has(key)) {
      mapGroup.set(key, []);
    }
    mapGroup.get(key)!.push(point);
  }

  private renderSpiderifiedMarkers(pointsByCoordinate: Map<string, MapPointInfo[]>): void {
    const offsetRadiusBase = 0.0003;

    for (const [key, points] of pointsByCoordinate.entries()) {
      const [centerLat, centerLng] = key.split(',').map(Number);

      if (points.length === 1) {
        const p = points[0];
        L.marker([p.lat, p.lng], { icon: this.createPillIcon(p.color, p.shortLabel) })
          .bindPopup(p.fullPopupText)
          .addTo(this.markersLayer);
        continue;
      }

      L.circleMarker([centerLat, centerLng], {
        radius: 4,
        color: '#000026',
        fillColor: 'white',
        fillOpacity: 1,
        weight: 2,
      }).addTo(this.markersLayer);

      const lngCorrection = Math.cos(centerLat * (Math.PI / 180));

      points.forEach((p, index) => {
        const currentRadius = offsetRadiusBase + Math.floor(index / 8) * 0.00015;
        const angle = (index * 2 * Math.PI) / points.length;

        const offsetLat = centerLat + currentRadius * Math.cos(angle);
        const offsetLng = centerLng + (currentRadius / lngCorrection) * Math.sin(angle);

        L.polyline(
          [
            [centerLat, centerLng],
            [offsetLat, offsetLng],
          ],
          {
            color: p.color,
            weight: 2,
            opacity: 0.8,
          },
        ).addTo(this.routesLayer);

        L.marker([offsetLat, offsetLng], { icon: this.createPillIcon(p.color, p.shortLabel) })
          .bindPopup(p.fullPopupText)
          .addTo(this.markersLayer);
      });
    }
  }

  private extractPointInfo(
    fichaje: FichajeTablaResponseDto,
    type: 'start' | 'end',
  ): MapPointInfo | null {
    const latitude =
      type === 'start'
        ? (fichaje.latEntrada ?? fichaje.start_lat)
        : (fichaje.latSalida ?? fichaje.end_lat);
    const longitude =
      type === 'start'
        ? (fichaje.lngEntrada ?? fichaje.start_lng)
        : (fichaje.lngSalida ?? fichaje.end_lng);

    if (latitude == null || longitude == null) {
      return null;
    }

    const color = type === 'start' ? '#2e7d32' : '#c62828';
    const typeLabel = type === 'start' ? 'E' : 'S';
    const typeFullName = type === 'start' ? 'Entrada' : 'Salida';

    const timeValue =
      type === 'start' ? (fichaje.start_at ?? fichaje.entrada) : (fichaje.end_at ?? fichaje.salida);
    const fallbackDate = fichaje.work_date ?? fichaje.fecha ?? '';

    const shortDate = this.formatDateDayMonth(fallbackDate);
    const shortLabel = `${shortDate} ${typeLabel}`;

    const popupText = `${typeFullName}: ${timeValue ? timeValue : fallbackDate}`;

    return {
      lat: latitude as number,
      lng: longitude as number,
      type,
      color,
      shortLabel,
      fullPopupText: popupText,
      dayKey: this.getDayKey(fichaje),
    };
  }

  private formatDateDayMonth(dateInput: string | unknown): string {
    if (!dateInput || typeof dateInput !== 'string') return '';
    const date = new Date(dateInput);
    if (isNaN(date.getTime())) return '';
    return `${date.getDate()}-${date.getMonth() + 1}`;
  }

  private getDayKey(fichaje: FichajeTablaResponseDto): string {
    const rawDay =
      fichaje.work_date ??
      fichaje.fecha ??
      fichaje.start_at ??
      fichaje.entrada ??
      fichaje.end_at ??
      fichaje.salida;
    if (!rawDay) return 'sin-fecha';

    const dayText = String(rawDay).trim();
    const isoPrefixMatch = dayText.match(/^\d{4}-\d{2}-\d{2}/);
    return isoPrefixMatch ? isoPrefixMatch[0] : dayText;
  }

  private createPillIcon(color: string, label: string): L.DivIcon {
    return L.divIcon({
      className: 'worktrace-leaflet-marker',
      iconSize: [0, 0],
      iconAnchor: [0, 0],
      popupAnchor: [0, -16],
      html: `
        <div style="
          position: absolute;
          transform: translate(-50%, -50%);
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 2px 8px;
          border-radius: 12px;
          background: ${color};
          color: #fff;
          font-size: 11px;
          font-weight: 700;
          box-shadow: 0 2px 8px rgba(0,0,0,0.4);
          border: 2px solid #fff;
          white-space: nowrap;
          width: max-content;
        ">${label}</div>
      `,
    });
  }
}
