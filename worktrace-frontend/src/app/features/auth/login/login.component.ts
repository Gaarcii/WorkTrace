import { Component, inject, ChangeDetectionStrategy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
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
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private readonly tokenStorage = inject(TokenStorageService);

  readonly errorMessage = signal<string | null>(null);
  readonly hidePassword = signal(true);
  readonly isSubmitting = signal(false);
  readonly forgotPasswordMode = signal(false);
  readonly forgotPasswordLoading = signal(false);
  readonly forgotPasswordMessage = signal<string | null>(null);
  readonly forgotPasswordError = signal<string | null>(null);

  loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  forgotPasswordForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
  });

  togglePassword() {
    this.hidePassword.update((v) => !v);
  }

  enableForgotPasswordMode() {
    this.forgotPasswordMode.set(true);
    this.errorMessage.set(null);
    this.forgotPasswordMessage.set(null);
    this.forgotPasswordError.set(null);
  }

  returnToLoginMode() {
    this.forgotPasswordMode.set(false);
    this.forgotPasswordForm.reset();
    this.forgotPasswordMessage.set(null);
    this.forgotPasswordError.set(null);
  }

  onSubmit() {
    if (this.loginForm.invalid || this.isSubmitting()) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.errorMessage.set(null);
    this.isSubmitting.set(true);

    this.authService
      .login(this.loginForm.getRawValue())
      .pipe(
        take(1),
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
          } else if (response.role === 'ROLE_INSPECTOR' || response.role === 'INSPECTOR') {
            this.router.navigate(['/inspector/home']);
          } else {
            this.router.navigate(['/worker/home']);
          }
        },
        error: () => {
          this.errorMessage.set('Credenciales incorrectas. Inténtalo de nuevo.');
        },
      });
  }

  onForgotPasswordSubmit() {
    if (this.forgotPasswordForm.invalid || this.forgotPasswordLoading()) {
      this.forgotPasswordForm.markAllAsTouched();
      return;
    }

    this.forgotPasswordError.set(null);
    this.forgotPasswordMessage.set(null);
    this.forgotPasswordLoading.set(true);

    const email = this.forgotPasswordForm.getRawValue().email as string;

    this.authService
      .solicitarRecuperacion(email)
      .pipe(
        take(1),
        finalize(() => this.forgotPasswordLoading.set(false)),
      )
      .subscribe({
        next: (response) => {
          this.forgotPasswordMessage.set(
            response.message || 'Revisa tu correo para continuar con la recuperación.',
          );
        },
        error: (err: HttpErrorResponse) => {
          const msg = err.error?.message || err.error || 'No se pudo enviar el enlace.';
          this.forgotPasswordError.set(typeof msg === 'string' ? msg : 'Error inesperado');
        },
      });
  }
}
