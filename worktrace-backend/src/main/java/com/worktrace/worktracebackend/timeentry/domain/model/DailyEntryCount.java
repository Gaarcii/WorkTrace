package com.worktrace.worktracebackend.timeentry.domain.model;

import java.time.LocalDate;

/**
 * Recuento de fichajes de un día concreto.
 * <p>
 * Modelo de dominio usado para construir el gráfico semanal de fichajes: cada
 * instancia asocia una fecha con el número de fichajes registrados ese día.
 *
 * @param date  Día al que corresponde el recuento.
 * @param count Número de fichajes registrados ese día.
 */
public record DailyEntryCount(
        LocalDate date,
        Long count
) {
}
