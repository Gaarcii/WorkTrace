import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  inject,
  signal,
  DestroyRef,
} from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged, finalize, startWith, take } from 'rxjs/operators';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { InspectorDailyClosureDto } from '../../../shared/models/inspector.model';
import { RegistrosFilterComponent } from './registros-filter/registros-filter.component';
import { RegistrosTableComponent } from './registros-table/registros-table.component';
import { InspectorRegistroDiarioService } from '../../../shared/services/inspector/inspector-registro-diario.service';

@Component({
  selector: 'app-inspector-registros',
  imports: [MatIconModule, MatButtonModule, RegistrosFilterComponent, RegistrosTableComponent],
  templateUrl: './inspector-registros.component.html',
  styleUrls: ['./inspector-registros.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InspectorRegistrosComponent implements OnInit {
  private readonly inspectorWorkerService = inject(InspectorRegistroDiarioService);
  private readonly destroyRef = inject(DestroyRef);

  readonly filterForm = new FormGroup({
    startDate: new FormControl<string | null>(null),
    endDate: new FormControl<string | null>(null),
  });

  private readonly _loading = signal<boolean>(true);
  private readonly _error = signal<string | null>(null);
  private readonly _registros = signal<InspectorDailyClosureDto[]>([]);

  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly registros = this._registros.asReadonly();

  ngOnInit(): void {
    this.filterForm.valueChanges
      .pipe(
        startWith(this.filterForm.value),
        debounceTime(400),
        distinctUntilChanged((prev, curr) => JSON.stringify(prev) === JSON.stringify(curr)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((values) => {
        this.loadRegistros(values.startDate, values.endDate);
      });
  }

  private loadRegistros(startDate?: string | null, endDate?: string | null): void {
    this._loading.set(true);
    this._error.set(null);

    this.inspectorWorkerService
      .getRegistrosDiarios(0, 100, startDate ?? undefined, endDate ?? undefined)
      .pipe(
        take(1),
        finalize(() => this._loading.set(false)),
      )
      .subscribe({
        next: (response) => {
          this._registros.set(response.content || []);
        },
        error: (err: unknown) => {
          this._error.set('Error al cargar los registros diarios de la base de datos.');
          this._registros.set([]);
        },
      });
  }

  clearError(): void {
    this._error.set(null);
  }

  clearFilters(): void {
    this.filterForm.reset();
  }

  async copyToClipboard(text: string): Promise<void> {
    if (!text) return;
    try {
      await navigator.clipboard.writeText(text);
    } catch (err) {
    }
  }
}
