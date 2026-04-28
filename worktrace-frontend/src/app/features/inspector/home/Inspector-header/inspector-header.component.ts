import { Component, ChangeDetectionStrategy } from '@angular/core';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-inspector-header',
  standalone: true,
  imports: [MatChipsModule, MatIconModule],
  templateUrl: './inspector-header.component.html',
  styleUrls: ['./inspector-header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InspectorHeaderComponent {}
