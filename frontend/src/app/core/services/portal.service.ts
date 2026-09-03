import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { MyCourse, MySummary } from '../models/portal.models';

/**
 * بوابة المتدرّب. لا تمرّر أي معرّف — الخادم يعرف صاحب الطلب من التوكن.
 */
@Injectable({ providedIn: 'root' })
export class PortalService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/trainee/me`;

  myCourses(): Observable<ApiResponse<MyCourse[]>> {
    return this.http.get<ApiResponse<MyCourse[]>>(`${this.baseUrl}/courses`);
  }

  mySummary(): Observable<ApiResponse<MySummary>> {
    return this.http.get<ApiResponse<MySummary>>(`${this.baseUrl}/summary`);
  }
}
