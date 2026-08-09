import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { Course, CourseRequest } from '../models/course.models';

@Injectable({ providedIn: 'root' })
export class CourseService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/courses`;

  findAll(): Observable<ApiResponse<Course[]>> {
    return this.http.get<ApiResponse<Course[]>>(this.baseUrl);
  }

  create(request: CourseRequest): Observable<ApiResponse<Course>> {
    return this.http.post<ApiResponse<Course>>(this.baseUrl, request);
  }

  update(id: number, request: CourseRequest): Observable<ApiResponse<Course>> {
    return this.http.put<ApiResponse<Course>>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<ApiResponse<null>> {
    return this.http.delete<ApiResponse<null>>(`${this.baseUrl}/${id}`);
  }
}
