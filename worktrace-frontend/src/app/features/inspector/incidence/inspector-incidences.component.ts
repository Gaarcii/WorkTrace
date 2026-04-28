import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  inject,
  signal,
  DestroyRef,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { combineLatest } from 'rxjs';
import { debounceTime, distinctUntilChanged, finalize, startWith, take } from 'rxjs/operators';

import { InspectorWorkerService } from '../../../shared/services/inspector/inspector-worker.service';
import { InspectorIncidenceDto } from '../../../shared/models/inspector.model';
import { WorkerIncidenceTypesService } from '../../../shared/services/worker/worker-tipos-incidencias.service';

import {
  IncidencesFilterComponent,
  StatusOption,
} from './incidences-filter/incidences-filter.component';
import { IncidencesListComponent } from './incidences-list/incidences-list.component';

@Component({
  selector: 'app-inspector-incidences',
  imports: [IncidencesFilterComponent, IncidencesListComponent],
  templateUrl: './inspector-incidences.component.html',
  styleUrls: ['./inspector-incidences.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InspectorIncidencesComponent implements OnInit {
  private readonly inspectorWorkerService = inject(InspectorWorkerService);
  private readonly incidenceTypesService = inject(WorkerIncidenceTypesService);
  private readonly destroyRef = inject(DestroyRef);

  readonly statusControl = new FormControl<string>('');
  readonly typeControl = new FormControl<string>('');
  readonly searchControl = new FormControl<string>('', { nonNullable: true });

  private readonly _loading = signal<boolean>(true);
  private readonly _incidents = signal<InspectorIncidenceDto[]>([]);

  readonly loading = this._loading.asReadonly();
  readonly incidents = this._incidents.asReadonly();
  readonly tipos = this.incidenceTypesService.tiposSignal.asReadonly();

  readonly statusOptions: StatusOption[] = [
    { text: 'Pendiente', value: 'PENDING' },
    { text: 'Resuelta', value: 'RESOLVED' },
    { text: 'Rechazada', value: 'REJECTED' },
  ];

  ngOnInit(): void {
    this.loadTipos();
    this.loadIncidents();

    combineLatest([
      this.statusControl.valueChanges.pipe(startWith(this.statusControl.value)),
      this.typeControl.valueChanges.pipe(startWith(this.typeControl.value)),
      this.searchControl.valueChanges.pipe(startWith(this.searchControl.value)),
    ])
      .pipe(
        debounceTime(300),
        distinctUntilChanged((prev, curr) => JSON.stringify(prev) === JSON.stringify(curr)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(([estado, tipo, busqueda]) => {
        this.loadIncidents(estado ?? undefined, tipo ?? undefined, busqueda ?? undefined);
      });
  }

  private loadIncidents(estado?: string, tipoIncidenciaId?: string, busqueda?: string): void {
    this._loading.set(true);

    this.inspectorWorkerService
      .getIncidencias(0, 100, estado, tipoIncidenciaId, busqueda)
      .pipe(
        take(1),
        finalize(() => this._loading.set(false)),
      )
      .subscribe({
        next: (response) => {
          this._incidents.set(response.content || []);
        },
        error: (err: unknown) => console.error('Error al cargar incidencias:', err),
      });
  }

  private loadTipos(): void {
    this.incidenceTypesService
      .obtenerTipos()
      .pipe(take(1))
      .subscribe({
        error: (err: unknown) => console.error('Error al cargar tipos de incidencia:', err),
      });
  }
}
