import { ChangeDetectionStrategy, Component, effect, input, output } from '@angular/core';
import { EmployeeListItem, EmployeeTableHeader } from '../../admin-trabajadores.types';

@Component({
  selector: 'app-employees-table',
  templateUrl: './employee-table.component.html',
  styleUrl: './employee-table.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmployeesTableComponent {
  readonly headers = input.required<EmployeeTableHeader[]>();
  readonly items = input.required<EmployeeListItem[]>();
  readonly loading = input.required<boolean>();
  readonly formatDate = input.required<(date: string) => string>();

  readonly rowClick = output<EmployeeListItem>();

  readonly itemsPerPage = 10;
  currentPage = 1;

  constructor() {
    effect(() => {
      const maxPage = Math.max(1, Math.ceil(this.items().length / this.itemsPerPage));
      if (this.currentPage > maxPage) {
        this.currentPage = maxPage;
      }
    });
  }

  get pagedItems(): EmployeeListItem[] {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    return this.items().slice(start, start + this.itemsPerPage);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.items().length / this.itemsPerPage));
  }

  get pageStart(): number {
    if (!this.items().length) {
      return 0;
    }
    return (this.currentPage - 1) * this.itemsPerPage + 1;
  }

  get pageEnd(): number {
    return Math.min(this.currentPage * this.itemsPerPage, this.items().length);
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage -= 1;
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage += 1;
    }
  }

  getCellValue(item: EmployeeListItem, key: string): string {
    if (key === 'jobTitle') {
      return item.jobTitle || '-';
    }

    if (key === 'weeklyHours') {
      return item.weeklyHours ? `${item.weeklyHours}h` : '-';
    }

    if (key === 'isFirstLogin') {
      return item.isFirstLogin ? 'Pendiente' : 'Activo';
    }

    if (key === 'createdAt') {
      return this.formatDate()(item.createdAt);
    }

    const value = item[key as keyof EmployeeListItem];
    return value == null ? '-' : String(value);
  }
}
