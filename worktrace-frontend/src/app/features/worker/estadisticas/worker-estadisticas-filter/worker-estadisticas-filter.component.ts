import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { ReactiveFormsModule, FormGroup } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-worker-estadisticas-filter',
  imports: [ReactiveFormsModule, MatButtonModule],
  templateUrl: './worker-estadisticas-filter.component.html',
  styleUrls: ['./worker-estadisticas-filter.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerEstadisticasFilterComponent {
  readonly form = input.required<FormGroup>();
  readonly aplicar = output<void>();

  onSubmit(): void {
    if (this.form().valid) {
      this.aplicar.emit();
    }
  }
}
