import { Injectable, signal } from '@angular/core';

export type ModalKind = 'confirm' | 'alert' | 'select';

export interface ModalOption {
  value: string | number;
  label: string;
}

export interface ModalState {
  kind: ModalKind;
  title: string;
  message: string;
  confirmText: string;
  cancelText: string;
  danger: boolean;
  options: ModalOption[];
  selected: string | number | null;
}

@Injectable({ providedIn: 'root' })
export class ModalService {

  readonly state = signal<ModalState | null>(null);

  private resolver: ((value: any) => void) | null = null;

  confirm(
    title: string,
    message: string,
    opts: { confirmText?: string; cancelText?: string; danger?: boolean } = {}
  ): Promise<boolean> {
    return new Promise((resolve) => {
      this.resolver = resolve;
      this.state.set({
        kind: 'confirm',
        title,
        message,
        confirmText: opts.confirmText ?? 'تأكيد',
        cancelText: opts.cancelText ?? 'إلغاء',
        danger: opts.danger ?? false,
        options: [],
        selected: null,
      });
    });
  }

  alert(title: string, message: string): Promise<void> {
    return new Promise((resolve) => {
      this.resolver = () => resolve();
      this.state.set({
        kind: 'alert',
        title,
        message,
        confirmText: 'حسناً',
        cancelText: '',
        danger: false,
        options: [],
        selected: null,
      });
    });
  }

  select(title: string, message: string, options: ModalOption[]): Promise<string | number | null> {
    return new Promise((resolve) => {
      this.resolver = resolve;
      this.state.set({
        kind: 'select',
        title,
        message,
        confirmText: 'تأكيد',
        cancelText: 'إلغاء',
        danger: false,
        options,
        selected: options.length ? options[0].value : null,
      });
    });
  }

  updateSelected(value: string | number): void {
    const current = this.state();
    if (current) {
      this.state.set({ ...current, selected: value });
    }
  }

  confirmAction(): void {
    const current = this.state();
    if (!current) return;
    if (current.kind === 'select') {
      this.resolve(current.selected);
    } else {
      this.resolve(true);
    }
  }

  cancel(): void {
    const current = this.state();
    this.resolve(current?.kind === 'select' ? null : false);
  }

  private resolve(value: any): void {
    const r = this.resolver;
    this.resolver = null;
    this.state.set(null);
    if (r) {
      r(value);
    }
  }
}
