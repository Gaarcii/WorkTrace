import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRippleModule } from '@angular/material/core';

@Component({
  selector: 'app-worker-action-card',
  imports: [MatIconModule, MatProgressSpinnerModule, MatRippleModule],
  templateUrl: './worker-action-card.component.html',
  styleUrls: ['./worker-action-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerActionCardComponent {
  readonly isLoading = input.required<boolean>();
  readonly texto = input.required<string>();
  readonly subtexto = input.required<string>();

  readonly actionClicked = output<void>();

  onClick(): void {
    if (!this.isLoading()) {
      this.actionClicked.emit();
    }
  }
}
