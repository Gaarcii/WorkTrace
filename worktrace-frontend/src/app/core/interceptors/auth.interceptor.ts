import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenStorageService } from '../auth/token-storage.service';

/**
 * Interceptor funcional de HTTP para añadir el token de autenticación a las peticiones salientes.
 *
 * @description
 * Este interceptor se ejecuta para cada petición HTTP que realiza la aplicación.
 * Su propósito es obtener el token de autenticación (JWT) del `TokenStorageService` y,
 * si existe, añadirlo a la cabecera `Authorization` de la petición como un "Bearer token".
 * Esto asegura que las peticiones a la API estén debidamente autenticadas.
 *
 * @param req - La petición HTTP saliente que se va a interceptar.
 * @param next - El siguiente manejador en la cadena de interceptores, que se encarga de procesar la petición.
 *
 * @returns Un `Observable` que emite el evento HTTP, con la petición posiblemente modificada para incluir el token.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenStorage = inject(TokenStorageService);
  const token = tokenStorage.getToken();

  if (token) {
    const clonedRequest = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`),
    });
    return next(clonedRequest);
  }

  return next(req);
};
