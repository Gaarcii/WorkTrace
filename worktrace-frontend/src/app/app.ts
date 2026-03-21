import { Component, signal, inject, OnInit, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { HeaderBarComponent } from './shared/components/header-bar/header-bar.component';
import { WorkerBottomNavComponent } from './features/worker/navbar/worker-navbar.component';
import { TokenStorageService } from './core/auth/token-storage.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderBarComponent, WorkerBottomNavComponent],
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

        this.showHeader.set(!(isLogin || isChangePassword));
        this.userRole.set(this.tokenStorage.getRole());
      });
  }
}
