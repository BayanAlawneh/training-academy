export type NotificationType =
  | 'EXAM_PUBLISHED' | 'SESSION_SCHEDULED' | 'ATTENDANCE_RECORDED'
  | 'ENROLLED' | 'GENERAL';

export interface AppNotification {
  id: number;
  title: string;
  body: string | null;
  link: string | null;
  type: NotificationType;
  read: boolean;
  createdAt: string;
}
