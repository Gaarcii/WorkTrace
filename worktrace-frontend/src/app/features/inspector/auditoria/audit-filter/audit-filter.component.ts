import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';

export interface ActionOption {
  title: string;
  value: string;
}

@Component({
  selector: 'app-audit-filter',
  imports: [ReactiveFormsModule, MatCardModule],
  templateUrl: './audit-filter.component.html',
  styleUrls: ['./audit-filter.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuditFilterComponent {
  readonly filterForm = input.required<FormGroup>();
  readonly actionOptions = input.required<ActionOption[]>();
  readonly clear = output<void>();
}
