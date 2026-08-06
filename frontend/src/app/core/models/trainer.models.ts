export interface Trainer {
  id: number;
  userId: number;
  username: string;
  name: string;
  email: string;
  dateOfBirth: string;
  specialization: string | null;
}

export interface TrainerRequest {
  username: string;
  name: string;
  email: string;
  password?: string;
  dateOfBirth: string;
  specialization: string;
}