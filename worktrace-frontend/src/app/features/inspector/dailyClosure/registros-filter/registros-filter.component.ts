import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-registros-filter',
  imports: [ReactiveFormsModule, MatCardModule, MatIconModule],
  templateUrl: './registros-filter.component.html',
  styleUrls: ['./registros-filter.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegistrosFilterComponent {
  readonly filterForm = input.required<FormGroup>();
  readonly clear = output<void>();
}
