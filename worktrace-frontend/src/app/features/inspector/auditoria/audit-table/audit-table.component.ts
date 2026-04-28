import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { InspectorAuditDto } from '../../../../shared/models/inspector.model';

@Component({
  selector: 'app-audit-table',
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './audit-table.component.html',
  styleUrls: ['./audit-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuditTableComponent {
  readonly loading = input.required<boolean>();
  readonly records = input.required<InspectorAuditDto[]>();
  readonly viewDetails = output<InspectorAuditDto>();

  getActionLabel(action: string): string {
    const labels: Record<string, string> = {
      ADMIN_ADJUST: 'Modificado',
      SOFT_DELETE: 'Eliminado',
      LOGIN: 'Acceso',
      EXPORT: 'Exportado',
      INSERT: 'Creación',
    };
    return labels[action?.toUpperCase()] || action || 'Desconocido';
  }

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

  formatDateTime(dateString?: string): string {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date);
  }

  getUserInitials(fullName?: string): string {
    if (!fullName) return '??';
    return fullName
      .split(' ')
      .map((w) => w[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }
}
