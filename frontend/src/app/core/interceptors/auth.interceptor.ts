import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * يرفق التوكن بكل طلب، ويتعامل مع أخطاء المصادقة.
 *
 * القاعدة الحاسمة:
 *   401 = "لا نعرف من أنت" → الجلسة انتهت فعلاً → تسجيل خروج.
 *   403 = "نعرفك لكن ممنوع" → المستخدم يبقى داخل النظام، والصفحة تعرض الرسالة.
 *   500 = خطأ خادم → لا علاقة له بالجلسة إطلاقاً.
 *
 * الخلل السابق: الباك اند كان يردّ 401 عند أي خطأ غير معالَج (لأن الطلب
 * يُحوَّل إلى /error المحمي)، فيطرد هذا الإنترسبتور المستخدم عند كل فشل حذف.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const token = auth.getToken();
  const isAuthCall = request.url.includes('/auth/login')
    || request.url.includes('/auth/signup');

  const authorised = token && !isAuthCall
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(authorised).pipe(
    catchError((error: HttpErrorResponse) => {

      // خطأ شبكة (السيرفر مطفأ) — status = 0. لا تطرد المستخدم.
      if (error.status === 0) {
        return throwError(() => ({
          ...error,
          error: { message: 'تعذّر الاتصال بالخادم. تأكّد من تشغيل الباك اند.' }
        }));
      }

      // انتهاء الجلسة الحقيقي فقط.
      if (error.status === 401 && !isAuthCall && token) {
        auth.logout();
        router.navigateByUrl('/login');
      }

      return throwError(() => error);
    })
  );
};
