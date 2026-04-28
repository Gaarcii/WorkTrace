import { Component, ChangeDetectionStrategy, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { FormsModule } from '@angular/forms';

export type ExportFormat = 'PDF' | 'EXCEL';
export type ExportPeriod = 'TODAY' | 'MONTH' | 'YEAR' | 'ALL';

export interface ExportEvent {
  format: ExportFormat;
  period: ExportPeriod;
}

@Component({
  selector: 'app-inspector-export-card',
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatCardModule,
    FormsModule,
  ],
  templateUrl: './inspector-export-card.component.html',
  styleUrls: ['./inspector-export-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InspectorExportCardComponent {
  readonly isExporting = input<boolean>(false);
  readonly export = output<ExportEvent>();

  readonly selectedFormat = signal<ExportFormat>('PDF');
  readonly selectedPeriod = signal<ExportPeriod>('MONTH');

  setFormat(format: ExportFormat): void {
    if (this.isExporting()) return;
    this.selectedFormat.set(format);
  }

  setPeriod(period: ExportPeriod): void {
    if (this.isExporting()) return;
    this.selectedPeriod.set(period);
  }

  onExport(): void {
    this.export.emit({
      format: this.selectedFormat(),
      period: this.selectedPeriod(),
    });
  }
}
