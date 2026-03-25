import { Component, ChangeDetectionStrategy, input, output, signal } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-worker-perfil-password-modal',
  imports: [
    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './worker-perfil-password-modal.component.html',
  styleUrls: ['./worker-perfil-password-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerPerfilPasswordModalComponent {
  readonly passwordForm = input.required<FormGroup>();
  readonly passwordError = input.required<string>();
  readonly loading = input.required<boolean>();

  readonly close = output<void>();
  readonly save = output<void>();

  readonly showCurrentPassword = signal<boolean>(false);
  readonly showNewPassword = signal<boolean>(false);
  readonly showConfirmPassword = signal<boolean>(false);

  toggleActual() {
    this.showCurrentPassword.set(!this.showCurrentPassword());
  }
  toggleNueva() {
    this.showNewPassword.set(!this.showNewPassword());
  }
  toggleRepetir() {
    this.showConfirmPassword.set(!this.showConfirmPassword());
  }

  onClose(): void {
    this.close.emit();
  }
  onSave(): void {
    this.save.emit();
  }
}
