export interface MyCourse {
  enrollmentId: number;
  courseId: number;
  title: string;
  capacity: number;
  trainerName: string;
  trainerEmail: string;
  trainerSpecialization: string | null;
  enrolledOn: string;
  description: string | null;
  durationWeeks: number | null;
}

export interface MySummary {
  name: string;
  email: string;
  memberSince: string | null;
  courseCount: number;
}

export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'LATE' | 'EXCUSED' | 'NOT_RECORDED';

export interface MyAttendance {
  sessionId: number;
  courseId: number;
  courseTitle: string;
  sessionTitle: string;
  sessionDate: string;
  startTime: string;
  endTime: string;
  meetingLink: string | null;
  sessionStatus: string;
  attendanceStatus: AttendanceStatus;
  note: string | null;
}

export interface TrainerSummary {
  name: string;
  email: string;
  specialization: string | null;
  courseId: number | null;
  courseTitle: string | null;
  traineeCount: number;
  sessionCount: number;
}

export interface TrainingSession {
  id: number;
  courseId: number;
  courseTitle: string;
  title: string;
  sessionDate: string;
  startTime: string;
  endTime: string;
  meetingLink: string | null;
  status: string;
}

export interface SessionPayload {
  title: string;
  sessionDate: string;
  startTime: string;
  endTime: string;
  meetingLink: string | null;
}

export interface SchedulePayload {
  startDate: string;
  weeks: number;
  weekdays: number[];
  startTime: string;
  endTime: string;
  titlePrefix: string | null;
}

export interface AttendanceRow {
  traineeId: number;
  traineeName: string;
  traineeEmail: string;
  status: AttendanceStatus;
  note: string | null;
}

export interface AttendanceMarkPayload {
  entries: { traineeId: number; status: string; note: string | null }[];
}

export const ATTENDANCE_LABEL: Record<AttendanceStatus, string> = {
  PRESENT: 'حاضر',
  ABSENT: 'غائب',
  LATE: 'متأخر',
  EXCUSED: 'بعذر',
  NOT_RECORDED: 'لم يُسجَّل'
};

export const WEEKDAYS: { value: number; label: string }[] = [
  { value: 7, label: 'الأحد' },
  { value: 1, label: 'الاثنين' },
  { value: 2, label: 'الثلاثاء' },
  { value: 3, label: 'الأربعاء' },
  { value: 4, label: 'الخميس' },
  { value: 5, label: 'الجمعة' },
  { value: 6, label: 'السبت' }
];
