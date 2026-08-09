export interface Trainee {
  id: number;
  userId: number;
  username: string;
  name: string;
  email: string;
  dateOfBirth: string;
  enrollmentDate: string | null;
}

export interface TraineeRequest {
  username: string;
  name: string;
  email: string;
  password?: string;
  dateOfBirth: string;
  enrollmentDate?: string;
}