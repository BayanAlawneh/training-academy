import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import {
  AttendanceMarkPayload, AttendanceRow, SchedulePayload,
  SessionPayload, TrainerSummary, TrainingSession
} from '../models/portal.models';

@Injectable({ providedIn: 'root' })
export class TrainerPortalService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/trainer/me`;

  summary(): Observable<ApiResponse<TrainerSummary>> {
    return this.http.get<ApiResponse<TrainerSummary>>(`${this.baseUrl}/summary`);
  }

  sessions(): Observable<ApiResponse<TrainingSession[]>> {
    return this.http.get<ApiResponse<TrainingSession[]>>(`${this.baseUrl}/sessions`);
  }

  createSession(payload: SessionPayload): Observable<ApiResponse<TrainingSession>> {
    return this.http.post<ApiResponse<TrainingSession>>(`${this.baseUrl}/sessions`, payload);
  }

  generateSchedule(payload: SchedulePayload): Observable<ApiResponse<TrainingSession[]>> {
    return this.http.post<ApiResponse<TrainingSession[]>>(`${this.baseUrl}/sessions/generate`, payload);
  }

  updateSession(id: number, payload: SessionPayload): Observable<ApiResponse<TrainingSession>> {
    return this.http.put<ApiResponse<TrainingSession>>(`${this.baseUrl}/sessions/${id}`, payload);
  }

  deleteSession(id: number): Observable<ApiResponse<null>> {
    return this.http.delete<ApiResponse<null>>(`${this.baseUrl}/sessions/${id}`);
  }

  roster(sessionId: number): Observable<ApiResponse<AttendanceRow[]>> {
    return this.http.get<ApiResponse<AttendanceRow[]>>(`${this.baseUrl}/sessions/${sessionId}/attendance`);
  }

  markAttendance(sessionId: number, payload: AttendanceMarkPayload): Observable<ApiResponse<AttendanceRow[]>> {
    return this.http.put<ApiResponse<AttendanceRow[]>>(`${this.baseUrl}/sessions/${sessionId}/attendance`, payload);
  }
}
