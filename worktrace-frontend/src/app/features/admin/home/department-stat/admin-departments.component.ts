import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { DepartmentSummary } from '../admin-home.types';

@Component({
  selector: 'app-admin-departments',
  templateUrl: './admin-departments.component.html',
  styleUrls: ['./admin-departments.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminDepartmentsComponent {
  readonly departamentoSeleccionado = input<string | null>(null);
  readonly departamentos = input<DepartmentSummary[]>([]);

  readonly filter = output<string | null>();

  readonly departamentosFiltrados = computed<DepartmentSummary[]>(() => {
    const seleccionado = this.departamentoSeleccionado();
    if (!seleccionado) {
      return this.departamentos();
    }

    return this.departamentos().filter((dept) => dept.nombre === seleccionado);
  });

  onFilterChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.filter.emit(value === 'ALL' ? null : value);
  }
}
