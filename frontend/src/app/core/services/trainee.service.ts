import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { Trainee, TraineeRequest } from '../models/trainee.models';

@Injectable({ providedIn: 'root' })
export class TraineeService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/trainees`;

  findAll(): Observable<ApiResponse<Trainee[]>> {
    return this.http.get<ApiResponse<Trainee[]>>(this.baseUrl);
  }

  create(request: TraineeRequest): Observable<ApiResponse<Trainee>> {
    return this.http.post<ApiResponse<Trainee>>(this.baseUrl, request);
  }

  update(id: number, request: TraineeRequest): Observable<ApiResponse<Trainee>> {
    return this.http.put<ApiResponse<Trainee>>(`${this.baseUrl}/${id}`, request);
  }

  /** يرجع عدد الكورسات المسجَّل فيها المتدرّب قبل الحذف. */
  deletionCheck(id: number): Observable<ApiResponse<number>> {
    return this.http.get<ApiResponse<number>>(`${this.baseUrl}/${id}/deletion-check`);
  }

  delete(id: number): Observable<ApiResponse<null>> {
    return this.http.delete<ApiResponse<null>>(`${this.baseUrl}/${id}`);
  }
}