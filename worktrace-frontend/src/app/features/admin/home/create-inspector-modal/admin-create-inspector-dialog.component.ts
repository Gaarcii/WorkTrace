import { Component, ChangeDetectionStrategy, input, output, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { InspectorRequestDto } from '../../../../shared/models/inspector.model';

@Component({
  selector: 'app-admin-create-inspector-dialog',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-create-inspector-dialog.component.html',
  styleUrls: ['./admin-create-inspector-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminCreateInspectorDialogComponent {
  readonly modelValue = input<boolean>(false);
  readonly cargando = input<boolean>(false);

  readonly updateModelValue = output<boolean>();
  readonly confirm = output<InspectorRequestDto>();

  readonly form = new FormGroup({
    fullName: new FormControl<string>('', { validators: [Validators.required], nonNullable: true }),
    email: new FormControl<string>('', {
      validators: [Validators.required, Validators.email],
      nonNullable: true,
    }),
    phone: new FormControl<string>('', { validators: [Validators.required], nonNullable: true })
  });

  constructor() {
    effect(() => {
      if (this.modelValue()) {
        this.form.reset();
      }
    });
  }

  closeDialog(): void {
    this.updateModelValue.emit(false);
  }

  onSubmit(): void {
    if (this.form.valid && !this.cargando()) {
      this.confirm.emit(this.form.getRawValue());
    }
  }
}
