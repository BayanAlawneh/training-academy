export interface Enrollment {
  id: number;
  courseId: number;
  courseTitle: string;
  traineeId: number;
  traineeName: string;
  traineeEmail: string;
  enrolledOn: string;
}

export interface EnrollmentRequest {
  courseId: number;
  traineeId: number;
}
