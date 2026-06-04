import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { InspectorDailyClosureDto } from '../../../../shared/models/inspector.model';

@Component({
  selector: 'app-registros-table',
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatDividerModule,
  ],
  templateUrl: './registros-table.component.html',
  styleUrls: ['./registros-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegistrosTableComponent {
  readonly loading = input.required<boolean>();
  readonly registros = input.required<InspectorDailyClosureDto[]>();

  readonly copyHash = output<string>();

  formatDate(dateString?: string): string {
    if (!dateString) return '—';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  }

  formatDateTime(dateTimeString?: string): string {
    if (!dateTimeString) return '—';
    const date = new Date(dateTimeString);
    return date.toLocaleString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  getRowClass(estado: string): string {
    const s = estado?.toUpperCase();
    if (s === 'CORRUPTED' || s === 'INVALID') return 'manipulated-row';
    if (s === 'MODIFIED') return 'modified-row';
    if (s === 'VALID') return 'valid-row';
    return '';
  }

  getStatusLabel(estado: string): string {
    const s = estado?.toUpperCase();
    if (s === 'VALID') return 'ÍNTEGRO';
    if (s === 'CORRUPTED' || s === 'INVALID') return 'CORRUPTO';
    if (s === 'MODIFIED') return 'MODIFICADO';
    return 'SIN VERIFICAR';
  }

  getStatusColor(estado: string): string {
    const s = estado?.toUpperCase();
    if (s === 'VALID') return 'success';
    if (s === 'CORRUPTED' || s === 'INVALID') return 'error';
    if (s === 'MODIFIED') return 'warning';
    return 'default';
  }
}
