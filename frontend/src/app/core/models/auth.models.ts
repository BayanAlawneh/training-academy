export type RoleName = 'ADMIN' | 'TRAINER' | 'TRAINEE';

export interface AuthUser {
  id: number;
  username: string;
  name: string;
  email: string;
  role: RoleName;
}

export interface LoginResult extends AuthUser {
  token: string;
  tokenType: string;
  expiresInMs: number;
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
export interface SignupRequest {
  username: string;
  name: string;
  email: string;
  password: string;
  dateOfBirth: string;
}