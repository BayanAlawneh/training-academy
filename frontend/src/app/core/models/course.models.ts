export interface Course {
  id: number;
  title: string;
  capacity: number;
  trainerId: number;
  trainerName: string;
  enrolledCount: number;
  full: boolean;
  description: string | null;
  durationWeeks: number | null;
}

export interface CourseRequest {
  title: string;
  capacity: number;
  trainerId: number;
  description: string | null;
  durationWeeks: number | null;
}
