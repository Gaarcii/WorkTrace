package com.worktrace.worktracebackend.timeentry.domain.port.in;

import com.worktrace.worktracebackend.timeentry.domain.model.DateRange;

import java.time.LocalDate;

/**
 * Caso de uso (puerto de entrada) que resuelve el rango de fechas efectivo de los
 * informes y estadísticas del trabajador autenticado cuando el cliente no aporta
 * todas las fechas.
 * <p>
 * Encapsula la regla de defaulting del rango, evitando que se repita en cada
 * endpoint: si falta la fecha de fin se usa el día de hoy; si falta la de inicio
 * se usa la del primer fichaje del trabajador (o hoy si todavía no tiene
 * ninguno).
 */
public interface ResolveEmployeeReportRangeUseCase {

    /**
     * Resuelve el rango de fechas efectivo aplicando los valores por defecto.
     *
     * @param startDate Fecha de inicio solicitada, o {@code null} para usar la
     *                  fecha del primer fichaje del trabajador.
     * @param endDate   Fecha de fin solicitada, o {@code null} para usar hoy.
     * @return Un {@link DateRange} con ambas fechas ya resueltas.
     */
    DateRange execute(LocalDate startDate, LocalDate endDate);
}
