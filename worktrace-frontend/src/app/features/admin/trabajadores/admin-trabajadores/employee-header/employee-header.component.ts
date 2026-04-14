import { Component, ChangeDetectionStrategy, output } from '@angular/core';

@Component({
  selector: 'app-employee-header',
  templateUrl: './employee-header.component.html',
  styleUrl: './employee-header.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EmployeeHeaderComponent {
  public readonly create = output<void>();

  public onCreateAction(): void {
    this.create.emit();
  }
}