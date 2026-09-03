import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { ExamPaper, MyExam, SubmissionResult } from '../models/exam.models';

/** اختبارات المتدرّب. */
@Injectable({ providedIn: 'root' })
export class TraineeExamService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/trainee/me/exams`;

  list(): Observable<ApiResponse<MyExam[]>> {
    return this.http.get<ApiResponse<MyExam[]>>(this.baseUrl);
  }

  paper(examId: number): Observable<ApiResponse<ExamPaper>> {
    return this.http.get<ApiResponse<ExamPaper>>(`${this.baseUrl}/${examId}/paper`);
  }

  submit(examId: number, answers: { questionId: number; selectedOptionId: number | null }[]):
      Observable<ApiResponse<SubmissionResult>> {
    return this.http.post<ApiResponse<SubmissionResult>>(
      `${this.baseUrl}/${examId}/submit`, { answers });
  }
}
