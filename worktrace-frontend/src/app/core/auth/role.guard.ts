import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { TokenStorageService } from './token-storage.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const tokenStorage = inject(TokenStorageService);
  const router = inject(Router);

  const expectedRoles: string[] = route.data['roles'] || [];
  const userRole = tokenStorage.getRole();

  // EL CHIVATO: Nos dirá en la consola exactamente qué está pasando
  console.log('RoleGuard -> Rol en LocalStorage:', userRole);
  console.log('RoleGuard -> Roles esperados por la ruta:', expectedRoles);

  if (!userRole) {
    console.error('RoleGuard -> Bloqueo: El rol es null o no se ha guardado.');
    router.navigate(['/login']);
    return false;
  }

  if (expectedRoles.includes(userRole)) {
    return true; // El rol coincide, le dejamos pasar
  }

  console.error('RoleGuard -> Bloqueo: El rol del usuario no está en la lista de permitidos.');

  // Si tiene un rol válido pero se ha equivocado de sitio, lo mandamos a su casa
  if (userRole === 'ROLE_ADMIN' || userRole === 'ADMIN') {
    router.navigate(['/admin/home']);
  } else {
    // Si no es admin y tampoco pasó el filtro de worker... ¡al login por seguridad!
    router.navigate(['/login']);
  }

  return false;
};
