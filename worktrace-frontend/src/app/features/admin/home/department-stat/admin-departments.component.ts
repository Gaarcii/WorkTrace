import {
  Component,
  ChangeDetectionStrategy,
  OnInit,
  inject,
  signal,
  computed,
} from '@angular/core';
import { finalize, take } from 'rxjs/operators';
import { AdminHomeService } from '../../../../shared/services/admin/admin-home.service';
import { DepartmentStatDto } from '../../../../shared/models/profile.model';

export interface DepartmentViewData {
  nombre: string;
  activos: number;
  total: number;
  porcentaje: number;
  activo: boolean;
}

@Component({
  selector: 'app-admin-departments',
  templateUrl: './admin-departments.component.html',
  styleUrls: ['./admin-departments.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminDepartmentsComponent implements OnInit {
  private readonly adminHomeService = inject(AdminHomeService);

  readonly loading = signal<boolean>(true);
  readonly departamentoSeleccionado = signal<string | null>(null);

  readonly departamentos = computed<DepartmentViewData[]>(() => {
    const rawData = this.adminHomeService.departmentStatsSignal();

    if (!rawData?.length) return [];

    return rawData.map((departamento) => ({
      nombre: departamento.departamento,
      activos: departamento.trabajadoresActivos,
      total: departamento.totalTrabajadores,
      porcentaje:
        departamento.totalTrabajadores > 0
          ? (departamento.trabajadoresActivos / departamento.totalTrabajadores) * 100
          : 0,
      activo: departamento.trabajadoresActivos > 0,
    }));
  });

  readonly departamentosFiltrados = computed<DepartmentViewData[]>(() => {
    const seleccion = this.departamentoSeleccionado();
    const todos = this.departamentos();

    if (!seleccion) {
      return todos;
    }

    return todos.filter((dept) => dept.nombre === seleccion);
  });

  ngOnInit(): void {
    this.cargarDepartamentos();
  }

  private cargarDepartamentos(): void {
    this.loading.set(true);

    this.adminHomeService
      .obtenerDepartamentos()
      .pipe(
        take(1),
        finalize(() => this.loading.set(false)),
      )
      .subscribe();
  }

  onFilterChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    const value = selectElement.value;

    this.departamentoSeleccionado.set(value === 'ALL' ? null : value);
  }
}
