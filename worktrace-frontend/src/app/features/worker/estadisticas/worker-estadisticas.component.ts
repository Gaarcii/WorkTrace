import {
  Component,
  ChangeDetectionStrategy,
  OnInit,
  inject,
  signal,
  computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { take, finalize } from 'rxjs/operators';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatRippleModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { WorkerEstadisticasService } from '../../../shared/services/worker/worker-estadisticas.service';
import { startOfWeek, endOfWeek, addDays, isSameDay, format, parseISO } from 'date-fns';
import { es } from 'date-fns/locale';

interface DiaGrafico {
  fecha: string;
  fechaCorta: string;
  fechaOriginal: string;
  dia: string;
  horas: string;
  balance: string;
  balanceClass: string;
  altura: number;
}

interface ResumenDia {
  fecha: string;
  fechaOriginal: string;
  total: string;
  balance: string;
  balanceClass: string;
  estado: string;
  estadoColor: string;
}

interface SemanaData {
  rangoTexto: string;
  dias: DiaGrafico[];
  resumenDias: ResumenDia[];
}

interface IncidenciaVista {
  tipo: string;
  estado: string;
  estadoColor: string;
  fecha: string;
}

@Component({
  selector: 'app-worker-estadisticas',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatRippleModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './worker-estadisticas.component.html',
  styleUrls: ['./worker-estadisticas.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerEstadisticasComponent implements OnInit {
  private readonly estadisticasService = inject(WorkerEstadisticasService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly periodoSeleccionado = signal<'semana' | 'mes' | 'todo' | 'custom'>('semana');
  readonly currentWeekIndex = signal<number>(0);
  readonly mostrarFiltroFechas = signal<boolean>(false);
  readonly isDownloadingPdf = signal<boolean>(false);
  readonly diaSeleccionado = signal<DiaGrafico | null>(null);

  readonly estadisticas = this.estadisticasService.estadisticasSignal;

  readonly filterForm = this.fb.nonNullable.group({
    fechaInicio: ['', Validators.required],
    fechaFin: ['', Validators.required],
  });

  readonly horasTrabajadas = computed(() =>
    this.formatMinutos(this.estadisticas()?.minutosTrabajadosTotal ?? 0),
  );
  readonly balanceHorario = computed(() => {
    const b = this.estadisticas()?.balanceMinutos ?? 0;
    return `${b >= 0 ? '+' : '-'}${this.formatMinutos(Math.abs(b))}`;
  });
  readonly balanceClass = computed(() =>
    (this.estadisticas()?.balanceMinutos ?? 0) >= 0 ? 'balance-positivo' : 'balance-negativo',
  );
  readonly jornadasIncompletas = computed(() => this.estadisticas()?.jornadasIncompletas ?? 0);
  readonly totalIncidencias = computed(() => this.estadisticas()?.incidencias ?? 0);

  readonly incidenciasPeriodo = computed<IncidenciaVista[]>(() => {
    const incidenciasRaw = this.estadisticas()?.incidenciasList ?? [];

    return incidenciasRaw.map((inc) => {
      const estadoOriginal = inc.estado || 'PENDING';
      const stLower = estadoOriginal.toLowerCase();

      let estadoTraducido = 'Pendiente';
      let estadoColor = 'warning';

      if (
        stLower === 'resolved' ||
        stLower === 'resuelta' ||
        stLower === 'aprobada' ||
        stLower === 'approved'
      ) {
        estadoTraducido = 'Resuelta';
        estadoColor = 'success';
      } else if (stLower === 'rejected' || stLower === 'rechazada') {
        estadoTraducido = 'Rechazada';
        estadoColor = 'error';
      } else if (stLower === 'pending' || stLower === 'pendiente') {
        estadoTraducido = 'Pendiente';
        estadoColor = 'warning';
      }

      let fechaFormateada = inc.creacion || '';
      if (inc.fecha) {
        const partesFecha = inc.fecha.split('-');
        if (partesFecha.length === 3) {
          const [year, month, day] = partesFecha;
          const dateStr = `${day}/${month}/${year}`;
          const timeStr = inc.hora ? inc.hora.substring(0, 5) : '';
          fechaFormateada = `${dateStr} ${timeStr}`.trim();
        }
      }

      return {
        tipo: inc.tipoIncidencia || 'Incidencia',
        estado: estadoTraducido,
        estadoColor: estadoColor,
        fecha: fechaFormateada,
      };
    });
  });

  readonly historialPaginado = computed<SemanaData[]>(() => {
    const diarios = this.estadisticas()?.resumenDiario ?? [];
    if (diarios.length === 0) return [];

    const fechas = diarios.map((d) => parseISO(d.fecha));
    const inicioAbsoluto = startOfWeek(new Date(Math.min(...fechas.map((f) => f.getTime()))), {
      weekStartsOn: 1,
    });
    const finAbsoluto = endOfWeek(new Date(Math.max(...fechas.map((f) => f.getTime()))), {
      weekStartsOn: 1,
    });

    const semanas: SemanaData[] = [];
    let lunesActual = inicioAbsoluto;

    while (lunesActual <= finAbsoluto) {
      const diasGrafico: DiaGrafico[] = [];
      const resumenSemana: ResumenDia[] = [];
      const domingoActual = endOfWeek(lunesActual, { weekStartsOn: 1 });

      for (let i = 0; i < 7; i++) {
        const fechaDia = addDays(lunesActual, i);
        const datoReal = diarios.find((d) => isSameDay(parseISO(d.fecha), fechaDia));

        const trabajados = datoReal?.minutosTrabajados ?? 0;
        const previstos = datoReal?.minutosPrevistos ?? 0;
        const balance = trabajados - previstos;

        diasGrafico.push({
          fecha: format(fechaDia, 'dd/MM/yyyy'),
          fechaCorta: format(fechaDia, 'dd/MM'),
          fechaOriginal: format(fechaDia, 'yyyy-MM-dd'),
          dia: format(fechaDia, 'EEE', { locale: es }).toUpperCase(),
          horas: this.formatMinutos(trabajados),
          balance: `${balance >= 0 ? '+' : '-'}${this.formatMinutos(Math.abs(balance))}`,
          balanceClass: balance >= 0 ? 'balance-positivo' : 'balance-negativo',
          altura: Math.min((trabajados / 60 / 10) * 100, 100),
        });

        if (trabajados > 0 || previstos > 0) {
          resumenSemana.push({
            fecha: format(fechaDia, 'EEE d MMM', { locale: es }),
            fechaOriginal: format(fechaDia, 'yyyy-MM-dd'),
            total: this.formatMinutos(trabajados),
            balance: `${balance >= 0 ? '+' : '-'}${this.formatMinutos(Math.abs(balance))}`,
            balanceClass: balance >= 0 ? 'balance-positivo' : 'balance-negativo',
            estado: trabajados >= previstos ? 'Completa' : 'Incompleta',
            estadoColor: trabajados >= previstos ? 'success' : 'warning',
          });
        }
      }

      semanas.push({
        rangoTexto: `${format(lunesActual, 'd MMM')} - ${format(domingoActual, 'd MMM yyyy', { locale: es })}`,
        dias: diasGrafico,
        resumenDias: resumenSemana.reverse(),
      });
      lunesActual = addDays(lunesActual, 7);
    }
    return semanas.reverse();
  });

  readonly semanaVisible = computed<SemanaData | undefined>(
    () => this.historialPaginado()[this.currentWeekIndex()],
  );

  ngOnInit(): void {
    this.cargarDatosPorPeriodo('semana');
  }

  prevWeek() {
    if (this.currentWeekIndex() < this.historialPaginado().length - 1)
      this.currentWeekIndex.update((i) => i + 1);
  }
  nextWeek() {
    if (this.currentWeekIndex() > 0) this.currentWeekIndex.update((i) => i - 1);
  }

  cambiarPeriodo(periodo: 'semana' | 'mes' | 'todo'): void {
    this.periodoSeleccionado.set(periodo);
    this.currentWeekIndex.set(0);
    this.mostrarFiltroFechas.set(false);
    this.cargarDatosPorPeriodo(periodo);
  }

  toggleFiltro(): void {
    this.mostrarFiltroFechas.update((v) => !v);
  }

  aplicarFiltroPersonalizado(): void {
    if (this.filterForm.invalid) return;
    const { fechaInicio, fechaFin } = this.filterForm.getRawValue();
    this.periodoSeleccionado.set('custom');
    this.currentWeekIndex.set(0);
    this.mostrarFiltroFechas.set(false);
    this.estadisticasService.obtenerEstadisticas(fechaInicio, fechaFin).pipe(take(1)).subscribe();
  }

  mostrarTooltip(dia: DiaGrafico): void {
    this.diaSeleccionado.set(dia);
  }
  cerrarTooltip(): void {
    this.diaSeleccionado.set(null);
  }

  verDetalleDia(dia: any): void {
    this.router.navigate(['/worker/historial'], { queryParams: { fecha: dia.fechaOriginal } });
  }

  verHistorialDetallado(): void {
    this.router.navigate(['/worker/historial']);
  }

  descargarRegistros(): void {
    if (this.isDownloadingPdf()) return;
    this.isDownloadingPdf.set(true);

    let inicio: Date | undefined = undefined;
    let fin: Date | undefined = undefined;
    const periodo = this.periodoSeleccionado();

    if (periodo === 'custom') {
      const vals = this.filterForm.getRawValue();
      inicio = vals.fechaInicio ? new Date(vals.fechaInicio) : undefined;
      fin = vals.fechaFin ? new Date(vals.fechaFin) : undefined;
    } else if (periodo === 'semana') {
      fin = new Date();
      fin.setHours(23, 59, 59, 999);
      inicio = new Date(fin);
      inicio.setDate(fin.getDate() - 7);
      inicio.setHours(0, 0, 0, 0);
    } else if (periodo === 'mes') {
      fin = new Date();
      fin.setHours(23, 59, 59, 999);
      inicio = new Date(fin);
      inicio.setMonth(fin.getMonth() - 1);
      inicio.setHours(0, 0, 0, 0);
    }

    let request$;
    if (periodo === 'todo') {
      request$ = this.estadisticasService.descargarInformePdf();
    } else {
      request$ = this.estadisticasService.descargarInformePdf(inicio, fin);
    }

    request$
      .pipe(
        take(1),
        finalize(() => this.isDownloadingPdf.set(false)),
      )
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `informe_horas.pdf`;
          a.click();
          window.URL.revokeObjectURL(url);
        },
      });
  }

  private cargarDatosPorPeriodo(periodo: 'semana' | 'mes' | 'todo'): void {
    if (periodo === 'todo') {
      this.estadisticasService.obtenerEstadisticas().pipe(take(1)).subscribe();
    } else {
      const { start, end } = this.calcularRangoFechas(periodo);
      this.estadisticasService.obtenerEstadisticas(start, end).pipe(take(1)).subscribe();
    }
  }

  private calcularRangoFechas(periodo: string): { start: Date; end: Date } {
    const end = new Date();
    const start = new Date();
    if (periodo === 'semana') {
      const day = start.getDay();
      start.setDate(start.getDate() - day + (day === 0 ? -6 : 1));
    } else if (periodo === 'mes') {
      start.setDate(1);
    }
    start.setHours(0, 0, 0, 0);
    return { start, end };
  }

  private formatMinutos(totalMinutos: number): string {
    const h = Math.floor(Math.abs(totalMinutos) / 60);
    const m = Math.floor(Math.abs(totalMinutos) % 60);
    return `${h}h ${m}m`;
  }
}
