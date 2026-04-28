import { Component, ChangeDetectionStrategy, OnInit, inject, signal } from '@angular/core';
import { take, finalize } from 'rxjs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { InspectorHomeService } from '../../../shared/services/inspector/inspector-home.service';
import { InspectorHomeResponseDto } from '../../../shared/models/inspector.model';
import { InspectorHeaderComponent } from './Inspector-header/inspector-header.component';
import { InspectorAlertComponent } from './Inspector-alert/inspector-alert.component';
import { InspectorStatsGridComponent } from './Inspector-stats-grid/inspector-stats-grid.component';
import { InspectorInfoGridComponent } from './Inspector-info-grid/inspector-info-grid.component';

@Component({
  selector: 'app-inspector-home',
  standalone: true,
  imports: [
    MatProgressSpinnerModule,
    InspectorHeaderComponent,
    InspectorAlertComponent,
    InspectorStatsGridComponent,
    InspectorInfoGridComponent,
  ],
  templateUrl: './inspector-home.component.html',
  styleUrls: ['./inspector-home.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InspectorHomeComponent implements OnInit {
  private readonly inspectorHomeService = inject(InspectorHomeService);

  private readonly _loading = signal<boolean>(true);
  private readonly _error = signal<string | null>(null);
  private readonly _stats = signal<InspectorHomeResponseDto | null>(null);

  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly stats = this._stats.asReadonly();

  ngOnInit(): void {
    this.loadDashboardStats();
  }

  clearError(): void {
    this._error.set(null);
  }

  retry(): void {
    this.loadDashboardStats();
  }

  private loadDashboardStats(): void {
    this._loading.set(true);
    this._error.set(null);

    this.inspectorHomeService
      .getHome()
      .pipe(
        take(1),
        finalize(() => this._loading.set(false)),
      )
      .subscribe({
        next: (data: InspectorHomeResponseDto) => {
          this._stats.set(data);
        },
        error: (err: unknown) => {
          const errorMessage =
            err instanceof Error ? err.message : 'Error inesperado al cargar las estadísticas';
          this._error.set(errorMessage);
        },
      });
  }
}
