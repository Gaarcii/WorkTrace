import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-error-alert',
  imports: [MatIconModule],
  templateUrl: './error-alert.component.html',
  styleUrls: ['./error-alert.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ErrorAlertComponent {
  readonly message = input.required<string>();

  readonly closed = output<void>();

  onClose(): void {
    this.closed.emit();
  }
}
