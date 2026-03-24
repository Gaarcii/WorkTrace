import {
  Component,
  ChangeDetectionStrategy,
  signal,
  inject,
  OnInit,
  DestroyRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd, RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs/operators';
import { MatIconModule } from '@angular/material/icon';
import { MatRippleModule } from '@angular/material/core'; // Añade el efecto "onda" al hacer clic

@Component({
  selector: 'app-worker-bottom-nav',
  imports: [CommonModule, RouterModule, MatIconModule, MatRippleModule],
  templateUrl: './worker-navbar.component.html',
  styleUrls: ['./worker-navbar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerBottomNavComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly activeTab = signal<string>('home');

  ngOnInit() {
    this.updateActiveTab(this.router.url);

    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((event: NavigationEnd) => {
        this.updateActiveTab(event.urlAfterRedirects);
      });
  }

  private updateActiveTab(url: string) {
    if (url.includes('/worker/home')) {
      this.activeTab.set('home');
    } else if (url.includes('/worker/historial')) {
      this.activeTab.set('history');
    } else if (url.includes('/worker/estadisticas')) {
      this.activeTab.set('stats');
    } else if (url.includes('/worker/incidencias')) {
      this.activeTab.set('incidence');
    } else if (url.includes('/worker/home')) {
      this.activeTab.set('user');
    } else {
      this.activeTab.set('home');
    }
  }
}
