import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-perfil-avatar-modal',
  imports: [MatIconModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './perfil-avatar-modal.component.html',
  styleUrls: ['./perfil-avatar-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PerfilAvatarModalComponent {
  readonly avatarUrl = input<string | undefined | null>(null);
  readonly avatarPreview = input.required<string>();
  readonly loading = input.required<boolean>();

  readonly close = output<void>();
  readonly fileSelected = output<Event>();
  readonly delete = output<void>();
  readonly save = output<void>();

  onClose(): void {
    this.close.emit();
  }
  onFileSelect(event: Event): void {
    this.fileSelected.emit(event);
  }
  onDelete(): void {
    this.delete.emit();
  }
  onSave(): void {
    this.save.emit();
  }
}
