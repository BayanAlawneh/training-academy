import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal, computed } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, AuthUser, LoginRequest, LoginResult, ROLE_HOME, RoleName, SignupRequest } from '../models/auth.models';

const USER_KEY = 'academy.user';
const TOKEN_KEY = 'academy.token';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly http = inject(HttpClient);

  private readonly userSignal = signal<AuthUser | null>(this.restoreUser());

  readonly user = this.userSignal.asReadonly();
  readonly role = computed<RoleName | null>(() => this.userSignal()?.role ?? null);
  readonly isAuthenticated = computed(() => this.userSignal() !== null);

  login(credentials: LoginRequest): Observable<ApiResponse<LoginResult>> {
    return this.http
      .post<ApiResponse<LoginResult>>(`${environment.apiUrl}/auth/login`, credentials)
      .pipe(tap(response => this.storeSession(response.data)));
  }
signup(request: SignupRequest): Observable<ApiResponse<LoginResult>> {
    return this.http
      .post<ApiResponse<LoginResult>>(`${environment.apiUrl}/auth/signup`, request)
      .pipe(tap(response => this.storeSession(response.data)));
  }

  logout(): void {
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(TOKEN_KEY);
    this.userSignal.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  homeRoute(): string {
    const current = this.role();
    return current ? ROLE_HOME[current] : '/login';
  }

  private storeSession(result: LoginResult): void {
    const user: AuthUser = {
      id: result.id,
      username: result.username,
      name: result.name,
      email: result.email,
      role: result.role
    };
    localStorage.setItem(TOKEN_KEY, result.token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.userSignal.set(user);
  }

  private restoreUser(): AuthUser | null {
    const raw = localStorage.getItem(USER_KEY);
    const token = localStorage.getItem(TOKEN_KEY);
    if (!raw || !token) {
      return null;
    }
    try {
      return JSON.parse(raw) as AuthUser;
    } catch {
      localStorage.removeItem(USER_KEY);
      localStorage.removeItem(TOKEN_KEY);
      return null;
    }
  }
}