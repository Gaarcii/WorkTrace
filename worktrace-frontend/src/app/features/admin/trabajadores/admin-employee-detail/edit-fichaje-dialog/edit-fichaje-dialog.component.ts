import { Component, ChangeDetectionStrategy, inject, input, output, effect } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  EditTimeEntryRequestDto,
  TimeEntryTableResponseDto,
} from '../../../../../shared/models/time-entry.model';

type EditFichajeFormGroup = {
  id: FormControl<string>;
  date: FormControl<string>;
  startTime: FormControl<string>;
  endTime: FormControl<string | null>;
  modificationReason: FormControl<string>;
};

type EditFichajeFormValue = {
  id: string;
  date: string;
  startTime: string;
  endTime: string | null;
  modificationReason: string;
};

@Component({
  selector: 'app-edit-fichaje-dialog',
  templateUrl: './edit-fichaje-dialog.component.html',
  styleUrl: './edit-fichaje-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
})
export class EditFichajeDialogComponent {
  private readonly fb = inject(FormBuilder);

  public readonly isOpen = input.required<boolean>();
  public readonly fichaje = input<TimeEntryTableResponseDto | null>(null);
  public readonly loading = input.required<boolean>();
  public readonly error = input<string | null>(null);

  public readonly closeDialog = output<void>();
  public readonly save = output<EditTimeEntryRequestDto>();

  public readonly form = this.fb.group<EditFichajeFormGroup>({
    id: this.fb.control('', { nonNullable: true }),
    date: this.fb.control('', { nonNullable: true, validators: [Validators.required] }),
    startTime: this.fb.control('', { nonNullable: true, validators: [Validators.required] }),
    endTime: this.fb.control<string | null>(null),
    modificationReason: this.fb.control('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(10)],
    }),
  });

  constructor() {
    effect(() => {
      const data = this.fichaje();
      const open = this.isOpen();

      if (open && data) {
        this.form.patchValue(this.toFormValue(data));
      } else if (!open) {
        this.form.reset();
      }
    });
  }

  public onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const rawValue = this.form.getRawValue();

    this.save.emit({
      entrada: this.toOffsetDateTime(rawValue.date, rawValue.startTime),
      salida: rawValue.endTime ? this.toOffsetDateTime(rawValue.date, rawValue.endTime) : null,
      justificacion: rawValue.modificationReason.trim(),
    });
  }

  public onClose(): void {
    if (!this.loading()) {
      this.closeDialog.emit();
    }
  }

  private toFormValue(fichaje: TimeEntryTableResponseDto): EditFichajeFormValue {
    const startValue = fichaje.start_at ?? fichaje.entrada ?? '';
    const endValue = fichaje.end_at ?? fichaje.salida ?? null;
    const dateValue = fichaje.work_date ?? fichaje.fecha ?? startValue;

    return {
      id: fichaje.id,
      date: this.toDateInputValue(dateValue),
      startTime: this.toTimeInputValue(startValue),
      endTime: endValue ? this.toTimeInputValue(endValue) : null,
      modificationReason: '',
    };
  }

  private toDateInputValue(value: string): string {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return value.slice(0, 10);
    }

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  private toTimeInputValue(value: string): string {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      const timeMatch = value.match(/\d{2}:\d{2}/);
      return timeMatch?.[0] ?? '';
    }

    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${hours}:${minutes}`;
  }

  private toOffsetDateTime(dateValue: string, timeValue: string): string {
    const dateTime = new Date(`${dateValue}T${timeValue}:00`);

    if (Number.isNaN(dateTime.getTime())) {
      return `${dateValue}T${timeValue}:00+00:00`;
    }

    const timezoneOffsetMinutes = -dateTime.getTimezoneOffset();
    const sign = timezoneOffsetMinutes >= 0 ? '+' : '-';
    const absoluteMinutes = Math.abs(timezoneOffsetMinutes);
    const hours = String(Math.floor(absoluteMinutes / 60)).padStart(2, '0');
    const minutes = String(absoluteMinutes % 60).padStart(2, '0');

    return `${dateValue}T${timeValue}:00${sign}${hours}:${minutes}`;
  }
}
