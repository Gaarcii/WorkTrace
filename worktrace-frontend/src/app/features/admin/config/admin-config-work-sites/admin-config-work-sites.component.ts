import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { WorkSiteView } from '../admin-config.types';
import { WorkSiteRequestDto } from '../../../../shared/models/work-site.model';

@Component({
  selector: 'app-admin-config-work-sites',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './admin-config-work-sites.component.html',
  styleUrl: './admin-config-work-sites.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminConfigWorkSitesComponent {
  readonly loading = input.required<boolean>();
  readonly workSites = input.required<WorkSiteView[]>();
  readonly saving = input.required<boolean>();
  readonly deleting = input.required<boolean>();

  readonly dialogCreateOpen = input.required<boolean>();
  readonly dialogDeleteOpen = input.required<boolean>();
  readonly formData = input.required<WorkSiteRequestDto>();
  readonly editingSite = input<WorkSiteView | null>(null);
  readonly siteToDelete = input<WorkSiteView | null>(null);
  readonly errorMessage = input<string>('');

  readonly openCreateDialog = output<void>();
  readonly closeCreateDialog = output<void>();
  readonly submitSite = output<void>();
  readonly editSite = output<WorkSiteView>();
  readonly confirmDeleteSite = output<WorkSiteView>();
  readonly closeDeleteDialog = output<void>();
  readonly deleteSite = output<void>();

  readonly formNameChange = output<string>();
  readonly formAddressChange = output<string>();

  onNameChange(value: string): void {
    this.formNameChange.emit(value);
  }

  onAddressChange(value: string): void {
    this.formAddressChange.emit(value);
  }
}
