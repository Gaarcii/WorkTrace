import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs/operators';
import { MatIconModule } from '@angular/material/icon';
import { MatRippleModule } from '@angular/material/core';

@Component({
  selector: 'app-inspector-bottom-nav',
  imports: [CommonModule, RouterModule, MatIconModule, MatRippleModule],
  templateUrl: './inspector-navbar.component.html',
  styleUrls: ['./inspector-navbar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InspectorBottomNavComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly activeTab = signal<string>('dashboard');

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
    if (url.includes('/inspector/empleados')) {
      this.activeTab.set('empleados');
    } else if (url.includes('/inspector/incidencias')) {
      this.activeTab.set('incidencias');
    } else if (url.includes('/inspector/registros')) {
      this.activeTab.set('registros');
    } else if (url.includes('/inspector/auditoria')) {
      this.activeTab.set('auditoria');
    } else {
      this.activeTab.set('dashboard');
    }
  }
}
