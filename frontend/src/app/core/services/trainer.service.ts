import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { Trainer, TrainerRequest } from '../models/trainer.models';

@Injectable({ providedIn: 'root' })
export class TrainerService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/trainers`;

  findAll(): Observable<ApiResponse<Trainer[]>> {
    return this.http.get<ApiResponse<Trainer[]>>(this.baseUrl);
  }

  create(request: TrainerRequest): Observable<ApiResponse<Trainer>> {
    return this.http.post<ApiResponse<Trainer>>(this.baseUrl, request);
  }

  update(id: number, request: TrainerRequest): Observable<ApiResponse<Trainer>> {
    return this.http.put<ApiResponse<Trainer>>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<ApiResponse<null>> {
    return this.http.delete<ApiResponse<null>>(`${this.baseUrl}/${id}`);
  }
}