import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  signal,
  inject,
  OnInit,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-perfil-email-modal',
  imports: [
    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './perfil-email-modal.component.html',
  styleUrls: ['./perfil-email-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PerfilEmailModalComponent implements OnInit {
  private readonly fb = inject(FormBuilder);

  readonly loading = input.required<boolean>();

  readonly closeClicked = output<void>();
  readonly confirm = output<string>();

  readonly hideConfirmPassword = signal<boolean>(true);

  form!: FormGroup;

  ngOnInit(): void {
    this.form = this.fb.group({
      password: ['', Validators.required],
    });
  }

  toggleVisibility(): void {
    this.hideConfirmPassword.set(!this.hideConfirmPassword());
  }

  onClose(): void {
    this.closeClicked.emit();
  }
  onConfirm(): void {
    if (this.form.valid) {
      this.confirm.emit(this.form.value.password);
    }
  }
}
