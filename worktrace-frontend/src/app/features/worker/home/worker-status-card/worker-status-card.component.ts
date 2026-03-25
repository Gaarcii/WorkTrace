import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-worker-status-card',
  imports: [CommonModule],
  templateUrl: './worker-status-card.component.html',
  styleUrls: ['./worker-status-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerStatusCardComponent {
  readonly badgeClass = input.required<string>();
  readonly dotClass = input.required<string>();
  readonly texto = input.required<string>();
  readonly hora = input.required<string>();
  readonly fecha = input.required<string>();
}
