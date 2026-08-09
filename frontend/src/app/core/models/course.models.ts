export interface Course {
  id: number;
  title: string;
  capacity: number;
  trainerId: number;
  trainerName: string;
  enrolledCount: number;
  full: boolean;
}

export interface CourseRequest {
  title: string;
  capacity: number;
  trainerId: number;
}
