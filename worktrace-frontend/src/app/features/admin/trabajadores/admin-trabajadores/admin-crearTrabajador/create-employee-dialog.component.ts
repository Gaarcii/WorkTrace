import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CreateEmployeeForm } from '../../admin-trabajadores.types';
import { JobPositionUiDto } from '../../../../../shared/models/profile.model';

@Component({
  selector: 'app-create-employee-dialog',
  imports: [CommonModule, FormsModule],
  templateUrl: './create-employee-dialog.component.html',
  styleUrl: './create-employee-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreateEmployeeDialogComponent {
  readonly modelValue = input.required<boolean>();
  readonly loading = input.required<boolean>();
  readonly mostrarCredenciales = input(false);
  readonly formData = input.required<CreateEmployeeForm>();
  readonly rules = input<Record<string, unknown>>({});
  readonly credencialesRecientes = input<unknown | null>(null);
  readonly error = input<string | null>(null);
  readonly puestosTrabajo = input.required<JobPositionUiDto[]>();

  readonly close = output<void>();
  readonly submit = output<void>();
  readonly openJobPositions = output<void>();
  readonly copyCredentials = output<unknown>();

  onFullNameChange(value: string): void {
    this.formData().fullName = value;
  }

  onEmployeeCodeChange(value: string): void {
    this.formData().employeeCode = value;
  }

  onEmailChange(value: string): void {
    this.formData().email = value;
  }

  onPhoneChange(value: string): void {
    this.formData().phone = value;
  }

  onWeeklyHoursChange(value: unknown): void {
    const parsed = typeof value === 'number' ? value : Number(value);
    this.formData().weeklyHours = Number.isFinite(parsed) ? parsed : null;
  }

  onPositionChange(value: string | null): void {
    this.formData().positionId = value;
  }
}
