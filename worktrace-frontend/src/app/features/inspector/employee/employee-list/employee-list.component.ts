import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { EmployeeCardComponent } from '../employee-card/employee-card.component';
import { EmployeeDto } from '../../../../shared/models/inspector.model';

@Component({
  selector: 'app-employee-list',
  imports: [MatProgressSpinnerModule, MatIconModule, EmployeeCardComponent],
  templateUrl: './employee-list.component.html',
  styleUrls: ['./employee-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmployeeListComponent {
  readonly employees = input.required<EmployeeDto[]>();
  readonly loading = input.required<boolean>();
  readonly employeeSelected = output<EmployeeDto>();
}
