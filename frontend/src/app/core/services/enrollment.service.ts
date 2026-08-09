import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { Enrollment, EnrollmentRequest } from '../models/enrollment.models';

@Injectable({ providedIn: 'root' })
export class EnrollmentService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/enrollments`;

  findByCourse(courseId: number): Observable<ApiResponse<Enrollment[]>> {
    return this.http.get<ApiResponse<Enrollment[]>>(`${this.baseUrl}/course/${courseId}`);
  }

  enrol(request: EnrollmentRequest): Observable<ApiResponse<Enrollment>> {
    return this.http.post<ApiResponse<Enrollment>>(this.baseUrl, request);
  }

  remove(id: number): Observable<ApiResponse<null>> {
    return this.http.delete<ApiResponse<null>>(`${this.baseUrl}/${id}`);
  }
}
