import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { TokenStorageService } from './token-storage.service';

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
