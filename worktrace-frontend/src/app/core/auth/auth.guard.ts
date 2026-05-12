import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { TokenStorageService } from './token-storage.service';

/**
 * Guardia de autenticación funcional para proteger las rutas.
 *
 * @description
 * Este guardia verifica si el usuario tiene un token de autenticación almacenado.
 * Si el token existe, permite el acceso a la ruta solicitada. En caso contrario,
 * redirige al usuario a la página de inicio de sesión.
 *
 * @param route - La instantánea de la ruta que se está activando.
 * @param state - El estado del enrutador en el momento de la activación.
 *
 * @returns `true` si el usuario está autenticado, de lo contrario `false` y redirige a '/login'.
 */
export const authGuard: CanActivateFn = () => {
  const tokenStorage = inject(TokenStorageService);
  const router = inject(Router);

  if (tokenStorage.getToken()) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};
