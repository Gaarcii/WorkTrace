import { Component, ChangeDetectionStrategy, input, output, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {
  InspectorAuditDto,
  InspectorAuditDetailDto,
} from '../../../../shared/models/inspector.model';

export interface AuditSnapshot {
  id?: string;
  workDate?: string;
  estadoFichaje?: string;
  createdAt?: string;
  employee?: {
    fullName?: string;
    employeeCode?: string;
    phone?: string;
    position?: { title?: string };
    weeklyHours?: number;
  };
  company?: {
    companyName?: string;
    cif?: string;
  };
  startAt?: string;
  startIp?: string;
  startLat?: number;
  startLng?: number;
  startAccuracyM?: number;
  startGeoip?: { isp?: string; city?: string; connection?: string };
  startUserAgent?: string;
  endAt?: string;
  endIp?: string;
  endLat?: number;
  endLng?: number;
  endAccuracyM?: number;
  endGeoip?: { isp?: string; city?: string; connection?: string };
  endUserAgent?: string;
  createdBy?: { email?: string; username?: string; role?: string };
  modificationReason?: string;
  deleteReason?: string;
  flags?: string[];
  [key: string]: unknown;
}

export interface AuditDataGroup {
  title: string;
  icon: string;
  items: { label: string; value: string }[];
}

@Component({
  selector: 'app-audit-detail',
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    MatDividerModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './audit-detail.component.html',
  styleUrls: ['./audit-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuditDetailComponent {
  readonly loadingDetails = input.required<boolean>();
  readonly recordBase = input<InspectorAuditDto | null>(null);
  readonly recordDetail = input<InspectorAuditDetailDto | null>(null);
  readonly parsedOldData = input<Record<string, unknown> | null>(null);

  readonly close = output<void>();

  readonly groupedOldData = computed<AuditDataGroup[]>(() => {
    const rawData = this.parsedOldData();
    if (!rawData) return [];

    const data = rawData as AuditSnapshot;
    const groups: AuditDataGroup[] = [];

    const addGroup = (title: string, icon: string, fields: { label: string; value: unknown }[]) => {
      const validItems = fields
        .filter((f) => {
          if (f.value === null || f.value === undefined || f.value === '') return false;
          if (Array.isArray(f.value) && f.value.length === 0) return false;
          return true;
        })
        .map((f) => ({ label: f.label, value: String(f.value) }));

      if (validItems.length > 0) {
        groups.push({ title, icon, items: validItems });
      }
    };

    addGroup('Información del Fichaje', 'assignment', [
      { label: 'ID Registro', value: data.id },
      { label: 'Fecha de Trabajo', value: data.workDate },
      { label: 'Estado', value: data.estadoFichaje },
      { label: 'Fecha Modificación', value: this.formatFullDateTime(data.createdAt) },
    ]);

    if (data.employee || data.company) {
      addGroup('Empleado y Empresa', 'badge', [
        { label: 'Nombre Completo', value: data.employee?.fullName },
        { label: 'DNI', value: data.employee?.employeeCode },
        { label: 'Puesto', value: data.employee?.position?.title },
        { label: 'Horas Semanales', value: data.employee?.weeklyHours },
        { label: 'Razón Social', value: data.company?.companyName },
        { label: 'CIF Empresa', value: data.company?.cif },
      ]);
    }

    addGroup('Registro de Entrada', 'login', [
      { label: 'Hora Registrada', value: this.formatFullDateTime(data.startAt) },
      { label: 'Dirección IP', value: data.startIp },
      {
        label: 'Coordenadas',
        value: data.startLat && data.startLng ? `${data.startLat}, ${data.startLng}` : null,
      },
      { label: 'Precisión (metros)', value: data.startAccuracyM },
      { label: 'ISP / Operador', value: data.startGeoip?.isp },
      { label: 'Ciudad', value: data.startGeoip?.city },
      { label: 'Conexión', value: data.startGeoip?.connection },
      { label: 'Dispositivo', value: data.startUserAgent },
    ]);

    addGroup('Registro de Salida', 'logout', [
      { label: 'Hora Registrada', value: this.formatFullDateTime(data.endAt) },
      { label: 'Dirección IP', value: data.endIp },
      {
        label: 'Coordenadas',
        value: data.endLat && data.endLng ? `${data.endLat}, ${data.endLng}` : null,
      },
      { label: 'Precisión (metros)', value: data.endAccuracyM },
      { label: 'ISP / Operador', value: data.endGeoip?.isp },
      { label: 'Ciudad', value: data.endGeoip?.city },
      { label: 'Conexión', value: data.endGeoip?.connection },
      { label: 'Dispositivo', value: data.endUserAgent },
    ]);

    return groups;
  });

  getActionColor(action: string): string {
    const colors: Record<string, string> = {
      ADMIN_ADJUST: 'status-warning',
      SOFT_DELETE: 'status-error',
      LOGIN: 'status-info',
      EXPORT: 'status-purple',
      INSERT: 'status-success',
    };
    return colors[action?.toUpperCase()] || 'status-default';
  }

  formatFullDateTime(dateString?: string): string | null {
    if (!dateString) return null;
    try {
      const date = new Date(dateString);
      return new Intl.DateTimeFormat('es-ES', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      }).format(date);
    } catch {
      return dateString;
    }
  }
}
