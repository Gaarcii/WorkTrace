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
import { MatRippleModule } from '@angular/material/core';

@Component({
  selector: 'app-admin-navbar',
  imports: [CommonModule, RouterModule, MatIconModule, MatRippleModule],
  templateUrl: './admin-navbar.component.html',
  styleUrls: ['./admin-navbar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminNavbarComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly activeTab = signal<string>('home');

  ngOnInit(): void {
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

  private updateActiveTab(url: string): void {
    if (url.includes('/admin/home') || url === '/admin') {
      this.activeTab.set('home');
    } else if (url.includes('/admin/incidencias')) {
      this.activeTab.set('incidencia');
    } else if (url.includes('/admin/trabajadores')) {
      this.activeTab.set('trabajadores');
    } else if (url.includes('/admin/perfil')) {
      this.activeTab.set('perfil');
      } else if (url.includes('/admin/config')) {
      this.activeTab.set('configuracion');
    } else {
      this.activeTab.set('home');
    }
  }
}
