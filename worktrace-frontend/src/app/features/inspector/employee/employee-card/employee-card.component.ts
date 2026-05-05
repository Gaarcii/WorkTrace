import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { EmployeeDto } from '../../../../shared/models/inspector.model';

@Component({
  selector: 'app-employee-card',
  imports: [MatCardModule, MatIconModule, MatDividerModule],
  templateUrl: './employee-card.component.html',
  styleUrls: ['./employee-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmployeeCardComponent {
  readonly employee = input.required<EmployeeDto>();
  readonly cardClick = output<EmployeeDto>();
}
