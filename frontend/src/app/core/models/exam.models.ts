export type ExamState = 'UPCOMING' | 'OPEN' | 'SUBMITTED' | 'MISSED';
export type GradeState = 'SUBMITTED' | 'MISSED' | 'PENDING';

export interface ExamOption {
  id: number;
  text: string;
  correct: boolean | null;
}

export interface ExamQuestion {
  id: number;
  text: string;
  type: string;
  marks: number;
  position: number;
  options: ExamOption[];
}

export interface Exam {
  id: number;
  courseId: number;
  courseTitle: string;
  title: string;
  description: string | null;
  totalMarks: number;
  opensAt: string;
  closesAt: string;
  published: boolean;
  questionCount: number;
  submissionCount: number;
  questions: ExamQuestion[] | null;
}

export interface MyExam {
  examId: number;
  courseId: number;
  courseTitle: string;
  title: string;
  description: string | null;
  totalMarks: number;
  opensAt: string;
  closesAt: string;
  questionCount: number;
  state: ExamState;
  score: number | null;
  submittedAt: string | null;
}

export interface ExamPaper {
  examId: number;
  title: string;
  description: string | null;
  totalMarks: number;
  closesAt: string;
  questions: ExamQuestion[];
}

export interface SubmissionResult {
  examId: number;
  examTitle: string;
  score: number;
  totalMarks: number;
  correctCount: number;
  questionCount: number;
}

export interface GradeRow {
  traineeId: number;
  traineeName: string;
  traineeEmail: string;
  score: number | null;
  totalMarks: number;
  state: GradeState;
  submittedAt: string | null;
}

export interface ExamPayload {
  title: string;
  description: string | null;
  opensAt: string;
  closesAt: string;
  questions: {
    text: string;
    type: string;
    marks: number;
    options: { text: string; correct: boolean }[];
  }[];
}

export const EXAM_STATE_LABEL: Record<ExamState, string> = {
  UPCOMING: 'لم يبدأ بعد',
  OPEN: 'متاح الآن',
  SUBMITTED: 'تم التسليم',
  MISSED: 'فاتك'
};

export const GRADE_STATE_LABEL: Record<GradeState, string> = {
  SUBMITTED: 'سلّم',
  MISSED: 'لم يسلّم',
  PENDING: 'بانتظار'
};
