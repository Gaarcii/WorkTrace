import {
  Component,
  ChangeDetectionStrategy,
  OnInit,
  inject,
  signal,
  computed,
  effect,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
  ValidationErrors,
  ValidatorFn,
} from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { take, finalize } from 'rxjs/operators';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { WorkerIncidenciasService } from '../../../shared/services/worker/worker-incidencias.service';
import { WorkerIncidenceTypesService } from '../../../shared/services/worker/worker-tipos-incidencias.service';
import { IncidenceRequest } from '../../../shared/models/incidence.model';

@Component({
  selector: 'app-worker-incidencias',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './worker-incidencias.component.html',
  styleUrls: ['./worker-incidencias.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerIncidenciasComponent implements OnInit {
  private readonly incidenciasService = inject(WorkerIncidenciasService);
  private readonly tiposService = inject(WorkerIncidenceTypesService);
  private readonly fb = inject(FormBuilder);

  readonly mostrarFormulario = signal<boolean>(false);
  readonly mostrarConfirmacion = signal<boolean>(false);
  readonly loading = signal<boolean>(false);
  readonly mensajeError = signal<string>('');

  readonly maxDateStr = format(new Date(), 'yyyy-MM-dd');
  readonly incidencias = this.incidenciasService.incidenciasSignal;
  readonly tipos = this.tiposService.tiposSignal;

  readonly incidenciasFormateadas = computed(() => {
    return this.incidencias().map((inc) => {
      const stLower = (inc.estado || 'PENDING').toLowerCase();
      let estadoTraducido = 'Pendiente';
      let estadoColor = 'warning';

      if (['resolved', 'resuelta', 'aprobada', 'approved'].includes(stLower)) {
        estadoTraducido = 'Aprobada';
        estadoColor = 'success';
      } else if (['rejected', 'rechazada'].includes(stLower)) {
        estadoTraducido = 'Rechazada';
        estadoColor = 'error';
      }

      return {
        ...inc,
        estadoTraducido,
        estadoColor,
        horaFormateada: inc.hora ? inc.hora.substring(0, 5) : '',
      };
    });
  });

  readonly form = this.fb.nonNullable.group({
    tipoId: ['', Validators.required],
    fecha: [this.maxDateStr, [Validators.required, this.fechaPasadaOPresenteValidator()]],
    hora: [format(new Date(), 'HH:mm'), Validators.required],
    comentario: [''],
  });

  readonly tipoSeleccionadoId = toSignal(this.form.controls.tipoId.valueChanges, {
    initialValue: '',
  });

  readonly mostrarHora = computed(() => {
    const id = this.tipoSeleccionadoId();
    const tipo = this.tipos().find((t) => t.id === id);
    return id !== '' && !['Otro', 'Jornada especial'].includes(tipo?.name || '');
  });

  constructor() {
    effect(() => {
      const horaCtrl = this.form.controls.hora;
      if (this.mostrarHora()) {
        horaCtrl.setValidators([Validators.required]);
      } else {
        horaCtrl.clearValidators();
      }
      horaCtrl.updateValueAndValidity();
    });
  }

  ngOnInit(): void {
    this.incidenciasService.obtenerMisIncidencias().pipe(take(1)).subscribe();
    if (this.tipos().length === 0) {
      this.tiposService.obtenerTipos().pipe(take(1)).subscribe();
    }
  }

  abrirFormulario(): void {
    this.mostrarFormulario.set(true);
  }

  cerrarFormulario(): void {
    this.mostrarFormulario.set(false);
    this.mensajeError.set('');
    this.form.reset({
      tipoId: '',
      fecha: this.maxDateStr,
      hora: format(new Date(), 'HH:mm'),
      comentario: '',
    });
  }

  enviarIncidencia(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    const val = this.form.getRawValue();
    const payload: IncidenceRequest = {
      typeId: val.tipoId,
      fechaAfectada: val.fecha,
      hora: this.mostrarHora() ? val.hora : '',
      comentario: val.comentario,
    };

    this.incidenciasService
      .crearIncidencia(payload)
      .pipe(
        take(1),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: () => {
          this.cerrarFormulario();
          this.mostrarConfirmacion.set(true);
          setTimeout(() => this.mostrarConfirmacion.set(false), 3000);
        },
        error: (err) => this.mensajeError.set(err.message || 'Error al enviar'),
      });
  }

  formatearFecha(fecha: string): string {
    try {
      return format(new Date(fecha), 'dd/MM/yyyy', { locale: es });
    } catch {
      return fecha;
    }
  }

  private fechaPasadaOPresenteValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      const fechaSeleccionada = new Date(control.value);
      const hoy = new Date();
      hoy.setHours(23, 59, 59, 999);
      return fechaSeleccionada > hoy ? { fechaFutura: true } : null;
    };
  }
}
