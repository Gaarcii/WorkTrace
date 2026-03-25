import { Component, inject, ChangeDetectionStrategy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { take, finalize } from 'rxjs/operators';
import { AuthService } from '../../../core/auth/auth.service';
import { TokenStorageService } from '../../../core/auth/token-storage.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-password-change',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './password-change.component.html',
  styleUrl: './password-change.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PasswordChangeComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private tokenStorage = inject(TokenStorageService);

  // Convertimos el estado a Signals
  hideActual = signal(true);
  hideNueva = signal(true);
  hideRepetir = signal(true);
  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  passwordForm: FormGroup = this.fb.group(
    {
      actual: ['', [Validators.required]],
      nueva: ['', [Validators.required, Validators.minLength(6)]],
      repetir: ['', [Validators.required]],
    },
    { validators: this.passwordsMatchValidator },
  );

  passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
    const nueva = control.get('nueva')?.value;
    const repetir = control.get('repetir')?.value;

    if (nueva !== repetir && repetir !== '') {
      control.get('repetir')?.setErrors({ noMatch: true });
      return { noMatch: true };
    }
    return null;
  }

  // Métodos para alternar la visibilidad (Signals)
  toggleActual() {
    this.hideActual.update((v) => !v);
  }
  toggleNueva() {
    this.hideNueva.update((v) => !v);
  }
  toggleRepetir() {
    this.hideRepetir.update((v) => !v);
  }

  onSubmit() {
    // 1. Protección contra envíos dobles y formularios inválidos
    if (this.passwordForm.invalid || this.isSubmitting()) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    // 2. Reiniciamos estado
    this.errorMessage.set(null);
    this.isSubmitting.set(true);

    // 3. Flujo reactivo con RxJS
    this.authService
      .changeFirstPassword(this.passwordForm.getRawValue())
      .pipe(
        take(1),
        // finalize apaga el spinner independientemente de si hay éxito o error
        finalize(() => this.isSubmitting.set(false)),
      )
      .subscribe({
        next: () => {
          const role = this.tokenStorage.getRole();
          if (role === 'ROLE_ADMIN' || role === 'ADMIN') {
            this.router.navigate(['/admin/home']);
          } else {
            this.router.navigate(['/worker/home']);
          }
        },
        error: (err: HttpErrorResponse) => {
          console.error(err);
          // Si tuvieras tu errorHandlerService importado, lo usarías aquí.
          // Por ahora lo parseamos localmente:
          const msg =
            err.error?.message ||
            err.error ||
            'La contraseña actual es incorrecta o hubo un error.';
          this.errorMessage.set(typeof msg === 'string' ? msg : 'Error inesperado');
        },
      });
  }
}
