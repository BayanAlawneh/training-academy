export type RoleName = 'ADMIN' | 'TRAINER' | 'TRAINEE';

export interface AuthUser {
  id: number;
  name: string;
  email: string;
  role: RoleName;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export const ROLE_HOME: Record<RoleName, string> = {
  ADMIN: '/admin',
  TRAINER: '/trainer',
  TRAINEE: '/trainee'
};