import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'worker/home',
    loadComponent: () =>
      import('./features/worker/home/worker-home.component').then((m) => m.WorkerHomeComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_WORKER', 'WORKER'] },
  },
  {
    path: 'worker/historial',
    loadComponent: () =>
      import('./features/worker/historial/worker-historial.component').then(
        (m) => m.WorkerHistoryComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_WORKER', 'WORKER'] },
  },
  {
    path: 'worker/estadisticas',
    loadComponent: () =>
      import('./features/worker/estadisticas/worker-estadisticas.component').then(
        (m) => m.WorkerEstadisticasComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_WORKER', 'WORKER'] },
  },
  {
    path: 'worker/incidencias',
    loadComponent: () =>
      import('./features/worker/incidencias/worker-incidencias.component').then(
        (m) => m.WorkerIncidenciasComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_WORKER', 'WORKER'] },
  },
  {
    path: 'change-password',
    loadComponent: () =>
      import('./features/auth/password-change/password-change.component').then(
        (m) => m.PasswordChangeComponent,
      ),
    canActivate: [authGuard],
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: '**',
    redirectTo: '/login',
  },
];
