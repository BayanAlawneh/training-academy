import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ModalService } from '../../core/services/modal.service';

@Component({
  selector: 'app-modal',
  imports: [FormsModule],
  templateUrl: './modal.html',
  styleUrl: './modal.css'
})
export class Modal {
  readonly modal = inject(ModalService);

  onSelectChange(value: string): void {
    this.modal.updateSelected(value);
  }
}
