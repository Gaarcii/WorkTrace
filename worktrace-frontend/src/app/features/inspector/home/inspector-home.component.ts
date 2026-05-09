import { Component, ChangeDetectionStrategy, OnInit, inject, signal } from '@angular/core';
import { take, finalize } from 'rxjs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { InspectorHomeService } from '../../../shared/services/inspector/inspector-home.service';
import { InspectorHomeResponseDto } from '../../../shared/models/inspector.model';

import { InspectorHeaderComponent } from './Inspector-header/inspector-header.component';
import { InspectorAlertComponent } from './Inspector-alert/inspector-alert.component';
import { InspectorStatsGridComponent } from './Inspector-stats-grid/inspector-stats-grid.component';
import {
  InspectorExportCardComponent,
  ExportEvent,
} from './inspector-export-card/inspector-export-card.component';
import { InspectorInfoGridComponent } from './Inspector-info-grid/inspector-info-grid.component';

@Component({
  selector: 'app-inspector-home',
  standalone: true,
  imports: [
    MatProgressSpinnerModule,
    InspectorHeaderComponent,
    InspectorAlertComponent,
    InspectorStatsGridComponent,
    InspectorInfoGridComponent,
    InspectorExportCardComponent,
  ],
  templateUrl: './inspector-home.component.html',
  styleUrls: ['./inspector-home.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InspectorHomeComponent implements OnInit {
  private readonly inspectorHomeService = inject(InspectorHomeService);

  private readonly _loading = signal<boolean>(true);
  private readonly _error = signal<string | null>(null);
  private readonly _stats = signal<InspectorHomeResponseDto | null>(null);

  // Estado para el widget de exportación
  private readonly _isExporting = signal<boolean>(false);

  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly stats = this._stats.asReadonly();
  readonly isExporting = this._isExporting.asReadonly();

  ngOnInit(): void {
    this.loadDashboardStats();
  }

  clearError(): void {
    this._error.set(null);
  }

  retry(): void {
    this.loadDashboardStats();
  }

  private loadDashboardStats(): void {
    this._loading.set(true);
    this._error.set(null);

    this.inspectorHomeService
      .getHome()
      .pipe(
        take(1),
        finalize(() => this._loading.set(false)),
      )
      .subscribe({
        next: (data: InspectorHomeResponseDto) => {
          this._stats.set(data);
        },
        error: (err: unknown) => {
          const errorMessage =
            err instanceof Error ? err.message : 'Error inesperado al cargar las estadísticas';
          this._error.set(errorMessage);
        },
      });
  }

  // --- LÓGICA DE EXPORTACIÓN (SIN MODAL) ---

  handleExport(event: ExportEvent): void {
    this._isExporting.set(true);
    this._error.set(null);
    const { startDate, endDate } = this.calculateDatesForPeriod(event.period);

    const exportObs$ =
      event.format === 'PDF'
        ? this.inspectorHomeService.exportReportPdf(startDate, endDate)
        : this.inspectorHomeService.exportReportExcel(startDate, endDate);

    exportObs$
      .pipe(
        take(1),
        finalize(() => this._isExporting.set(false)),
      )
      .subscribe({
        next: (blob: Blob) => {
          this.downloadBlob(blob, event.format, startDate, endDate);
        },
        error: (err: unknown) => {
          console.error('Error al exportar:', err);
          this._error.set('Error al generar el documento de exportación. Inténtelo de nuevo.');
        },
      });
  }

  private downloadBlob(blob: Blob, format: string, start: Date, end: Date): void {
    const extension = format === 'PDF' ? 'pdf' : 'xlsx';
    const dateStr = `${start.toISOString().split('T')[0]}_al_${end.toISOString().split('T')[0]}`;
    const filename = `informe_inspeccion_${dateStr}.${extension}`;

    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    a.remove();
  }

  private calculateDatesForPeriod(period: string): { startDate: Date; endDate: Date } {
    const today = new Date();
    const endDate = new Date(today); // Hasta el momento actual
    let startDate = new Date();

    switch (period) {
      case 'TODAY':
        startDate.setHours(0, 0, 0, 0); // Desde las 00:00 de hoy
        break;
      case 'MONTH':
        startDate = new Date(today.getFullYear(), today.getMonth(), 1); // Día 1 del mes actual
        break;
      case 'YEAR':
        startDate = new Date(today.getFullYear(), 0, 1); // 1 de enero del año actual
        break;
      case 'ALL':
        // Histórico de 4 años atrás según normativa
        startDate = new Date(today.getFullYear() - 4, today.getMonth(), today.getDate());
        break;
    }
    return { startDate, endDate };
  }
}
