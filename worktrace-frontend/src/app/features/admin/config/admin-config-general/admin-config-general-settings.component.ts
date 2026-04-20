import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CompanyFormData } from '../admin-config.types';

@Component({
  selector: 'app-admin-config-general-settings',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './admin-config-general-settings.component.html',
  styleUrl: './admin-config-general-settings.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminConfigGeneralSettingsComponent {
  readonly isLoading = input.required<boolean>();
  readonly isSaving = input.required<boolean>();
  readonly loadError = input<string | null>(null);
  readonly logoPreview = input<string | null>(null);
  readonly formData = input.required<CompanyFormData>();

  readonly companyNameChange = output<string>();
  readonly cifChange = output<string>();
  readonly fileSelected = output<Event>();
  readonly removeLogo = output<void>();
  readonly saveSettings = output<void>();

  onCompanyNameChange(value: string): void {
    this.companyNameChange.emit(value);
  }

  onCifChange(value: string): void {
    this.cifChange.emit(value);
  }

  onFileChange(event: Event): void {
    this.fileSelected.emit(event);
  }

  onRemoveLogoClick(): void {
    this.removeLogo.emit();
  }

  onSave(): void {
    this.saveSettings.emit();
  }
}
