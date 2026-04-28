import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-inspector-alert',
  standalone: true,
  imports: [MatCardModule, MatIconModule, MatButtonModule],
  templateUrl: './inspector-alert.component.html',
  styleUrls: ['./inspector-alert.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InspectorAlertComponent {
  error = input.required<string>();
  
  onRetry = output<void>();
  onClear = output<void>();

  retry(): void {
    this.onRetry.emit();
  }

  clearError(): void {
    this.onClear.emit();
  }
}