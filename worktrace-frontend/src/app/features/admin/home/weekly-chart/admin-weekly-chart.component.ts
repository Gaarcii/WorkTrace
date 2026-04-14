import { Component, ChangeDetectionStrategy, OnInit, inject, signal, computed, output } from '@angular/core';
import { AdminHomeService } from '../../../../shared/services/admin/admin-home.service';
import { take, finalize } from 'rxjs/operators';

export interface ChartBarData {
  dateStr: string;
  diaName: string;
  valor: number;
  porcentaje: number;
  seleccionado: boolean;
}

@Component({
  selector: 'app-admin-weekly-chart',
  templateUrl: './admin-weekly-chart.component.html',
  styleUrls: ['./admin-weekly-chart.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminWeeklyChartComponent implements OnInit {
  private readonly adminHomeService = inject(AdminHomeService);

  readonly openCalendar = output<void>();
  readonly daySelected = output<ChartBarData>();

  readonly loading = signal<boolean>(false);
  readonly referenceDate = signal<Date>(new Date());
  readonly selectedDateStr = signal<string | null>(null);

  readonly weekStart = computed<Date>(() => this.getStartOfWeek(this.referenceDate()));
  readonly weekEnd = computed<Date>(() => this.getEndOfWeek(this.referenceDate()));

  readonly rangoSemana = computed<string>(() => {
    return `${this.formatShortDate(this.weekStart())} - ${this.formatShortDate(this.weekEnd())}`;
  });

  readonly fichajesSemana = computed<ChartBarData[]>(() => {
    const rawData = this.adminHomeService.weeklyChartSignal();
    
    if (!rawData || rawData.length === 0) {
      return this.generateEmptyWeek();
    }

    const maxFichajes = Math.max(...rawData.map(d => d.numFichajes), 1);
    const selected = this.selectedDateStr();

    return rawData.map(d => {
      const dateObj = new Date(d.fecha);
      return {
        dateStr: d.fecha,
        diaName: this.getDayName(dateObj),
        valor: d.numFichajes,
        porcentaje: (d.numFichajes / maxFichajes) * 100,
        seleccionado: d.fecha === selected
      };
    });
  });

  ngOnInit(): void {
    this.loadWeeklyData();
  }

  private loadWeeklyData(): void {
    this.loading.set(true);
    
    this.adminHomeService.obtenerWeeklyChart(this.weekStart(), this.weekEnd())
      .pipe(
        take(1),
        finalize(() => this.loading.set(false))
      )
      .subscribe();
  }

  onPrevWeek(): void {
    const newDate = new Date(this.referenceDate());
    newDate.setDate(newDate.getDate() - 7);
    this.referenceDate.set(newDate);
    this.loadWeeklyData();
  }

  onNextWeek(): void {
    const newDate = new Date(this.referenceDate());
    newDate.setDate(newDate.getDate() + 7);
    this.referenceDate.set(newDate);
    this.loadWeeklyData();
  }

  onOpenCalendar(): void {
    this.openCalendar.emit();
  }

  onSelectDay(dia: ChartBarData): void {
    this.selectedDateStr.set(dia.dateStr);
    this.daySelected.emit(dia);
  }


  private getStartOfWeek(date: Date): Date {
    const d = new Date(date);
    const day = d.getDay() || 7; 
    d.setDate(d.getDate() - day + 1);
    d.setHours(0, 0, 0, 0);
    return d;
  }

  private getEndOfWeek(date: Date): Date {
    const d = this.getStartOfWeek(date);
    d.setDate(d.getDate() + 6);
    d.setHours(23, 59, 59, 999);
    return d;
  }

  private formatShortDate(date: Date): string {
    const d = String(date.getDate()).padStart(2, '0');
    const m = String(date.getMonth() + 1).padStart(2, '0');
    return `${d}/${m}`;
  }

  private getDayName(date: Date): string {
    const days = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];
    return days[date.getDay()];
  }

  private generateEmptyWeek(): ChartBarData[] {
    const start = this.weekStart();
    const emptyWeek: ChartBarData[] = [];
    for (let i = 0; i < 7; i++) {
      const current = new Date(start);
      current.setDate(current.getDate() + i);
      const dateStr = current.toISOString().split('T')[0];
      emptyWeek.push({
        dateStr,
        diaName: this.getDayName(current),
        valor: 0,
        porcentaje: 0,
        seleccionado: dateStr === this.selectedDateStr()
      });
    }
    return emptyWeek;
  }
}