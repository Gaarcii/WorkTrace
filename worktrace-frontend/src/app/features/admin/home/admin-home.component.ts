import { Component, OnInit, ViewChild, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { finalize, firstValueFrom, take } from 'rxjs';
import { format, formatDistanceToNow } from 'date-fns';
import { es } from 'date-fns/locale';
import { AdminHomeStatsComponent } from './stats/admin-home-stats.component';
import { AdminRecentAlertsComponent } from './alerts/admin-recent-alerts.component';
import { AdminWeeklyChartComponent } from './weekly-chart/admin-weekly-chart.component';
import { AdminDepartmentsComponent } from './department-stat/admin-departments.component';
import { AdminActiveWorkersComponent } from './active-workers/admin-active-workers.component';
import { AdminSelectedDayEntriesComponent } from './selected-day-entries/admin-selected-day-entries.component';
import { AdminQuickActionsComponent } from './quick-actions/admin-quick-actions.component';
import { AdminExportLegalDialogComponent } from './export-legal-dialog/admin-export-legal-dialog.component';
import { AdminHomeService } from '../../../shared/services/admin/admin-home.service';
import {
  ActiveWorkerDto,
  AdminTimeEntryByDateResponseDto,
  DailyTimeEntryCountDto,
} from '../../../shared/models/time-entry.model';
import { AdminIncidenceResponseDto } from '../../../shared/models/incidence.model';
import { DepartmentStatDto } from '../../../shared/models/profile.model';
import {
  ActiveWorkerCard,
  DashboardAlert,
  DepartmentSummary,
  FormatoExportacion,
  QuickAction,
  RangoExportacion,
  SelectedDayEntry,
  WeekChartDay,
} from './admin-home.types';
import { InspectorRequestDto } from '../../../shared/models/inspector.model';
import { AdminCreateInspectorDialogComponent } from './create-inspector-modal/admin-create-inspector-dialog.component';

@Component({
  selector: 'app-admin-home',
  imports: [
    AdminHomeStatsComponent,
    AdminRecentAlertsComponent,
    AdminWeeklyChartComponent,
    AdminDepartmentsComponent,
    AdminSelectedDayEntriesComponent,
    AdminActiveWorkersComponent,
    AdminQuickActionsComponent,
    AdminExportLegalDialogComponent,
    AdminCreateInspectorDialogComponent,
  ],
  templateUrl: './admin-home.component.html',
  styleUrl: './admin-home.component.scss',
})
export class AdminHomeComponent implements OnInit {
  private readonly adminHomeService = inject(AdminHomeService);
  private readonly router = inject(Router);

  @ViewChild('datePicker') private datePicker?: { nativeElement: HTMLInputElement };

  readonly loading = signal<boolean>(true);

  readonly trabajadoresActivos = signal<number>(0);
  readonly totalTrabajadores = signal<number>(0);
  readonly fichajesHoy = signal<number>(0);
  readonly horasTotales = signal<number>(0);
  readonly trabajadoresList = signal<ActiveWorkerCard[]>([]);
  readonly departamentos = signal<DepartmentSummary[]>([]);
  readonly departamentoSeleccionado = signal<string | null>(null);
  readonly alertasRecientes = signal<DashboardAlert[]>([]);
  readonly alertasPendientes = signal<number>(0);

  readonly fichajesSemana = signal<WeekChartDay[]>([]);
  readonly semanaSeleccionada = signal<number>(0);
  readonly opcionesSemana = signal<string[]>([
    'Esta semana',
    'Semana pasada',
    'Hace 2 semanas',
    'Hace 3 semanas',
  ]);
  readonly textoSemanaSeleccionada = signal<string>('Esta semana');

  readonly diaSeleccionado = signal<string | null>(null);
  readonly fichajesDiaSeleccionado = signal<SelectedDayEntry[]>([]);

  readonly dialogoInspector = signal<boolean>(false);
  readonly cargandoInspector = signal<boolean>(false);
  readonly dialogoExportar = signal<boolean>(false);
  readonly rangoExportacion = signal<RangoExportacion>('mes');
  readonly formatoExportacion = signal<FormatoExportacion>('pdf');
  readonly cargandoExportacion = signal<boolean>(false);

  readonly acciones = signal<QuickAction[]>([
    { icono: 'mdi-download', texto: 'Exportar' },
    { icono: 'mdi-account-plus', texto: 'Añadir' },
    { icono: 'mdi-calendar-blank', texto: 'Horarios' },
    { icono: 'mdi-bell-outline', texto: 'Incidencias' },
    { icono: 'mdi-security', texto: 'Auditor' },
  ]);

  readonly trabajadoresListFiltrados = computed<ActiveWorkerCard[]>(() => {
    const departamento = this.departamentoSeleccionado();
    if (!departamento) {
      return this.trabajadoresList();
    }

    const normalizado = departamento.trim().toLowerCase();
    return this.trabajadoresList().filter(
      (worker) => worker.departamento.trim().toLowerCase() === normalizado,
    );
  });

  readonly rangoSemana = computed<string>(() => {
    const inicioSemana = this.getInicioSemanaSeleccionada();
    const finSemana = new Date(inicioSemana);
    finSemana.setDate(inicioSemana.getDate() + 6);

    const diaInicio = format(inicioSemana, 'd');
    const mesInicio = format(inicioSemana, 'MMMM', { locale: es });
    const diaFin = format(finSemana, 'd');
    const mesFin = format(finSemana, 'MMMM', { locale: es });
    const anio = format(inicioSemana, 'yyyy');

    if (mesInicio === mesFin) {
      return `${diaInicio}-${diaFin} ${mesInicio} ${anio}`;
    }

    return `${diaInicio} ${mesInicio} - ${diaFin} ${mesFin} ${anio}`;
  });

  readonly leyendaGrafico = computed<string>(() => {
    const seleccionado = this.diaSeleccionado();
    if (seleccionado) {
      const dia = this.fichajesSemana().find((item) => item.date === seleccionado);
      if (dia) {
        const fecha = new Date(dia.date);
        const fechaFormateada = format(fecha, "d 'de' MMMM", { locale: es });
        return `${fechaFormateada}: ${dia.valor} fichajes`;
      }
    }

    return `Hoy: ${this.fichajesHoy()} fichajes`;
  });

  readonly formatTiempo = (fecha: string): string => {
    if (!fecha) {
      return '';
    }

    try {
      return formatDistanceToNow(new Date(fecha), {
        addSuffix: true,
        locale: es,
      });
    } catch {
      return '';
    }
  };

  readonly formatFechaDia = (fechaString: string | null): string => {
    if (!fechaString) {
      return '';
    }

    try {
      return format(new Date(fechaString), "d 'de' MMMM", { locale: es });
    } catch {
      return '';
    }
  };

  ngOnInit(): void {
    void this.cargarDashboard();
  }

  filtrarDepartamento(departamento: string | null): void {
    this.departamentoSeleccionado.set(departamento);
  }

  navigateToIncidencias(): void {
    void this.router.navigate(['/admin/incidencias']);
  }

  semanaAnterior(): void {
    this.semanaSeleccionada.set(this.semanaSeleccionada() + 1);
    this.diaSeleccionado.set(null);
    this.fichajesDiaSeleccionado.set([]);
    this.textoSemanaSeleccionada.set(`Hace ${this.semanaSeleccionada()} semana(s)`);
    void this.cargarGraficaSemanal();
  }

  semanaSiguiente(): void {
    if (this.semanaSeleccionada() <= 0) {
      return;
    }

    this.semanaSeleccionada.set(this.semanaSeleccionada() - 1);
    this.diaSeleccionado.set(null);
    this.fichajesDiaSeleccionado.set([]);
    this.textoSemanaSeleccionada.set(
      this.semanaSeleccionada() === 0 ? 'Esta semana' : 'Semana pasada',
    );
    void this.cargarGraficaSemanal();
  }

  abrirSelector(): void {
    this.datePicker?.nativeElement.showPicker?.();
    this.datePicker?.nativeElement.click();
  }

  onDateChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    if (!value) {
      return;
    }

    const nuevaFecha = new Date(`${value}T00:00:00`);
    if (Number.isNaN(nuevaFecha.getTime())) {
      return;
    }

    const inicioEstaSemana = this.getInicioSemana();
    const inicioSemanaSeleccionada = this.getInicioSemana(nuevaFecha);
    const diferenciaMs = inicioEstaSemana.getTime() - inicioSemanaSeleccionada.getTime();
    const diferenciaSemanas = Math.round(diferenciaMs / (7 * 24 * 60 * 60 * 1000));

    this.semanaSeleccionada.set(diferenciaSemanas);
    this.diaSeleccionado.set(null);
    this.fichajesDiaSeleccionado.set([]);

    void this.cargarGraficaSemanal();
  }

  seleccionarDia(dia: WeekChartDay): void {
    this.diaSeleccionado.set(dia.date);
    this.fichajesSemana.update((dias) =>
      dias.map((item) => ({
        ...item,
        seleccionado: item.date === dia.date,
      })),
    );
    void this.cargarFichajesDia(dia.workDate);
  }

  handleAccionClick(accion: QuickAction): void {
    if (accion.texto === 'Exportar') {
      this.dialogoExportar.set(true);
      return;
    }

    if (accion.texto === 'Añadir') {
      void this.router.navigate(['/admin/trabajadores'], { queryParams: { open: 'true' } });
      return;
    }

    if (accion.texto === 'Horarios') {
      void this.router.navigate(['/admin/trabajadores']);
      return;
    }

    if (accion.texto === 'Alertas') {
      void this.router.navigate(['/admin/incidencias']);
      return;
    }

    if (accion.texto === 'Auditor') {
      this.dialogoInspector.set(true);
      return;
    }
  }

  async generarInformeLegal(): Promise<void> {
    if (this.cargandoExportacion()) {
      return;
    }

    this.cargandoExportacion.set(true);

    try {
      const { inicio, fin } = this.getRangoExportacionFechas();
      const formato = this.formatoExportacion();

      const blob = await firstValueFrom(
        formato === 'pdf'
          ? this.adminHomeService.exportReportPdf(inicio, fin)
          : this.adminHomeService.exportarInformeEmpresaExcel(inicio, fin),
      );

      const extension = formato === 'pdf' ? 'pdf' : 'xlsx';
      const fileName = `informe_legal_${inicio}_a_${fin}.${extension}`;
      this.descargarBlob(blob, fileName);
      this.dialogoExportar.set(false);
    } catch (error) {
      console.error('Error crítico exportando:', error);
      alert('Error al generar el informe legal de inspección.');
    } finally {
      this.cargandoExportacion.set(false);
    }
  }

  crearInspector(data: InspectorRequestDto): void {
    if (this.cargandoInspector()) return;

    this.cargandoInspector.set(true);

    this.adminHomeService
      .registerInspector(data)
      .pipe(
        take(1),
        finalize(() => this.cargandoInspector.set(false)),
      )
      .subscribe({
        next: (response) => {
          alert(response.message);
          this.dialogoInspector.set(false);
        },
        error: (err) => {
          console.error('Error al procesar el inspector:', err);
          alert('Hubo un error al crear o notificar al inspector.');
        },
      });
  }

  private async cargarDashboard(): Promise<void> {
    this.loading.set(true);

    try {
      await this.cargarTotalTrabajadores();
      await this.cargarAlertas();
      await this.cargarFichajesHoy();
      await this.cargarTrabajadoresActivos();
      await this.cargarGraficaSemanal();
      await this.cargarFichajesHoyPorDefecto();
      await this.cargarDepartamentos();
    } catch (error) {
      console.error('Error inesperado al cargar dashboard:', error);
    } finally {
      this.loading.set(false);
    }
  }

  private async cargarTotalTrabajadores(): Promise<void> {
    const total = await firstValueFrom(this.adminHomeService.obtenerTotalTrabajadores());
    this.totalTrabajadores.set(total ?? 0);
  }

  private async cargarAlertas(): Promise<void> {
    const incidencias = await firstValueFrom(
      this.adminHomeService.obtenerIncidenciasAdmin('PENDING', 0, 5),
    );
    this.alertasRecientes.set(incidencias.map((item) => this.mapIncidencia(item)));
    this.alertasPendientes.set(this.adminHomeService.adminIncidenciasTotalSignal());
  }

  private async cargarFichajesHoy(): Promise<void> {
    const fichajesHoy = await firstValueFrom(this.adminHomeService.obtenerNumFichajesHoy());
    this.fichajesHoy.set(fichajesHoy ?? 0);

    const horasHoy = await firstValueFrom(this.adminHomeService.getTotalHoursToday());
    const minutosTotales = horasHoy?.minutosTotales ?? 0;
    this.horasTotales.set(Math.round(minutosTotales / 60));
  }

  private async cargarTrabajadoresActivos(): Promise<void> {
    const trabajadores = await firstValueFrom(this.adminHomeService.getActiveWorkers());
    const mapped = trabajadores.map((worker) => this.mapActiveWorker(worker));
    this.trabajadoresList.set(mapped);
    this.trabajadoresActivos.set(mapped.length);
  }

  private async cargarDepartamentos(): Promise<void> {
    const departamentos = await firstValueFrom(this.adminHomeService.obtenerDepartamentos());
    this.departamentos.set(this.mapDepartamentos(departamentos));
  }

  private async cargarGraficaSemanal(): Promise<void> {
    const inicioSemana = this.getInicioSemanaSeleccionada();
    const finSemana = new Date(inicioSemana);
    finSemana.setDate(inicioSemana.getDate() + 6);

    const weeklyData = await firstValueFrom(
      this.adminHomeService.obtenerWeeklyChart(
        this.toIsoDate(inicioSemana),
        this.toIsoDate(finSemana),
      ),
    );

    this.fichajesSemana.set(this.mapSemana(weeklyData, inicioSemana));
  }

  private async cargarFichajesDia(fechaString: string): Promise<void> {
    const fecha = new Date(fechaString);
    if (Number.isNaN(fecha.getTime())) {
      this.fichajesDiaSeleccionado.set([]);
      return;
    }

    const workDate = this.toIsoDate(fecha);
    const fichajesDia = await firstValueFrom(this.adminHomeService.getTimeEntriesByDate(workDate));
    this.fichajesDiaSeleccionado.set(fichajesDia.map((item) => this.mapFichajeDia(item)));
  }

  private async cargarFichajesHoyPorDefecto(): Promise<void> {
    const hoy = new Date();
    const workDate = this.toIsoDate(hoy);

    const fichajesHoy = await firstValueFrom(this.adminHomeService.getTimeEntriesByDate(workDate));
    if (!fichajesHoy.length) {
      this.fichajesDiaSeleccionado.set([]);
      return;
    }

    const diaHoy = this.fichajesSemana().find((item) => item.workDate === workDate);
    if (!diaHoy) {
      this.fichajesDiaSeleccionado.set([]);
      return;
    }

    this.diaSeleccionado.set(diaHoy.date);
    this.fichajesSemana.update((dias) =>
      dias.map((item) => ({
        ...item,
        seleccionado: item.date === diaHoy.date,
      })),
    );
    this.fichajesDiaSeleccionado.set(fichajesHoy.map((item) => this.mapFichajeDia(item)));
  }

  private mapIncidencia(incidencia: AdminIncidenceResponseDto): DashboardAlert {
    return {
      id: incidencia.id,
      type: incidencia.incidenceType || 'Incidencia',
      comment: incidencia.comment || '',
      description: incidencia.comment || '',
      createdAt: incidencia.createdAt || '',
      workerName: incidencia.employeeName || 'Sin nombre',
      jobTitle: null,
    };
  }

  private mapDepartamentos(departamentos: DepartmentStatDto[]): DepartmentSummary[] {
    return [...departamentos]
      .map((departamento) => ({
        nombre: departamento.departamento,
        activos: departamento.trabajadoresActivos,
        total: departamento.totalTrabajadores,
        porcentaje:
          departamento.totalTrabajadores > 0
            ? Math.round((departamento.trabajadoresActivos / departamento.totalTrabajadores) * 100)
            : 0,
        horas: null,
        activo: departamento.trabajadoresActivos > 0,
      }))
      .sort((a, b) => b.total - a.total)
      .slice(0, 5);
  }

  private mapActiveWorker(worker: ActiveWorkerDto): ActiveWorkerCard {
    const nombre = worker.nombreCompleto || 'Sin nombre';
    const puntualidad = Number(worker.puntualidad ?? 0);
    const retraso = puntualidad > 10;
    const fechaEntrada = this.parseFecha(worker.horaFichaje);

    return {
      id: worker.trabajadorId,
      nombre,
      departamento: worker.puestoTrabajo || 'Empleado',
      avatar: worker.urlAvatar || null,
      iniciales: this.generarIniciales(nombre),
      estado: 'Activo',
      entrada: fechaEntrada ? format(fechaEntrada, 'HH:mm') : '--:--',
      retraso,
      minutosRetraso: retraso ? puntualidad : 0,
      fechaIso: fechaEntrada ? this.toIsoDate(fechaEntrada) : null,
    };
  }

  private mapFichajeDia(item: AdminTimeEntryByDateResponseDto): SelectedDayEntry {
    const nombre = item.nombreTrabajador || 'Sin nombre';
    const entradaDate = this.parseFecha(item.entrada);
    const salidaDate = this.parseFecha(item.salida);

    return {
      id: item.id,
      nombre,
      departamento: item.puestoTrabajo || 'Empleado',
      avatar: item.avatarUrl,
      iniciales: this.generarIniciales(nombre),
      entrada: entradaDate ? format(entradaDate, 'HH:mm') : '--:--',
      salida: salidaDate ? format(salidaDate, 'HH:mm') : null,
      duracion: this.formatearDuracion(item.minutosTrabajados),
    };
  }

  private formatearDuracion(minutos: number | null): string | null {
    if (minutos == null || minutos < 0) {
      return null;
    }

    const horas = Math.floor(minutos / 60);
    const mins = minutos % 60;

    if (horas === 0) {
      return `${mins}m`;
    }

    if (mins === 0) {
      return `${horas}h`;
    }

    return `${horas}h ${mins}m`;
  }

  private mapSemana(weeklyData: DailyTimeEntryCountDto[], inicioSemana: Date): WeekChartDay[] {
    const conteo = new Map<string, number>();
    for (const item of weeklyData) {
      conteo.set(item.fecha, Number(item.numFichajes ?? 0));
    }

    const diasSemana = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
    const hoy = new Date();
    const seleccionado = this.diaSeleccionado();

    const base = Array.from({ length: 7 }, (_, index) => {
      const fecha = new Date(inicioSemana);
      fecha.setDate(inicioSemana.getDate() + index);

      const workDate = this.toIsoDate(fecha);
      const count = conteo.get(workDate) ?? 0;
      const date = fecha.toDateString();
      const activo = this.semanaSeleccionada() === 0 && date === hoy.toDateString();

      return {
        dia: diasSemana[index],
        valor: count,
        porcentaje: 0,
        activo,
        date,
        workDate,
        seleccionado: activo || seleccionado === date,
      };
    });

    const max = Math.max(...base.map((item) => item.valor), 1);

    return base.map((item) => ({
      ...item,
      porcentaje: Math.round((item.valor / max) * 100),
    }));
  }

  private getInicioSemanaSeleccionada(): Date {
    const inicioEstaSemana = this.getInicioSemana();
    const inicioSemana = new Date(inicioEstaSemana);
    inicioSemana.setDate(inicioEstaSemana.getDate() - this.semanaSeleccionada() * 7);
    return inicioSemana;
  }

  private getInicioSemana(fechaBase: Date = new Date()): Date {
    const fecha = new Date(fechaBase);
    const diaSemana = fecha.getDay();
    const diasDesdeLunes = diaSemana === 0 ? 6 : diaSemana - 1;

    fecha.setDate(fecha.getDate() - diasDesdeLunes);
    fecha.setHours(0, 0, 0, 0);
    return fecha;
  }

  private getRangoExportacionFechas(): { inicio: string; fin: string } {
    const hoy = new Date();
    const rango = this.rangoExportacion();

    if (rango === 'hoy') {
      const fecha = this.toIsoDate(hoy);
      return { inicio: fecha, fin: fecha };
    }

    if (rango === 'mes') {
      const inicioMes = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
      return { inicio: this.toIsoDate(inicioMes), fin: this.toIsoDate(hoy) };
    }

    if (rango === 'anio') {
      const inicioAnio = new Date(hoy.getFullYear(), 0, 1);
      return { inicio: this.toIsoDate(inicioAnio), fin: this.toIsoDate(hoy) };
    }

    const inicioHistorico = new Date(hoy.getFullYear() - 4, 0, 1);
    return { inicio: this.toIsoDate(inicioHistorico), fin: this.toIsoDate(hoy) };
  }

  private descargarBlob(blob: Blob, fileName: string): void {
    const url = window.URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = fileName;
    anchor.click();
    window.URL.revokeObjectURL(url);
  }

  private parseFecha(value: string | Date | null | undefined): Date | null {
    if (!value) {
      return null;
    }

    const fecha = new Date(value);
    if (Number.isNaN(fecha.getTime())) {
      return null;
    }

    return fecha;
  }

  private generarIniciales(nombre: string): string {
    if (!nombre) {
      return 'SN';
    }

    const palabras = nombre.trim().split(/\s+/);
    if (palabras.length >= 2) {
      return `${palabras[0][0]}${palabras[1][0]}`.toUpperCase();
    }

    return nombre.substring(0, 2).toUpperCase();
  }

  private toIsoDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
