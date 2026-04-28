import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { IncidenceCardComponent } from '../incidence-card/incidence-card.component';
import { InspectorIncidenceDto } from '../../../../shared/models/inspector.model';

@Component({
  selector: 'app-incidences-list',
  imports: [MatProgressSpinnerModule, MatIconModule, IncidenceCardComponent],
  templateUrl: './incidences-list.component.html',
  styleUrls: ['./incidences-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IncidencesListComponent {
  readonly loading = input.required<boolean>();
  readonly incidents = input.required<InspectorIncidenceDto[]>();
}
