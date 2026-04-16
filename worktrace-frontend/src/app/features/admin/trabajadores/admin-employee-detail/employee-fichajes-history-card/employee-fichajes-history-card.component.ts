import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { FichajeTablaResponseDto } from '../../../../../shared/models/time-entry.model';
import { TableHeader } from '../../admin-trabajadores.types';

@Component({
  selector: 'app-employee-fichajes-history-card',
  templateUrl: './employee-fichajes-history-card.component.html',
  styleUrl: './employee-fichajes-history-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, NgClass],
})
export class EmployeeFichajesHistoryCardComponent {
  public readonly fichajeHeaders = input.required<TableHeader[]>();
  public readonly fichajes = input.required<FichajeTablaResponseDto[]>();
  public readonly loading = input.required<boolean>();

  public readonly getDireccionFichaje =
    input.required<(id: string, lat: number, lng: number, type: 'start' | 'end') => string>();
  public readonly calcularHoras =
    input.required<
      (start: string | undefined, end: string | null | undefined) => string | number
    >();

  public readonly editFichaje = output<FichajeTablaResponseDto>();
  public readonly deleteFichaje = output<FichajeTablaResponseDto>();

  public onEdit(item: FichajeTablaResponseDto): void {
    this.editFichaje.emit(item);
  }

  public onDelete(item: FichajeTablaResponseDto): void {
    if (!item.deleted_at) {
      this.deleteFichaje.emit(item);
    }
  }

  public getFecha(item: FichajeTablaResponseDto): string | undefined {
    return item.fecha || item.work_date;
  }

  public getEntrada(item: FichajeTablaResponseDto): string | undefined {
    return item.entrada || item.start_at;
  }

  public getSalida(item: FichajeTablaResponseDto): string | null | undefined {
    return item.salida || item.end_at;
  }

  public getLatitud(item: FichajeTablaResponseDto, type: 'start' | 'end'): number | null {
    return type === 'start'
      ? (item.latEntrada ?? item.start_lat ?? null)
      : (item.latSalida ?? item.end_lat ?? null);
  }

  public getLongitud(item: FichajeTablaResponseDto, type: 'start' | 'end'): number | null {
    return type === 'start'
      ? (item.lngEntrada ?? item.start_lng ?? null)
      : (item.lngSalida ?? item.end_lng ?? null);
  }
}
