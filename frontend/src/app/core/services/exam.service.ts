import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { Exam, ExamPayload, GradeRow } from '../models/exam.models';

/** اختبارات المدرّب. */
@Injectable({ providedIn: 'root' })
export class ExamService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/trainer/me/exams`;

  list(): Observable<ApiResponse<Exam[]>> {
    return this.http.get<ApiResponse<Exam[]>>(this.baseUrl);
  }

  detail(id: number): Observable<ApiResponse<Exam>> {
    return this.http.get<ApiResponse<Exam>>(`${this.baseUrl}/${id}`);
  }

  create(payload: ExamPayload): Observable<ApiResponse<Exam>> {
    return this.http.post<ApiResponse<Exam>>(this.baseUrl, payload);
  }

  update(id: number, payload: ExamPayload): Observable<ApiResponse<Exam>> {
    return this.http.put<ApiResponse<Exam>>(`${this.baseUrl}/${id}`, payload);
  }

  setPublished(id: number, value: boolean): Observable<ApiResponse<Exam>> {
    return this.http.put<ApiResponse<Exam>>(`${this.baseUrl}/${id}/published?value=${value}`, {});
  }

  delete(id: number): Observable<ApiResponse<null>> {
    return this.http.delete<ApiResponse<null>>(`${this.baseUrl}/${id}`);
  }

  grades(id: number): Observable<ApiResponse<GradeRow[]>> {
    return this.http.get<ApiResponse<GradeRow[]>>(`${this.baseUrl}/${id}/grades`);
  }
}
