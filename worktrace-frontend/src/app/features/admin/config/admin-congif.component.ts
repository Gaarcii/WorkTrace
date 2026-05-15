import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { take } from 'rxjs/operators';
import { AdminConfigService } from '../../../shared/services/admin/admin-config.service';
import { WorkSiteRequestDto } from '../../../shared/models/work-site.model';
import {
  AdminConfigSectionItem,
  AdminConfigSectionKey,
  CompanyFormData,
  IncidenceTypeView,
  UiMessageState,
  WorkSiteView,
} from './admin-config.types';
import { AdminConfigHeaderComponent } from './admin-config-header/admin-config-header.component';
import { AdminConfigGeneralSettingsComponent } from './admin-config-general/admin-config-general-settings.component';
import { AdminConfigWorkSitesComponent } from './admin-config-work-sites/admin-config-work-sites.component';
import { AdminConfigIncidenceTypesComponent } from './admin.config-incidence/admin-config-incidence-types.component';

@Component({
  selector: 'app-admin-congif.component',
  standalone: true,
  imports: [
    AdminConfigHeaderComponent,
    AdminConfigGeneralSettingsComponent,
    AdminConfigWorkSitesComponent,
    AdminConfigIncidenceTypesComponent,
  ],
  templateUrl: './admin-congif.component.html',
  styleUrl: './admin-congif.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminCongifComponent implements OnInit {
  private readonly adminConfigService = inject(AdminConfigService);

  readonly activeSection = signal<AdminConfigSectionKey>('general');
  readonly sections = signal<AdminConfigSectionItem[]>([
    { key: 'general', label: 'General' },
    { key: 'sedes', label: 'Sedes' },
    { key: 'incidencias', label: 'Incidencias' },
  ]);

  readonly companyLoading = signal<boolean>(false);
  readonly companySaving = signal<boolean>(false);
  readonly companyLoadError = signal<string | null>(null);
  readonly companyFormData = signal<CompanyFormData>({ name: '', cif: '' });
  readonly companyLogoPreview = signal<string | null>(null);
  readonly selectedLogoFile = signal<File | null>(null);

  readonly workSitesLoading = signal<boolean>(false);
  readonly workSiteDialogCreateOpen = signal<boolean>(false);
  readonly workSiteDialogDeleteOpen = signal<boolean>(false);
  readonly workSiteSaving = signal<boolean>(false);
  readonly workSiteDeleting = signal<boolean>(false);
  readonly workSiteErrorMessage = signal<string>('');
  readonly editingWorkSite = signal<WorkSiteView | null>(null);
  readonly workSiteToDelete = signal<WorkSiteView | null>(null);
  readonly workSiteFormData = signal<WorkSiteRequestDto>({ name: '', address: '' });

  readonly incidenceTypesLoadingInitial = signal<boolean>(false);
  readonly incidenceTypesLoading = signal<boolean>(false);
  readonly incidenceTypeUpdatingId = signal<string | null>(null);
  readonly incidenceTypeDeletingId = signal<string | null>(null);
  readonly incidenceTypeDialogDeleteOpen = signal<boolean>(false);
  readonly incidenceTypeToDelete = signal<IncidenceTypeView | null>(null);
  readonly newIncidenceTypeName = signal<string>('');

  readonly uiMessage = signal<UiMessageState>({
    show: false,
    message: '',
    color: 'success',
  });

  ngOnInit(): void {
    this.loadCompanyData();
    this.loadWorkSites();
    this.loadIncidenceTypes();
  }

  setActiveSection(section: AdminConfigSectionKey): void {
    this.activeSection.set(section);
  }

  onCompanyNameChange(value: string): void {
    this.companyFormData.update((current) => ({
      ...current,
      name: value,
    }));
  }

  onCompanyCifChange(value: string): void {
    this.companyFormData.update((current) => ({
      ...current,
      cif: value,
    }));
  }

  onLogoSelected(event: Event): void {
    const input = event.target as HTMLInputElement | null;
    const file = input?.files?.[0];

    if (!file) {
      return;
    }

    if (file.size > 2 * 1024 * 1024) {
      this.showMessage('El archivo es demasiado grande. Maximo 2MB.', 'error');
      if (input) {
        input.value = '';
      }
      return;
    }

    this.selectedLogoFile.set(file);
    this.companyLogoPreview.set(URL.createObjectURL(file));
  }

  saveCompanySettings(): void {
    const data = this.companyFormData();
    const companyName = data.name.trim();
    const cif = data.cif.trim();

    if (!companyName) {
      this.showMessage('El nombre de la empresa es obligatorio', 'error');
      return;
    }

    if (!cif || cif.length !== 9) {
      this.showMessage('El CIF/NIF debe tener 9 caracteres', 'error');
      return;
    }

    this.companySaving.set(true);

    this.adminConfigService
      .updateCompanyData({ companyName, cif })
      .pipe(
        switchMap(() => {
          const logoFile = this.selectedLogoFile();
          return logoFile ? this.adminConfigService.updateCompanyLogo(logoFile) : of(null);
        }),
        take(1),
      )
      .subscribe({
        next: (logoResponse) => {
          if (logoResponse?.logoUrl) {
            this.companyLogoPreview.set(logoResponse.logoUrl);
            this.selectedLogoFile.set(null);
          }

          this.companySaving.set(false);
          this.showMessage('Configuracion guardada correctamente', 'success');
        },
        error: (error: { error?: { message?: string } }) => {
          this.companySaving.set(false);
          this.showMessage(error?.error?.message ?? 'Error al guardar la configuracion', 'error');
        },
      });
  }

  openCreateWorkSiteDialog(): void {
    this.editingWorkSite.set(null);
    this.workSiteFormData.set({ name: '', address: '' });
    this.workSiteErrorMessage.set('');
    this.workSiteDialogCreateOpen.set(true);
  }

  openEditWorkSiteDialog(site: WorkSiteView): void {
    this.editingWorkSite.set(site);
    this.workSiteFormData.set({
      name: site.name,
      address: site.address ?? '',
    });
    this.workSiteErrorMessage.set('');
    this.workSiteDialogCreateOpen.set(true);
  }

  closeWorkSiteDialog(): void {
    this.workSiteDialogCreateOpen.set(false);
    this.editingWorkSite.set(null);
    this.workSiteFormData.set({ name: '', address: '' });
    this.workSiteErrorMessage.set('');
  }

  onWorkSiteNameChange(value: string): void {
    this.workSiteFormData.update((current) => ({
      ...current,
      name: value,
    }));
  }

  onWorkSiteAddressChange(value: string): void {
    this.workSiteFormData.update((current) => ({
      ...current,
      address: value,
    }));
  }

  saveWorkSite(): void {
    const dto = {
      name: this.workSiteFormData().name.trim(),
      address: this.workSiteFormData().address.trim(),
    };

    if (!dto.name || !dto.address) {
      this.workSiteErrorMessage.set('Todos los campos son obligatorios');
      return;
    }

    this.workSiteSaving.set(true);
    this.workSiteErrorMessage.set('');

    const editing = this.editingWorkSite();
    const request = editing
      ? this.adminConfigService.updateWorkSite(editing.id, dto)
      : this.adminConfigService.createWorkSite(dto);

    request.pipe(take(1)).subscribe({
      next: () => {
        this.workSiteSaving.set(false);
        this.closeWorkSiteDialog();
        this.showMessage(
          editing ? 'Sede actualizada correctamente' : 'Sede creada correctamente',
          'success',
        );
      },
      error: (error: { error?: { message?: string } }) => {
        this.workSiteSaving.set(false);
        this.workSiteErrorMessage.set(error?.error?.message ?? 'Error al guardar la sede');
      },
    });
  }

  openDeleteWorkSiteDialog(site: WorkSiteView): void {
    this.workSiteToDelete.set(site);
    this.workSiteDialogDeleteOpen.set(true);
  }

  closeDeleteWorkSiteDialog(): void {
    this.workSiteDialogDeleteOpen.set(false);
    this.workSiteToDelete.set(null);
  }

  deleteWorkSite(): void {
    const site = this.workSiteToDelete();
    if (!site) {
      return;
    }

    this.workSiteDeleting.set(true);

    this.adminConfigService
      .deleteWorkSite(site.id)
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.workSiteDeleting.set(false);
          this.closeDeleteWorkSiteDialog();
          this.showMessage('Sede eliminada correctamente', 'success');
        },
        error: (error: { error?: { message?: string } }) => {
          this.workSiteDeleting.set(false);
          this.showMessage(error?.error?.message ?? 'Error al eliminar la sede', 'error');
        },
      });
  }

  onNewIncidenceTypeNameChange(value: string): void {
    this.newIncidenceTypeName.set(value);
  }

  createIncidenceType(): void {
    const name = this.newIncidenceTypeName().trim();

    if (!name) {
      return;
    }

    this.incidenceTypesLoading.set(true);

    this.adminConfigService
      .createIncidenceType({ name })
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.incidenceTypesLoading.set(false);
          this.incidenceTypeUpdatingId.set(null);
          this.newIncidenceTypeName.set('');
          this.showMessage('Tipo de incidencia creado correctamente', 'success');
        },
        error: (error: { error?: { message?: string } }) => {
          this.incidenceTypesLoading.set(false);
          this.incidenceTypeUpdatingId.set(null);
          this.showMessage(error?.error?.message ?? 'Error al crear el tipo', 'error');
        },
      });
  }

  updateIncidenceType(payload: { id: string; name: string }): void {
    const name = payload.name.trim();

    if (!name) {
      this.showMessage('El nombre del tipo de incidencia es obligatorio', 'error');
      return;
    }

    this.incidenceTypesLoading.set(true);
    this.incidenceTypeUpdatingId.set(payload.id);

    this.adminConfigService
      .updateIncidenceType(payload.id, { name })
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.incidenceTypesLoading.set(false);
          this.incidenceTypeUpdatingId.set(null);
          this.showMessage('Tipo de incidencia actualizado correctamente', 'success');
        },
        error: (error: { error?: { message?: string } }) => {
          this.incidenceTypesLoading.set(false);
          this.incidenceTypeUpdatingId.set(null);
          this.showMessage(error?.error?.message ?? 'Error al actualizar el tipo', 'error');
        },
      });
  }

  openDeleteIncidenceTypeDialog(type: IncidenceTypeView): void {
    this.incidenceTypeToDelete.set(type);
    this.incidenceTypeDialogDeleteOpen.set(true);
  }

  closeDeleteIncidenceTypeDialog(): void {
    this.incidenceTypeDialogDeleteOpen.set(false);
    this.incidenceTypeToDelete.set(null);
    this.incidenceTypeDeletingId.set(null);
  }

  deleteIncidenceType(): void {
    const type = this.incidenceTypeToDelete();
    if (!type) {
      return;
    }

    this.incidenceTypeDeletingId.set(type.id);
    this.incidenceTypesLoading.set(true);

    this.adminConfigService
      .deleteIncidenceType(type.id)
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.incidenceTypesLoading.set(false);
          this.incidenceTypeUpdatingId.set(null);
          this.incidenceTypeDeletingId.set(null);
          this.closeDeleteIncidenceTypeDialog();
          this.showMessage('Tipo de incidencia eliminado correctamente', 'success');
        },
        error: (error: { error?: { message?: string } }) => {
          this.incidenceTypesLoading.set(false);
          this.incidenceTypeUpdatingId.set(null);
          this.incidenceTypeDeletingId.set(null);
          this.showMessage(error?.error?.message ?? 'Error al eliminar el tipo', 'error');
        },
      });
  }

  closeMessage(): void {
    this.uiMessage.set({
      show: false,
      message: '',
      color: 'success',
    });
  }

  workSites(): WorkSiteView[] {
    return this.adminConfigService.workSitesSignal();
  }

  incidenceTypes(): IncidenceTypeView[] {
    return this.adminConfigService.incidenceTypesSignal();
  }

  private loadWorkSites(): void {
    this.workSitesLoading.set(true);

    this.adminConfigService
      .getWorkSites()
      .pipe(take(1))
      .subscribe({
        next: () => this.workSitesLoading.set(false),
        error: () => {
          this.workSitesLoading.set(false);
          this.showMessage('No se pudieron cargar las sedes', 'error');
        },
      });
  }

  private loadCompanyData(): void {
    this.companyLoading.set(true);
    this.companyLoadError.set(null);

    this.adminConfigService
      .getCompanyData()
      .pipe(take(1))
      .subscribe({
        next: (company) => {
          this.companyFormData.set({
            name: company.companyName ?? '',
            cif: company.cif ?? '',
          });
          this.companyLogoPreview.set(company.logoUrl ?? null);
          this.selectedLogoFile.set(null);
          this.companyLoading.set(false);
        },
        error: (error: { error?: { message?: string } }) => {
          this.companyLoading.set(false);
          this.companyLoadError.set(
            error?.error?.message ?? 'No se pudo cargar la configuracion de la empresa',
          );
        },
      });
  }

  private loadIncidenceTypes(): void {
    this.incidenceTypesLoadingInitial.set(true);

    this.adminConfigService
      .getIncidenceTypes()
      .pipe(take(1))
      .subscribe({
        next: () => this.incidenceTypesLoadingInitial.set(false),
        error: () => {
          this.incidenceTypesLoadingInitial.set(false);
          this.showMessage('No se pudieron cargar los tipos de incidencia', 'error');
        },
      });
  }

  private showMessage(message: string, color: 'success' | 'error'): void {
    this.uiMessage.set({
      show: true,
      message,
      color,
    });
  }
}
