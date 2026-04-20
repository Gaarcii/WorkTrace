import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-perfil-contact',
  imports: [
    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './perfil-contact.component.html',
  styleUrls: ['./perfil-contact.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PerfilContactComponent {
  readonly form = input.required<FormGroup>();
  readonly loading = input.required<boolean>();

  readonly save = output<void>();

  onSave(): void {
    this.save.emit();
  }
}
