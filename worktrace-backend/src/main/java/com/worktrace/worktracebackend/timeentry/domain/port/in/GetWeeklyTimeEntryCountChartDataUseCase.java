package com.worktrace.worktracebackend.timeentry.domain.port.in;

import com.worktrace.worktracebackend.timeentry.domain.model.DailyEntryCount;

import java.time.LocalDate;
import java.util.List;

/**
 * Caso de uso (puerto de entrada) que obtiene el recuento de fichajes por día en
 * un rango de fechas, para construir el gráfico semanal del panel de
 * administración.
 * <p>
 * El recuento se calcula siempre sobre la empresa del usuario autenticado,
 * garantizando el aislamiento multi-tenant.
 */
public interface GetWeeklyTimeEntryCountChartDataUseCase {

    /**
     * Calcula el número de fichajes por día dentro del rango indicado.
     * <p>
     * La lista resultante incluye <strong>todos</strong> los días del rango, con
     * recuento {@code 0} para los días sin fichajes, de modo que el gráfico no
     * presente huecos.
     *
     * @param startDate Fecha de inicio del rango (incluida).
     * @param endDate   Fecha de fin del rango (incluida).
     * @return Una lista de {@link DailyEntryCount}, un elemento por día del rango
     * en orden cronológico.
     */
    List<DailyEntryCount> execute(LocalDate startDate, LocalDate endDate);
}
