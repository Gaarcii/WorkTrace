import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { AdminConfigSectionItem, AdminConfigSectionKey } from '../admin-config.types';

@Component({
  selector: 'app-admin-config-header',
  standalone: true,
  templateUrl: './admin-config-header.component.html',
  styleUrl: './admin-config-header.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminConfigHeaderComponent {
  readonly sections = input.required<AdminConfigSectionItem[]>();
  readonly activeSection = input.required<AdminConfigSectionKey>();

  readonly sectionChange = output<AdminConfigSectionKey>();

  setSection(section: AdminConfigSectionKey): void {
    this.sectionChange.emit(section);
  }
}
