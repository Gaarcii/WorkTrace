import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  ViewChild,
  ElementRef,
} from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-worker-history-header',
  imports: [MatIconModule, MatButtonModule],
  templateUrl: './worker-history-header.component.html',
  styleUrls: ['./worker-history-header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerHistoryHeaderComponent {
  readonly rangoSemana = input.required<string>();

  readonly prevWeek = output<void>();
  readonly nextWeek = output<void>();
  readonly dateChanged = output<Event>();

  @ViewChild('datePicker') private readonly datePicker!: ElementRef<HTMLInputElement>;

  onPrev(): void {
    this.prevWeek.emit();
  }

  onNext(): void {
    this.nextWeek.emit();
  }

  abrirSelector(): void {
    if (this.datePicker) {
      if (typeof this.datePicker.nativeElement.showPicker === 'function') {
        this.datePicker.nativeElement.showPicker();
      } else {
        this.datePicker.nativeElement.click();
      }
    }
  }

  onDateChange(event: Event): void {
    this.dateChanged.emit(event);
  }
}
