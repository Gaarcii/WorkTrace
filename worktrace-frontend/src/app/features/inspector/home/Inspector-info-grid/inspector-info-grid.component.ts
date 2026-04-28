import { Component, ChangeDetectionStrategy } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';

@Component({
  selector: 'app-inspector-info-grid',
  standalone: true,
  imports: [MatCardModule, MatIconModule, MatListModule],
  templateUrl: './inspector-info-grid.component.html',
  styleUrls: ['./inspector-info-grid.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InspectorInfoGridComponent {}