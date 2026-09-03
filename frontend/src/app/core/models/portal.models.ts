export interface MyCourse {
  enrollmentId: number;
  courseId: number;
  title: string;
  capacity: number;
  trainerName: string;
  trainerEmail: string;
  trainerSpecialization: string | null;
  enrolledOn: string;
}

export interface MySummary {
  name: string;
  email: string;
  memberSince: string | null;
  courseCount: number;
}
