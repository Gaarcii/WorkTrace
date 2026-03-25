import {
  Component,
  ChangeDetectionStrategy,
  OnInit,
  inject,
  signal,
  computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { take, finalize } from 'rxjs/operators';

// Angular Material Imports
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

// Servicios y Modelos
import { ProfileService } from '../../../shared/services/profile.service';
import { AuthService } from '../../../core/auth/auth.service';
import { ProfileRequest, ProfileResponse } from '../../../shared/models/profile.model';
import { PasswordChangeRequest } from '../../../shared/models/auth.model';

@Component({
  selector: 'app-worker-perfil',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './worker-perfil.component.html',
  styleUrls: ['./worker-perfil.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerPerfilComponent implements OnInit {
  private readonly profileService = inject(ProfileService);
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly profile = this.profileService.currentUser;

  readonly loading = signal<boolean>(false);
  readonly errorMessage = signal<string>('');
  readonly mostrarConfirmacion = signal<boolean>(false);
  readonly mostrarError = signal<boolean>(false);

  // Modales Avatar
  readonly mostrarSelectorAvatar = signal<boolean>(false);
  readonly archivoSeleccionado = signal<File | null>(null);
  readonly avatarPreview = signal<string>('');

  // Modales Contraseña
  readonly mostrarCambiarPassword = signal<boolean>(false);
  readonly loadingPassword = signal<boolean>(false);
  readonly passwordError = signal<string>('');
  readonly showCurrentPassword = signal<boolean>(false);
  readonly showNewPassword = signal<boolean>(false);
  readonly showConfirmPassword = signal<boolean>(false);

  readonly mostrarPedirPasswordEmail = signal<boolean>(false);
  readonly hideConfirmPassword = signal<boolean>(true);

  // Formulario de Datos
  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    telefono: ['', Validators.required],
  });

  // Formulario de Contraseña
  readonly passwordForm = this.fb.group(
    {
      actual: ['', [Validators.required]],
      nueva: ['', [Validators.required, Validators.minLength(6)]],
      repetir: ['', [Validators.required]],
    },
    { validators: this.passwordsMatchValidator },
  );

  readonly passwordConfirmacion = this.fb.control('', Validators.required);

  // --- LÓGICA DEL CALENDARIO HORIZONTAL ---
  readonly horarioSemanal = computed(() => {
    const diasSemana = [
      'MONDAY',
      'TUESDAY',
      'WEDNESDAY',
      'THURSDAY',
      'FRIDAY',
      'SATURDAY',
      'SUNDAY',
    ];
    const iniciales: Record<string, string> = {
      MONDAY: 'L',
      TUESDAY: 'M',
      WEDNESDAY: 'X',
      THURSDAY: 'J',
      FRIDAY: 'V',
      SATURDAY: 'S',
      SUNDAY: 'D',
    };

    const horarioBackend = this.profile()?.horario || [];

    return diasSemana.map((diaEnum) => {
      const turno = horarioBackend.find((h) => h.diaSemana.toUpperCase() === diaEnum);
      return {
        id: diaEnum,
        inicial: iniciales[diaEnum],
        trabaja: !!turno,
        start: turno?.start ? turno.start.substring(0, 5) : null,
        end: turno?.end ? turno.end.substring(0, 5) : null,
        lugar: turno?.lugar || '',
        ubicacion: turno?.ubicacion || '',
      };
    });
  });

  ngOnInit(): void {
    this.cargarDatosPerfil();
  }

  private cargarDatosPerfil(): void {
    this.loading.set(true);
    this.profileService
      .fetchMyProfile()
      .pipe(
        take(1),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (res: ProfileResponse) => {
          this.form.patchValue({ email: res.email, telefono: res.telefono });
        },
        error: () => this.mostrarMensajeError('Error al cargar el perfil'),
      });
  }

  guardarPerfil(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const formValues = this.form.getRawValue();
    const emailOriginal = this.profile()?.email;

    if (emailOriginal && formValues.email !== emailOriginal) {
      this.mostrarPedirPasswordEmail.set(true);
      return;
    }
    this.ejecutarActualizacion('');
  }

  confirmarGuardarConPassword(): void {
    if (this.passwordConfirmacion.invalid) {
      this.passwordConfirmacion.markAsTouched();
      return;
    }
    this.ejecutarActualizacion(this.passwordConfirmacion.value || '');
  }

  cerrarModalPasswordEmail(): void {
    this.mostrarPedirPasswordEmail.set(false);
    this.passwordConfirmacion.reset();
  }

  private ejecutarActualizacion(contrasenaActual: string): void {
    this.loading.set(true);
    const formValues = this.form.getRawValue();

    const request: ProfileRequest = {
      email: formValues.email,
      telefono: formValues.telefono,
      contrasenaActual: contrasenaActual,
      eliminarAvatar: 'false',
      avatar: this.archivoSeleccionado() ? this.archivoSeleccionado() : null,
    };

    this.profileService
      .updateMyProfile(request)
      .pipe(
        take(1),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: () => {
          this.cerrarModalPasswordEmail();
          this.cerrarSelectorAvatar();
          this.mostrarConfirmacion.set(true);
          setTimeout(() => this.mostrarConfirmacion.set(false), 3000);
        },
        error: (err) => {
          const msg = err.error?.message || 'Error al guardar los cambios o contraseña incorrecta';
          this.mostrarMensajeError(msg);
        },
      });
  }

  abrirSelectorAvatar(): void {
    this.mostrarSelectorAvatar.set(true);
  }
  cerrarSelectorAvatar(): void {
    this.mostrarSelectorAvatar.set(false);
    this.archivoSeleccionado.set(null);
    this.avatarPreview.set('');
    this.errorMessage.set('');
  }

  procesarArchivo(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    if (!file.type.startsWith('image/')) {
      this.mostrarMensajeError('Por favor selecciona una imagen válida');
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.mostrarMensajeError('La imagen no puede superar 5MB');
      return;
    }

    this.archivoSeleccionado.set(file);
    this.avatarPreview.set(URL.createObjectURL(file));
  }

  eliminarFoto(): void {
    this.loading.set(true);
    const formValues = this.form.getRawValue();
    const request: ProfileRequest = {
      email: formValues.email,
      telefono: formValues.telefono,
      contrasenaActual: '',
      eliminarAvatar: 'true',
    };

    this.profileService
      .updateMyProfile(request)
      .pipe(
        take(1),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: () => {
          this.cerrarSelectorAvatar();
          this.mostrarConfirmacion.set(true);
          setTimeout(() => this.mostrarConfirmacion.set(false), 3000);
        },
        error: () => this.mostrarMensajeError('Error al eliminar la foto'),
      });
  }

  abrirModalPassword(): void {
    this.mostrarCambiarPassword.set(true);
  }
  cerrarModalPassword(): void {
    this.mostrarCambiarPassword.set(false);
    this.passwordForm.reset();
    this.passwordError.set('');
  }

  toggleActual() {
    this.showCurrentPassword.set(!this.showCurrentPassword());
  }
  toggleNueva() {
    this.showNewPassword.set(!this.showNewPassword());
  }
  toggleRepetir() {
    this.showConfirmPassword.set(!this.showConfirmPassword());
  }

  passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
    const nueva = control.get('nueva')?.value;
    const repetir = control.get('repetir')?.value;
    if (nueva !== repetir && repetir !== '') {
      control.get('repetir')?.setErrors({ noMatch: true });
      return { noMatch: true };
    }
    return null;
  }

  actualizarPassword(): void {
    if (this.passwordForm.invalid || this.loadingPassword()) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.passwordError.set('');
    this.loadingPassword.set(true);
    const values = this.passwordForm.getRawValue();

    const request: PasswordChangeRequest = {
      actual: values.actual || '',
      nueva: values.nueva || '',
      repetir: values.repetir || '',
    };

    this.authService
      .changeFirstPassword(request)
      .pipe(
        take(1),
        finalize(() => this.loadingPassword.set(false)),
      )
      .subscribe({
        next: () => {
          this.cerrarModalPassword();
          this.mostrarConfirmacion.set(true);
          setTimeout(() => this.mostrarConfirmacion.set(false), 3000);
        },
        error: (err) => {
          const msg =
            err.error?.message ||
            err.error ||
            'La contraseña actual es incorrecta o hubo un error.';
          this.passwordError.set(typeof msg === 'string' ? msg : 'Error inesperado');
        },
      });
  }

  private mostrarMensajeError(msg: string): void {
    this.errorMessage.set(msg);
    this.mostrarError.set(true);
    setTimeout(() => this.mostrarError.set(false), 5000);
  }
}
