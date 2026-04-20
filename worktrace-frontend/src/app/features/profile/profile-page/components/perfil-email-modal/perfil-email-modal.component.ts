import { Component, ChangeDetectionStrategy, input, output, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-perfil-email-modal',
  imports: [
    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './perfil-email-modal.component.html',
  styleUrls: ['./perfil-email-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PerfilEmailModalComponent {
  readonly passwordControl = input.required<FormControl>();
  readonly loading = input.required<boolean>();

  readonly close = output<void>();
  readonly confirm = output<void>();

  readonly hideConfirmPassword = signal<boolean>(true);

  toggleVisibility(): void {
    this.hideConfirmPassword.set(!this.hideConfirmPassword());
  }

  onClose(): void {
    this.close.emit();
  }
  onConfirm(): void {
    this.confirm.emit();
  }
}
