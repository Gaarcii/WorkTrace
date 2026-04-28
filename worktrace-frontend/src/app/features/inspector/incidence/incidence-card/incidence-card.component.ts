import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { InspectorIncidenceDto } from '../../../../shared/models/inspector.model';

@Component({
  selector: 'app-incidence-card',
  imports: [CommonModule, MatCardModule, MatDividerModule],
  templateUrl: './incidence-card.component.html',
  styleUrls: ['./incidence-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IncidenceCardComponent {
  readonly incident = input.required<InspectorIncidenceDto>();

  getInitials(name?: string): string {
    if (!name) return '?';
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .substring(0, 2);
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  }

  formatTime(dateString?: string): string {
    if (!dateString) return '';
    return new Date(dateString).toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  }

  getStatusLabel(status: string): string {
    const s = status?.toUpperCase();
    const labels: Record<string, string> = {
      PENDING: 'Pendiente',
      RESOLVED: 'Resuelta',
      REJECTED: 'Rechazada',
    };
    return labels[s] || status || 'Desconocido';
  }

  getStatusClass(status: string): string {
    const s = status?.toUpperCase();
    const classes: Record<string, string> = {
      PENDING: 'status-warning',
      RESOLVED: 'status-success',
      REJECTED: 'status-error',
    };
    return classes[s] || 'status-default';
  }
}
