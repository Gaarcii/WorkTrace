import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  inject,
  signal,
  DestroyRef,
  ViewChild,
  TemplateRef,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged, finalize, take } from 'rxjs/operators';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { InspectorWorkerService } from '../../../shared/services/inspector/inspector-worker.service';
import { EmployeeDto, EmployeeDetailDto } from '../../../shared/models/inspector.model';
import { EmployeeSearchComponent } from './employee-search/employee-search.component';
import { EmployeeListComponent } from './employee-list/employee-list.component';
import { EmployeeDetailComponent } from './employee-detail/employee-detail.component';

@Component({
  selector: 'app-inspector-employees',
  imports: [
    MatDialogModule,
    EmployeeSearchComponent,
    EmployeeListComponent,
    EmployeeDetailComponent,
  ],
  templateUrl: './inspector-employees.component.html',
  styleUrls: ['./inspector-employees.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InspectorEmployeesComponent implements OnInit {
  private readonly inspectorWorkerService = inject(InspectorWorkerService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialog = inject(MatDialog);

  @ViewChild('employeeDialogTemplate') employeeDialogTemplate!: TemplateRef<unknown>;

  readonly searchControl = new FormControl<string>('', { nonNullable: true });

  private readonly _loading = signal<boolean>(true);
  private readonly _loadingSchedule = signal<boolean>(false);
  private readonly _employees = signal<EmployeeDto[]>([]);
  private readonly _selectedEmployeeBase = signal<EmployeeDto | null>(null);
  private readonly _selectedEmployeeDetail = signal<EmployeeDetailDto | null>(null);

  readonly loading = this._loading.asReadonly();
  readonly loadingSchedule = this._loadingSchedule.asReadonly();
  readonly employees = this._employees.asReadonly();
  readonly selectedEmployeeBase = this._selectedEmployeeBase.asReadonly();
  readonly selectedEmployeeDetail = this._selectedEmployeeDetail.asReadonly();

  ngOnInit(): void {
    this.loadEmployees();

    this.searchControl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((searchTerm: string) => {
        this.loadEmployees(searchTerm);
      });
  }

  private loadEmployees(search = ''): void {
    this._loading.set(true);

    this.inspectorWorkerService
      .getEmpleados(0, 100, search)
      .pipe(
        take(1),
        finalize(() => this._loading.set(false)),
      )
      .subscribe({
        next: (response) => {
          this._employees.set(response.content || []);
        },
      });
  }

  openEmployeeDialog(employee: EmployeeDto): void {
    this._selectedEmployeeBase.set(employee);
    this.loadEmployeeSchedule(employee.id);

    this.dialog.open(this.employeeDialogTemplate, {
      width: '800px',
      maxWidth: '95vw',
      maxHeight: '95vh',
      panelClass: 'custom-dialog-no-padding',
      autoFocus: false,
    });
  }

  closeDialog(): void {
    this.dialog.closeAll();
    this._selectedEmployeeBase.set(null);
    this._selectedEmployeeDetail.set(null);
  }

  private loadEmployeeSchedule(userId: string): void {
    this._loadingSchedule.set(true);
    this._selectedEmployeeDetail.set(null);

    this.inspectorWorkerService
      .getEmpleadoDetalle(userId)
      .pipe(
        take(1),
        finalize(() => this._loadingSchedule.set(false)),
      )
      .subscribe({
        next: (detail: EmployeeDetailDto) => {
          this._selectedEmployeeDetail.set(detail);
        },
      });
  }
}
