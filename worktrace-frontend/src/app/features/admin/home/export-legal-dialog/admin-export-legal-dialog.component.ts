import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { FormatoExportacion, RangoExportacion } from '../admin-home.types';

@Component({
  selector: 'app-admin-export-legal-dialog',
  templateUrl: './admin-export-legal-dialog.component.html',
  styleUrls: ['./admin-export-legal-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminExportLegalDialogComponent {
  readonly modelValue = input.required<boolean>();
  readonly formatoExportacion = input.required<FormatoExportacion>();
  readonly rangoExportacion = input.required<RangoExportacion>();
  readonly cargandoExportacion = input.required<boolean>();

  readonly updateModelValue = output<boolean>();
  readonly updateFormatoExportacion = output<FormatoExportacion>();
  readonly updateRangoExportacion = output<RangoExportacion>();
  readonly confirm = output<void>();
  readonly cancelClicked = output<void>();

  onSelectFormato(formato: FormatoExportacion): void {
    this.updateFormatoExportacion.emit(formato);
  }

  onSelectRango(rango: RangoExportacion): void {
    this.updateRangoExportacion.emit(rango);
  }

  handleCancel(): void {
    this.updateModelValue.emit(false);
    this.cancelClicked.emit();
  }

  handleConfirm(): void {
    this.confirm.emit();
  }
}
