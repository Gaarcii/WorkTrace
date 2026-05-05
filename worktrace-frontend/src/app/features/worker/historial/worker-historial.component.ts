import {
  Component,
  ChangeDetectionStrategy,
  OnInit,
  inject,
  signal,
  computed,
} from '@angular/core';
import { take } from 'rxjs/operators';
import { WorkerHistoryService } from '../../../shared/services/worker/worker-historial.service';
import { ActivatedRoute } from '@angular/router';
import { WorkerHistoryHeaderComponent } from './worker-history-header/worker-history-header.component';
import { WorkerHistoryStatsComponent } from './worker-history-stats/worker-history-stats.component';
import {
  WorkerHistoryRecordsComponent,
  FormattedRegistro,
} from './worker-history-records/worker-history-records.component';
import {
  WorkerHistoryDaysComponent,
  DiaSemana,
} from './worker-history-days/worker-history-days.component';

@Component({
  selector: 'app-worker-history',
  imports: [
    WorkerHistoryHeaderComponent,
    WorkerHistoryDaysComponent,
    WorkerHistoryStatsComponent,
    WorkerHistoryRecordsComponent,
  ],
  templateUrl: './worker-historial.component.html',
  styleUrls: ['./worker-historial.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerHistoryComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly historyService = inject(WorkerHistoryService);

  readonly fechaSeleccionada = signal<Date>(new Date());
  readonly semanaActual = signal<Date>(this.getStartOfWeek(new Date()));

  readonly historial = this.historyService.historialSignal;

  readonly rangoSemana = computed(() => {
    const inicio = this.semanaActual();
    const fin = this.addDays(inicio, 6);

    const diaInicio = inicio.getDate();
    const mesInicio = inicio.toLocaleDateString('es-ES', { month: 'long' });
    const diaFin = fin.getDate();
    const mesFin = fin.toLocaleDateString('es-ES', { month: 'long' });
    const anio = inicio.getFullYear();

    if (mesInicio === mesFin) {
      return `${diaInicio}-${diaFin} ${mesInicio} ${anio}`;
    } else {
      return `${diaInicio} ${mesInicio} - ${diaFin} ${mesFin} ${anio}`;
    }
  });

  readonly diasSemana = computed<DiaSemana[]>(() => {
    const dias: DiaSemana[] = [];
    const inicio = this.semanaActual();
    const seleccionada = this.fechaSeleccionada();

    for (let i = 0; i < 7; i++) {
      const fechaActual = this.addDays(inicio, i);
      dias.push({
        fecha: fechaActual.toISOString(),
        nombre: fechaActual.toLocaleDateString('es-ES', { weekday: 'short' }).toUpperCase(),
        numero: fechaActual.getDate(),
        seleccionado: this.isSameDay(fechaActual, seleccionada),
      });
    }
    return dias;
  });

  readonly estadisticasDia = computed(() => {
    const data = this.historial();
    const trabajados = data?.minutosTrabajadosDia ?? 0;
    const objetivo = data?.minutosObjetivoDia ?? 0;
    const balance = trabajados - objetivo;

    return {
      horas: this.formatMinutos(trabajados),
      balance: (balance >= 0 ? '+ ' : '- ') + this.formatMinutos(Math.abs(balance)),
      isPositive: balance >= 0,
    };
  });

  readonly estadisticasSemana = computed(() => {
    const data = this.historial();
    const trabajados = parseInt(data?.minutosTrabajadosSemana || '0', 10);
    const objetivo = parseInt(data?.minutosObjetivoSemana || '0', 10);
    const balance = trabajados - objetivo;

    return {
      horas: this.formatMinutos(trabajados),
      balance: (balance >= 0 ? '+ ' : '- ') + this.formatMinutos(Math.abs(balance)),
      isPositive: balance >= 0,
    };
  });

  readonly fichajesDelDia = computed<FormattedRegistro[]>(() => {
    const registros = this.historial()?.registrosDia ?? [];

    return registros.map((registro) => {
      const fechaObj = new Date(registro.fecha);
      const isEntrada = registro.tipoEvento.toUpperCase() === 'ENTRADA';

      return {
        ...registro,
        icono: isEntrada ? 'login' : 'logout',
        tipoStr: isEntrada ? 'Entrada' : 'Salida',
        fechaStr: fechaObj.toLocaleDateString('es-ES', {
          weekday: 'long',
          day: 'numeric',
          month: 'long',
        }),
        horaStr: fechaObj.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' }),
      };
    });
  });

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      const fechaParam = params.get('fecha');
      if (fechaParam) {
        const [year, month, day] = fechaParam.split('-').map(Number);
        const fecha = new Date(year, month - 1, day);
        this.fechaSeleccionada.set(fecha);
        this.semanaActual.set(this.getStartOfWeek(fecha));
        this.cargarHistorial(fecha);
      } else {
        this.cargarHistorial(this.fechaSeleccionada());
      }
    });
  }

  semanaAnterior(): void {
    const nuevaSemana = this.addDays(this.semanaActual(), -7);
    this.semanaActual.set(nuevaSemana);
    this.ajustarFechaSeleccionada(nuevaSemana);
  }

  semanaSiguiente(): void {
    const nuevaSemana = this.addDays(this.semanaActual(), 7);
    this.semanaActual.set(nuevaSemana);
    this.ajustarFechaSeleccionada(nuevaSemana);
  }

  seleccionarDia(dia: DiaSemana): void {
    const nuevaFecha = new Date(dia.fecha);
    this.fechaSeleccionada.set(nuevaFecha);
    this.cargarHistorial(nuevaFecha);
  }

  onDateChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.value) return;

    const nuevaFecha = new Date(input.value + 'T00:00:00');
    if (!isNaN(nuevaFecha.getTime())) {
      this.fechaSeleccionada.set(nuevaFecha);
      this.semanaActual.set(this.getStartOfWeek(nuevaFecha));
      this.cargarHistorial(nuevaFecha);
    }
  }

  private cargarHistorial(fecha: Date): void {
    this.historyService.getHistoryByDate(fecha).pipe(take(1)).subscribe();
  }

  private ajustarFechaSeleccionada(inicioSemana: Date): void {
    const finSemana = this.addDays(inicioSemana, 6);
    const seleccionada = this.fechaSeleccionada();

    if (seleccionada < inicioSemana || seleccionada > finSemana) {
      this.fechaSeleccionada.set(inicioSemana);
      this.cargarHistorial(inicioSemana);
    }
  }

  private getStartOfWeek(date: Date): Date {
    const result = new Date(date);
    const day = result.getDay();
    const diff = result.getDate() - day + (day === 0 ? -6 : 1);
    result.setDate(diff);
    result.setHours(0, 0, 0, 0);
    return result;
  }

  private addDays(date: Date, days: number): Date {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
  }

  private isSameDay(d1: Date, d2: Date): boolean {
    return (
      d1.getFullYear() === d2.getFullYear() &&
      d1.getMonth() === d2.getMonth() &&
      d1.getDate() === d2.getDate()
    );
  }

  private formatMinutos(totalMinutos: number): string {
    const h = Math.floor(Math.abs(totalMinutos) / 60);
    const m = Math.floor(Math.abs(totalMinutos) % 60);
    return `${h}h ${m}m`;
  }
}
