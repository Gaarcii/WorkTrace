import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  DestroyRef,
  ViewChild,
  TemplateRef,
} from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged, finalize, take } from 'rxjs/operators';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

import { InspectorWorkerService } from '../../../shared/services/inspector/inspector-worker.service';
import { InspectorAuditDto, InspectorAuditDetailDto } from '../../../shared/models/inspector.model';
import { AuditDetailComponent } from './audit-detail/audit-detail.component';
import { AuditFilterComponent, ActionOption } from './audit-filter/audit-filter.component';
import { AuditTableComponent } from './audit-table/audit-table.component';


@Component({
  selector: 'app-inspector-audit',
  imports: [
    MatDialogModule,
    MatIconModule,
    AuditFilterComponent,
    AuditTableComponent,
    AuditDetailComponent,
  ],
  templateUrl: './inspector-audit.component.html',
  styleUrls: ['./inspector-audit.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InspectorAuditComponent implements OnInit {
  private readonly inspectorWorkerService = inject(InspectorWorkerService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialog = inject(MatDialog);

  @ViewChild('detailsDialogTemplate') detailsDialogTemplate!: TemplateRef<unknown>;

  readonly filterForm = new FormGroup({
    action: new FormControl<string>('', { nonNullable: true }),
    startDate: new FormControl<string>('', { nonNullable: true }),
    endDate: new FormControl<string>('', { nonNullable: true }),
  });

  private readonly _loading = signal<boolean>(true);
  private readonly _loadingDetails = signal<boolean>(false);
  private readonly _records = signal<InspectorAuditDto[]>([]);
  private readonly _selectedRecordBase = signal<InspectorAuditDto | null>(null);
  private readonly _selectedRecordDetail = signal<InspectorAuditDetailDto | null>(null);

  readonly loading = this._loading.asReadonly();
  readonly loadingDetails = this._loadingDetails.asReadonly();
  readonly records = this._records.asReadonly();
  readonly selectedRecordBase = this._selectedRecordBase.asReadonly();
  readonly selectedRecordDetail = this._selectedRecordDetail.asReadonly();

  readonly actionOptions: ActionOption[] = [
    { title: 'Todas', value: '' },
    { title: 'Modificación Manual', value: 'ADMIN_ADJUST' },
    { title: 'Borrado de Registro', value: 'SOFT_DELETE' },
  ];

  readonly filteredRecords = computed(() => {
    return this._records();
  });

  readonly parsedOldData = computed(() => {
    const detail = this._selectedRecordDetail();
    if (!detail || !detail.datosAntesModificacion) return null;
    try {
      return JSON.parse(detail.datosAntesModificacion) as Record<string, unknown>;
    } catch (e) {
      console.error('Error parseando datosAntesModificacion:', e);
      return null;
    }
  });

  ngOnInit(): void {
    this.loadAuditData();

    this.filterForm.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged((prev, curr) => JSON.stringify(prev) === JSON.stringify(curr)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        this.loadAuditData();
      });
  }

  loadAuditData(): void {
    this._loading.set(true);
    const { action, startDate, endDate } = this.filterForm.getRawValue();
    this.inspectorWorkerService
      .getAuditorias(0, 100, action || undefined, startDate || undefined, endDate || undefined)
      .pipe(
        take(1),
        finalize(() => this._loading.set(false)),
      )
      .subscribe({
        next: (response) => {
          this._records.set(response.content || []);
        },
        error: (err: unknown) => console.error('Error cargando auditoría:', err),
      });
  }

  openDetails(record: InspectorAuditDto): void {
    this._selectedRecordBase.set(record);
    this._loadingDetails.set(true);
    this._selectedRecordDetail.set(null);

    this.dialog.open(this.detailsDialogTemplate, {
      width: '900px',
      maxWidth: '95vw',
      maxHeight: '90vh',
      panelClass: 'custom-dialog-no-padding',
      autoFocus: false,
    });

    this.inspectorWorkerService
      .getAuditoriaDetalle(record.id)
      .pipe(
        take(1),
        finalize(() => this._loadingDetails.set(false)),
      )
      .subscribe({
        next: (detail) => this._selectedRecordDetail.set(detail),
        error: (err: unknown) => console.error('Error cargando detalles:', err),
      });
  }

  closeDetails(): void {
    this.dialog.closeAll();
    this._selectedRecordBase.set(null);
    this._selectedRecordDetail.set(null);
  }

  clearFilters(): void {
    this.filterForm.reset();
  }
}
