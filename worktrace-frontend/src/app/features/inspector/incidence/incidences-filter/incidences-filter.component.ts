import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { IncidenceTypeProjection } from '../../../../shared/models/incidence-type.model';

export interface StatusOption {
  text: string;
  value: string;
}

@Component({
  selector: 'app-incidences-filter',
  imports: [ReactiveFormsModule, MatCardModule, MatIconModule],
  templateUrl: './incidences-filter.component.html',
  styleUrls: ['./incidences-filter.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IncidencesFilterComponent {
  readonly statusControl = input.required<FormControl<string | null>>();
  readonly typeControl = input.required<FormControl<string | null>>();
  readonly searchControl = input.required<FormControl<string | null>>();

  readonly statusOptions = input.required<StatusOption[]>();
  readonly tipos = input.required<IncidenceTypeProjection[]>();
}
