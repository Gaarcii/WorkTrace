import { Component, signal, inject, OnInit, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { HeaderBarComponent } from './shared/components/header-bar/header-bar.component';
import { WorkerBottomNavComponent } from './features/worker/navbar/worker-navbar.component';
import { AdminNavbarComponent } from './features/admin/navbar/admin-navbar.component';
import { InspectorBottomNavComponent } from './features/inspector/navbar/inspector-navbar.component';
import { TokenStorageService } from './core/auth/token-storage.service';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    HeaderBarComponent,
    WorkerBottomNavComponent,
    AdminNavbarComponent,
    InspectorBottomNavComponent,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly tokenStorage = inject(TokenStorageService);

  readonly showHeader = signal<boolean>(false);
  readonly userRole = signal<string | null>(null);

  ngOnInit() {
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((event: NavigationEnd) => {
        const url = event.urlAfterRedirects;
        const isLogin = url.includes('/login');
        const isChangePassword = url.includes('/change-password');
        const isResetPassword = url.includes('/reset-password');
        const isRegisterCompany = url.includes('/register-company');

        this.showHeader.set(!(isLogin || isChangePassword || isResetPassword || isRegisterCompany));
        this.userRole.set(this.tokenStorage.getRole());
        // Siempre desplazar al inicio al cambiar de ruta
        try {
          window.scrollTo({ top: 0, left: 0, behavior: 'auto' });
        } catch (e) {
          // noop en entornos sin DOM
        }
      });
  }
}
