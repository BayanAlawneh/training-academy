import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { TrainerService } from '../../core/services/trainer.service';
import { Trainer } from '../../core/models/trainer.models';

@Component({
  selector: 'app-trainers',
  imports: [ReactiveFormsModule],
  templateUrl: './trainers.html',
  styleUrl: './trainers.css'
})
export class Trainers implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly trainerService = inject(TrainerService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly trainers = signal<Trainer[]>([]);
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
    specialization: ['']
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.trainerService.findAll().subscribe({
      next: (response) => {
        this.trainers.set(response.data);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'تعذّر تحميل المدربين');
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

  openEdit(trainer: Trainer): void {
    this.editingId.set(trainer.id);
    this.errorMessage.set(null);
    this.form.setValue({
      username: trainer.username,
      name: trainer.name,
      email: trainer.email,
      password: '',
      dateOfBirth: trainer.dateOfBirth,
      specialization: trainer.specialization ?? ''
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
      specialization: value.specialization,
      ...(value.password ? { password: value.password } : {})
    };

    const request = id === null
      ? this.trainerService.create(payload)
      : this.trainerService.update(id, payload);

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

  remove(trainer: Trainer): void {
    if (!confirm(`حذف المدرّب ${trainer.name}؟`)) {
      return;
    }

    this.trainerService.delete(trainer.id).subscribe({
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