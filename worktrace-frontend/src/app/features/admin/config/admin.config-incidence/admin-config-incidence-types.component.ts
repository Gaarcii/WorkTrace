import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { IncidenceTypeView } from '../admin-config.types';

@Component({
  selector: 'app-admin-config-incidence-types',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './admin-config-incidence-types.component.html',
  styleUrl: './admin-config-incidence-types.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminConfigIncidenceTypesComponent {
  readonly loadingInitial = input.required<boolean>();
  readonly loading = input.required<boolean>();
  readonly incidenceTypes = input.required<IncidenceTypeView[]>();
  readonly updatingId = input<string | null>(null);
  readonly deletingId = input<string | null>(null);
  readonly newTypeName = input.required<string>();

  readonly deleteDialogOpen = input.required<boolean>();
  readonly typeToDelete = input<IncidenceTypeView | null>(null);
  readonly editingId = signal<string | null>(null);
  readonly editingName = signal<string>('');

  readonly newTypeNameChange = output<string>();
  readonly createType = output<void>();
  readonly updateType = output<{ id: string; name: string }>();
  readonly confirmDelete = output<IncidenceTypeView>();
  readonly closeDeleteDialog = output<void>();
  readonly deleteType = output<void>();

  onTypeNameChange(value: string): void {
    this.newTypeNameChange.emit(value);
  }

  startEdit(type: IncidenceTypeView): void {
    this.editingId.set(type.id);
    this.editingName.set(type.name);
  }

  cancelEdit(): void {
    this.editingId.set(null);
    this.editingName.set('');
  }

  onEditNameChange(value: string): void {
    this.editingName.set(value);
  }

  saveEdit(type: IncidenceTypeView): void {
    const name = this.editingName().trim();

    if (!name || name === type.name) {
      this.cancelEdit();
      return;
    }

    this.updateType.emit({ id: type.id, name });
    this.cancelEdit();
  }
}
