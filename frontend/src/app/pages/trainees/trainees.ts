import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { TraineeService } from '../../core/services/trainee.service';
import { Trainee } from '../../core/models/trainee.models';

@Component({
  selector: 'app-trainees',
  imports: [ReactiveFormsModule],
  templateUrl: './trainees.html',
  styleUrl: './trainees.css'
})
export class Trainees implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly traineeService = inject(TraineeService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly trainees = signal<Trainee[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly editingId = signal<number | null>(null);
  readonly formOpen = signal(false);

  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    name: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: [''],
    dateOfBirth: ['', [Validators.required]],
    enrollmentDate: ['']
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.traineeService.findAll().subscribe({
      next: (response) => {
        this.trainees.set(response.data);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'تعذّر تحميل المتدربين');
        this.loading.set(false);
      }
    });
  }

  openCreate(): void {
    this.editingId.set(null);
    this.errorMessage.set(null);
    this.form.reset();
    this.form.controls.password.setValidators([Validators.required, Validators.minLength(6)]);
    this.form.controls.password.updateValueAndValidity();
    this.formOpen.set(true);
  }

  openEdit(trainee: Trainee): void {
    this.editingId.set(trainee.id);
    this.errorMessage.set(null);
    this.form.setValue({
      username: trainee.username,
      name: trainee.name,
      email: trainee.email,
      password: '',
      dateOfBirth: trainee.dateOfBirth,
      enrollmentDate: trainee.enrollmentDate ?? ''
    });
    this.form.controls.password.setValidators([Validators.minLength(6)]);
    this.form.controls.password.updateValueAndValidity();
    this.formOpen.set(true);
  }

  cancel(): void {
    this.formOpen.set(false);
    this.errorMessage.set(null);
  }

  submit(): void {
    this.errorMessage.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const value = this.form.getRawValue();
    const id = this.editingId();

    const payload = {
      username: value.username,
      name: value.name,
      email: value.email,
      dateOfBirth: value.dateOfBirth,
      ...(value.password ? { password: value.password } : {}),
      ...(value.enrollmentDate ? { enrollmentDate: value.enrollmentDate } : {})
    };

    const request = id === null
      ? this.traineeService.create(payload)
      : this.traineeService.update(id, payload);

    request.subscribe({
      next: () => {
        this.saving.set(false);
        this.formOpen.set(false);
        this.load();
      },
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err?.error?.message ?? 'تعذّر حفظ البيانات');
      }
    });
  }

  remove(trainee: Trainee): void {
    if (!confirm(`حذف المتدرّب ${trainee.name}؟`)) {
      return;
    }

    this.traineeService.delete(trainee.id).subscribe({
      next: () => this.load(),
      error: (err) => this.errorMessage.set(err?.error?.message ?? 'تعذّر الحذف')
    });
  }

  back(): void {
    this.router.navigateByUrl('/admin');
  }

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}