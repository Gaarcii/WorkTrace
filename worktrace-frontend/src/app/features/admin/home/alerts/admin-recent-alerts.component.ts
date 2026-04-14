import { Component, ChangeDetectionStrategy, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { AdminHomeService } from '../../../../shared/services/admin/admin-home.service';
import { take, finalize } from 'rxjs/operators';

@Component({
  selector: 'app-admin-recent-alerts',
  imports: [DatePipe],
  templateUrl: './admin-recent-alerts.component.html',
  styleUrls: ['./admin-recent-alerts.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminRecentAlertsComponent implements OnInit {
  private readonly adminHomeService = inject(AdminHomeService);
  private readonly router = inject(Router);

  readonly loading = signal<boolean>(true);

  readonly alertasRecientes = this.adminHomeService.adminIncidenciasSignal;

  ngOnInit(): void {
    this.cargarAlertas();
  }

  private cargarAlertas(): void {
    this.loading.set(true);

    this.adminHomeService
      .obtenerIncidenciasAdmin('PENDING', 0, 5)
      .pipe(
        take(1),
        finalize(() => this.loading.set(false)),
      )
      .subscribe();
  }

  onViewAll(event: Event): void {
    event.preventDefault();
    void this.router.navigate(['/admin/incidencias']);
  }
}
