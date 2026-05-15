import {
  Component,
  ChangeDetectionStrategy,
  OnInit,
  inject,
  signal,
  computed,
  input,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { take, finalize } from 'rxjs/operators';
import { ProfileService } from '../../shared/services/profile.service';
import { AuthService } from '../../core/auth/auth.service';
import { ProfileRequest, ProfileResponse } from '../../shared/models/profile.model';
import { PasswordChangeRequest } from '../../shared/models/auth.model';
import { ScheduleDay } from '../../shared/models/work-schedule.model';
import { PerfilIdentityComponent } from './components/perfil-identity/perfil-identity.component';
import { PerfilContactComponent } from './components/perfil-contact/perfil-contact.component';
import { PerfilSecurityComponent } from './components/perfil-security/perfil-security.component';
import { PerfilScheduleComponent } from './components/perfil-schedule/perfil-schedule.component';
import { PerfilAvatarModalComponent } from './components/perfil-avatar-modal/perfil-avatar-modal.component';
import { PerfilPasswordModalComponent } from './components/perfil-password-modal/perfil-password-modal.component';
import { PerfilEmailModalComponent } from './components/perfil-email-modal/perfil-email-modal.component';

@Component({
  selector: 'app-profile-page',
  imports: [
    CommonModule,
    PerfilIdentityComponent,
    PerfilContactComponent,
    PerfilSecurityComponent,
    PerfilScheduleComponent,
    PerfilAvatarModalComponent,
    PerfilPasswordModalComponent,
    PerfilEmailModalComponent,
  ],
  templateUrl: './profile-page.component.html',
  styleUrls: ['./profile-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfilePageComponent implements OnInit {
  private readonly profileService = inject(ProfileService);
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly showSchedule = input<boolean>(true);
  readonly subtitle = input<string>('Gestiona tu informacion personal y revisa tu jornada');

  readonly profile = this.profileService.currentUser;

  readonly loading = signal<boolean>(false);
  readonly errorMessage = signal<string>('');
  readonly mostrarConfirmacion = signal<boolean>(false);
  readonly mostrarError = signal<boolean>(false);

  readonly mostrarSelectorAvatar = signal<boolean>(false);
  readonly archivoSeleccionado = signal<File | null>(null);
  readonly avatarPreview = signal<string>('');

  readonly mostrarCambiarPassword = signal<boolean>(false);
  readonly loadingPassword = signal<boolean>(false);
  readonly passwordError = signal<string>('');

  readonly mostrarPedirPasswordEmail = signal<boolean>(false);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    telefono: ['', Validators.required],
  });

  readonly passwordForm = this.fb.group(
    {
      actual: ['', [Validators.required]],
      nueva: ['', [Validators.required, Validators.minLength(6)]],
      repetir: ['', [Validators.required]],
    },
    { validators: this.passwordsMatchValidator },
  );

  readonly horarioSemanal = computed<ScheduleDay[]>(() => {
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

    const horarioBackend = this.profile()?.schedule || [];

    return diasSemana.map((diaEnum) => {
      const turno = horarioBackend.find(
        (h: {
          dayOfWeek?: string;
          diaSemana?: string;
          start?: string;
          end?: string;
          place?: string;
          location?: string;
        }) => {
          const rawDay = h?.dayOfWeek ?? h?.diaSemana;
          return typeof rawDay === 'string' && rawDay.toUpperCase() === diaEnum;
        },
      );
      return {
        id: diaEnum,
        initial: iniciales[diaEnum],
        work: !!turno,
        start: turno?.start ? turno.start.substring(0, 5) : null,
        end: turno?.end ? turno.end.substring(0, 5) : null,
        place: turno?.place || '',
        location: turno?.location || '',
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
          this.form.patchValue({ email: res.email, telefono: res.phone });
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

  confirmarGuardarConPassword(password: string): void {
    if (!password) {
      return;
    }
    this.ejecutarActualizacion(password);
  }

  cerrarModalPasswordEmail(): void {
    this.mostrarPedirPasswordEmail.set(false);
  }

  private ejecutarActualizacion(contrasenaActual: string): void {
    this.loading.set(true);
    const formValues = this.form.getRawValue();

    const request: ProfileRequest = {
      email: formValues.email,
      phone: formValues.telefono,
      actualPassword: contrasenaActual,
      deleteAvatar: 'false',
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
          const msg = err.error?.message || 'Error al guardar los cambios o contrasena incorrecta';
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
      this.mostrarMensajeError('Por favor selecciona una imagen valida');
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
    const request: ProfileRequest = {
      email: '',
      phone: '',
      actualPassword: '',
      deleteAvatar: 'true',
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
      currentPassword: values.actual || '',
      newPassword: values.nueva || '',
      repeatPassword: values.repetir || '',
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
            'La contrasena actual es incorrecta o hubo un error.';
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
