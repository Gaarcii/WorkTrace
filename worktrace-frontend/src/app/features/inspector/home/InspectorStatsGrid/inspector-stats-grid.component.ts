import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { InspectorHomeResponseDto } from '../../../../shared/models/inspector.model';

@Component({
  selector: 'app-inspector-stats-grid',
  standalone: true,
  imports: [MatCardModule, MatIconModule],
  templateUrl: './inspector-stats-grid.component.html',
  styleUrls: ['./inspector-stats-grid.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InspectorStatsGridComponent {
  stats = input.required<InspectorHomeResponseDto>();
}