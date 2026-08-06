import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const token = auth.getToken();
  const isAuthCall = request.url.includes('/auth/login')
    || request.url.includes('/auth/signup');

  const authorised = token && !isAuthCall
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(authorised).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isAuthCall) {
        auth.logout();
        router.navigateByUrl('/login');
      }
      return throwError(() => error);
    })
  );
};