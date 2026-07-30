import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal, computed } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, AuthUser, LoginRequest, ROLE_HOME, RoleName } from '../models/auth.models';

const USER_KEY = 'academy.user';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly http = inject(HttpClient);

  private readonly userSignal = signal<AuthUser | null>(this.restoreUser());

  readonly user = this.userSignal.asReadonly();
  readonly role = computed<RoleName | null>(() => this.userSignal()?.role ?? null);
  readonly isAuthenticated = computed(() => this.userSignal() !== null);

  login(credentials: LoginRequest): Observable<ApiResponse<AuthUser>> {
    return this.http
      .post<ApiResponse<AuthUser>>(`${environment.apiUrl}/auth/login`, credentials)
      .pipe(tap(response => this.storeUser(response.data)));
  }

  logout(): void {
    localStorage.removeItem(USER_KEY);
    this.userSignal.set(null);
  }

  homeRoute(): string {
    const current = this.role();
    return current ? ROLE_HOME[current] : '/login';
  }

  private storeUser(user: AuthUser): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.userSignal.set(user);
  }

  private restoreUser(): AuthUser | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as AuthUser;
    } catch {
      localStorage.removeItem(USER_KEY);
      return null;
    }
  }
}