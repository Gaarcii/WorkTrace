import { Component, inject, ChangeDetectionStrategy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { take, finalize } from 'rxjs/operators';
import { AuthService } from '../../../core/auth/auth.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TokenStorageService } from '../../../core/auth/token-storage.service';

@Component({
  selector: 'app-login',
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
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  // Activamos el máximo rendimiento
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private readonly tokenStorage = inject(TokenStorageService);

  // Transformamos el estado a Signals
  errorMessage = signal<string | null>(null);
  hidePassword = signal(true);
  isSubmitting = signal(false);

  loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  // Método limpio para alternar el ojito de la contraseña
  togglePassword() {
    this.hidePassword.update((v) => !v);
  }

  onSubmit() {
    // 1. Bloqueo de envíos si el formulario es inválido o ya está cargando
    if (this.loginForm.invalid || this.isSubmitting()) {
      this.loginForm.markAllAsTouched();
      return;
    }

    // 2. Reinicio del estado visual
    this.errorMessage.set(null);
    this.isSubmitting.set(true);

    // 3. Petición HTTP reactiva
    this.authService
      .login(this.loginForm.getRawValue())
      .pipe(
        take(1),
        // Finalize apaga el spinner automáticamente al terminar (éxito o error)
        finalize(() => this.isSubmitting.set(false)),
      )
      .subscribe({
        next: (response) => {
          this.tokenStorage.saveToken(response.token);
          this.tokenStorage.saveRole(response.role);

          if (response.firstLogin) {
            this.router.navigate(['/change-password']);
            return;
          }

          if (response.role === 'ROLE_ADMIN' || response.role === 'ADMIN') {
            this.router.navigate(['/admin/home']);
          } else {
            this.router.navigate(['/worker/home']);
          }
        },
        error: (err: HttpErrorResponse) => {
          console.error(err);
          this.errorMessage.set('Credenciales incorrectas. Inténtalo de nuevo.');
        },
      });
  }
}
