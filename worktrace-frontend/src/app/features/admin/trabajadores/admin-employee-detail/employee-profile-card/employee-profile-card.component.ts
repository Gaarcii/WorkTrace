import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { DatePipe } from '@angular/common';
import { EmployeeResponseDto } from '../../../../../shared/models/profile.model';

@Component({
  selector: 'app-employee-profile-card',
  templateUrl: './employee-profile-card.component.html',
  styleUrl: './employee-profile-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe],
})
export class EmployeeProfileCardComponent {
  public readonly empleado = input<EmployeeResponseDto | null>(null);

  public readonly editProfile = output<void>();
  public readonly goSchedule = output<void>();

  public onEditProfile(): void {
    this.editProfile.emit();
  }

  public onGoSchedule(): void {
    this.goSchedule.emit();
  }
}
