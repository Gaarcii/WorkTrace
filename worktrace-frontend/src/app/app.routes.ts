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
    path: 'register-company',
    loadComponent: () =>
      import('./features/auth/register-company/register-company.component').then(
        (m) => m.RegisterCompanyComponent,
      ),
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
    path: 'worker/perfil',
    loadComponent: () =>
      import('./features/worker/perfil/worker-perfil.component').then(
        (m) => m.WorkerPerfilComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_WORKER', 'WORKER'] },
  },
  {
    path: 'admin/home',
    loadComponent: () =>
      import('./features/admin/home/admin-home.component').then((m) => m.AdminHomeComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ADMIN'] },
  },
  {
    path: 'admin/incidencias',
    loadComponent: () =>
      import('./features/admin/incidencias/admin-incidencias.component').then(
        (m) => m.AdminIncidenciasComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ADMIN'] },
  },
  {
    path: 'admin/perfil',
    loadComponent: () =>
      import('./features/admin/perfil/admin-perfil.component').then((m) => m.AdminPerfilComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ADMIN'] },
  },
  {
    path: 'admin/trabajadores',
    loadComponent: () =>
      import('./features/admin/trabajadores/admin-trabajadores/admin-trabajadores.component').then(
        (m) => m.AdminTrabajadoresComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ADMIN'] },
  },
  {
    path: 'admin/trabajadores/:idTrabajador/horarios',
    loadComponent: () =>
      import('./features/admin/trabajadores/admin-employee-schedule-manager/admin-employee-schedule-manager.component').then(
        (m) => m.AdminEmployeeScheduleManagerComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ADMIN'] },
  },
  {
    path: 'admin/trabajadores/:id',
    loadComponent: () =>
      import('./features/admin/trabajadores/admin-employee-detail/admin-employee-detail.component').then(
        (m) => m.AdminEmployeeDetailComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ADMIN'] },
  },
  {
    path: 'admin/config',
    loadComponent: () =>
      import('./features/admin/config/admin-congif.component').then((m) => m.AdminCongifComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ADMIN'] },
  },
  {
    path: 'inspector/home',
    loadComponent: () =>
      import('./features/inspector/home/inspector-home.component').then(
        (m) => m.InspectorHomeComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_INSPECTOR', 'INSPECTOR'] },
  },
  {
    path: 'inspector/empleados',
    loadComponent: () =>
      import('./features/inspector/employee/inspector-employees.component').then(
        (m) => m.InspectorEmployeesComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_INSPECTOR', 'INSPECTOR'] },
  },
  {
    path: 'inspector/incidencias',
    loadComponent: () =>
      import('./features/inspector/incidence/inspector-incidences.component').then(
        (m) => m.InspectorIncidencesComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_INSPECTOR', 'INSPECTOR'] },
  },
  {
    path: 'inspector/registros',
    loadComponent: () =>
      import('./features/inspector/dailyClosure/inspector-registros.component').then(
        (m) => m.InspectorRegistrosComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_INSPECTOR', 'INSPECTOR'] },
  },
  {
    path: 'inspector/auditoria',
    loadComponent: () =>
      import('./features/inspector/auditoria/inspector-audit.component').then(
        (m) => m.InspectorAuditComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_INSPECTOR', 'INSPECTOR'] },
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
    path: 'reset-password',
    loadComponent: () =>
      import('./features/auth/reset-password/reset-password.component').then(
        (m) => m.ResetPasswordComponent,
      ),
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
