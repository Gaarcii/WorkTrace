import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, take } from 'rxjs/operators';
import { AuthService } from '../../../core/auth/auth.service';
import { ResetPasswordRequest } from '../../../shared/models/auth.model';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-reset-password',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResetPasswordComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);

  readonly token = signal<string | null>(null);
  readonly invalidToken = signal(false);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal(false);
  readonly hideNewPassword = signal(true);
  readonly hideRepeatPassword = signal(true);

  readonly passwordsMatchValidator = (control: AbstractControl): ValidationErrors | null => {
    const newPassword = control.get('newPassword')?.value;
    const repeatPassword = control.get('repeatPassword')?.value;
    const repeatControl = control.get('repeatPassword');

    if (newPassword !== repeatPassword && repeatPassword !== '') {
      repeatControl?.setErrors({ ...(repeatControl.errors ?? {}), noMatch: true });
      return { noMatch: true };
    }

    if (repeatControl?.hasError('noMatch')) {
      const nextErrors = { ...(repeatControl.errors ?? {}) };
      delete nextErrors['noMatch'];
      repeatControl.setErrors(Object.keys(nextErrors).length ? nextErrors : null);
    }

    return null;
  };

  readonly resetPasswordForm: FormGroup = this.fb.group(
    {
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      repeatPassword: ['', [Validators.required]],
    },
    { validators: this.passwordsMatchValidator },
  );

  toggleNewPassword(): void {
    this.hideNewPassword.update((value) => !value);
  }

  toggleRepeatPassword(): void {
    this.hideRepeatPassword.update((value) => !value);
  }

  ngOnInit(): void {
    this.route.queryParamMap.pipe(take(1)).subscribe((params) => {
      const token = params.get('token');
      this.token.set(token);

      if (!token) {
        this.invalidToken.set(true);
        this.error.set('Enlace inválido');
      } else {
        this.invalidToken.set(false);
        this.error.set(null);
      }
    });
  }

  onSubmit(): void {
    if (this.resetPasswordForm.invalid || this.loading() || !this.token()) {
      this.resetPasswordForm.markAllAsTouched();
      return;
    }

    this.error.set(null);
    this.success.set(false);
    this.loading.set(true);

    const request: ResetPasswordRequest = {
      token: this.token() as string,
      newPassword: this.resetPasswordForm.getRawValue().newPassword as string,
      repeatPassword: this.resetPasswordForm.getRawValue().repeatPassword as string,
    };

    this.authService
      .ejecutarResetPassword(request)
      .pipe(
        take(1),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: () => {
          this.success.set(true);
          this.error.set(null);
          this.resetPasswordForm.reset();
        },
        error: (err: HttpErrorResponse) => {
          const msg = err.error?.message || err.error || 'No se pudo actualizar la contraseña.';
          this.error.set(typeof msg === 'string' ? msg : 'Error inesperado');
        },
      });
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}
