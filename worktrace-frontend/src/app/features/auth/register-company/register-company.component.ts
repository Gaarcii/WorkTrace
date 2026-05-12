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
import { take, finalize, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { TokenStorageService } from '../../../core/auth/token-storage.service';
import { AdminConfigService } from '../../../shared/services/admin/admin-config.service';
import { CompanyRequestDto } from '../../../shared/models/auth.model';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-register-company',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './register-company.component.html',
  styleUrl: './register-company.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterCompanyComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly adminConfigService = inject(AdminConfigService);

  readonly errorMessage = signal<string | null>(null);
  readonly isSubmitting = signal(false);
  readonly hidePassword = signal(true);
  readonly hideRepeatPassword = signal(true);
  readonly logoPreview = signal<string | null>(null);
  readonly selectedFile = signal<File | null>(null);

  registerForm: FormGroup = this.fb.group({
    companyName: ['', [Validators.required, Validators.minLength(3)]],
    cif: ['', [Validators.required, Validators.minLength(8)]],
    adminFullName: ['', [Validators.required, Validators.minLength(3)]],
    adminEmployeeCode: ['', [Validators.required, Validators.minLength(6)]],
    adminPhone: ['', [Validators.required, Validators.pattern(/^\+?[0-9\s\-()]{9,}$/)]],
    adminEmail: ['', [Validators.required, Validators.email]],
    adminPassword: ['', [Validators.required, Validators.minLength(6)]],
    adminRepeatPassword: ['', Validators.required],
  });

  constructor() {
    this.registerForm.setValidators(this.passwordMatchValidator);
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('adminPassword');
    const repeatPassword = control.get('adminRepeatPassword');

    if (!password || !repeatPassword) {
      return null;
    }

    return password.value === repeatPassword.value ? null : { passwordMismatch: true };
  }

  togglePassword() {
    this.hidePassword.update((v) => !v);
  }

  toggleRepeatPassword() {
    this.hideRepeatPassword.update((v) => !v);
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];

      // Validar que sea una imagen
      if (!file.type.startsWith('image/')) {
        this.errorMessage.set('Por favor, selecciona una imagen válida');
        return;
      }

      // Validar tamaño (máximo 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.errorMessage.set('La imagen no debe superar 5MB');
        return;
      }

      this.selectedFile.set(file);
      this.errorMessage.set(null);

      // Crear preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.logoPreview.set(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  }

  removeLogo() {
    this.selectedFile.set(null);
    this.logoPreview.set(null);
  }

  onSubmit() {
    if (this.registerForm.invalid || this.isSubmitting()) {
      this.registerForm.markAllAsTouched();
      return;
    }

    if (this.registerForm.hasError('passwordMismatch')) {
      this.errorMessage.set('Las contraseñas no coinciden');
      return;
    }

    this.errorMessage.set(null);
    this.isSubmitting.set(true);

    const formValue = this.registerForm.getRawValue();

    const companyData: CompanyRequestDto = {
      companyName: formValue.companyName,
      cif: formValue.cif,
      admin: {
        email: formValue.adminEmail,
        password: formValue.adminPassword,
        profile: {
          fullName: formValue.adminFullName,
          employeeCode: formValue.adminEmployeeCode,
          phone: formValue.adminPhone,
        },
      },
    };

    this.authService
      .registerCompany(companyData)
      .pipe(
        take(1),
        switchMap((response) => {
          // Si hay logo, subirlo después de registrar
          if (this.selectedFile()) {
            return this.adminConfigService.updateCompanyLogo(this.selectedFile()!).pipe(
              switchMap(() => of(response)), // Retornar la respuesta original
            );
          }
          return of(response);
        }),
        finalize(() => this.isSubmitting.set(false)),
      )
      .subscribe({
        next: (response) => {
          this.tokenStorage.saveToken(response.token);
          this.tokenStorage.saveRole(response.role);

          if (response.role === 'ROLE_ADMIN' || response.role === 'ADMIN') {
            this.router.navigate(['/admin/home']);
          } else {
            this.router.navigate(['/login']);
          }
        },
        error: (err: HttpErrorResponse) => {
          const msg =
            err.error?.message || err.error?.error || 'Error en el registro. Inténtalo de nuevo.';
          this.errorMessage.set(typeof msg === 'string' ? msg : 'Error inesperado');
        },
      });
  }

  returnToLogin() {
    this.router.navigate(['/login']);
  }
}
