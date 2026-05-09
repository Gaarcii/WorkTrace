import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { TokenStorageService } from './token-storage.service';

/**
 * Guardia de autorización funcional basado en roles.
 *
 * @description
 * Este guardia verifica si el rol del usuario actual le permite acceder a una ruta específica.
 * El rol del usuario se obtiene del token de autenticación y se compara con una lista de
 * roles esperados definidos en la propiedad `data.roles` de la configuración de la ruta.
 * Si el rol es válido, permite el acceso. De lo contrario, redirige al usuario
 * a una página apropiada (login o el home de admin).
 *
 * @param route - La instantánea de la ruta que se está intentando activar. Contiene los datos de la ruta, como los roles esperados.
 * @param state - El estado del enrutador en el momento de la activación.
 *
 * @returns `true` si el rol del usuario está en la lista de roles permitidos, de lo contrario `false` y provoca una redirección.
 */
export const roleGuard: CanActivateFn = (route, state) => {
  const tokenStorage = inject(TokenStorageService);
  const router = inject(Router);

  const expectedRoles: string[] = route.data['roles'] || [];
  const userRole = tokenStorage.getRole();

  if (!userRole) {
    router.navigate(['/login']);
    return false;
  }

  if (expectedRoles.includes(userRole)) {
    return true;
  }

  if (userRole === 'ROLE_ADMIN' || userRole === 'ADMIN') {
    router.navigate(['/admin/home']);
  } else {
    router.navigate(['/login']);
  }

  return false;
};
