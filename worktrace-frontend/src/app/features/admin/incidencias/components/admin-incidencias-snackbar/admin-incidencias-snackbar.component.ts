import {
  ChangeDetectionStrategy,
  Component,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  input,
  output,
} from '@angular/core';

@Component({
  selector: 'app-admin-incidencias-snackbar',
  templateUrl: './admin-incidencias-snackbar.component.html',
  styleUrls: ['./admin-incidencias-snackbar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminIncidenciasSnackbarComponent implements OnChanges, OnDestroy {
  readonly modelValue = input.required<boolean>();
  readonly snackbarMessage = input.required<string>();

  readonly updateModelValue = output<boolean>();

  private hideTimer: ReturnType<typeof setTimeout> | null = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['modelValue']) {
      this.syncTimer();
    }
  }

  ngOnDestroy(): void {
    this.clearTimer();
  }

  close(): void {
    this.updateModelValue.emit(false);
    this.clearTimer();
  }

  private syncTimer(): void {
    this.clearTimer();
    if (!this.modelValue()) {
      return;
    }

    this.hideTimer = setTimeout(() => {
      this.updateModelValue.emit(false);
      this.hideTimer = null;
    }, 3000);
  }

  private clearTimer(): void {
    if (this.hideTimer) {
      clearTimeout(this.hideTimer);
      this.hideTimer = null;
    }
  }
}
