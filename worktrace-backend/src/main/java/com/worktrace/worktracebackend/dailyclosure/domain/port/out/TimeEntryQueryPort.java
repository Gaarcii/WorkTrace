package com.worktrace.worktracebackend.dailyclosure.domain.port.out;

import com.worktrace.worktracebackend.dailyclosure.domain.model.TimeEntrySnapshot;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Puerto de salida para consultar fichajes desde el dominio de cierre diario.
 * <p>
 * Proporciona las operaciones de solo lectura que necesita el cierre: validar
 * que no queden turnos abiertos y obtener los fichajes en el orden determinista
 * requerido para el cálculo del hash.
 */
public interface TimeEntryQueryPort {

    /**
     * Cuenta los fichajes aún abiertos (sin salida) de una empresa en una fecha.
     *
     * @param companyId Identificador de la empresa.
     * @param date      Fecha laboral a comprobar.
     * @return El número de fichajes en estado abierto.
     */
    long countOpenShifts(UUID companyId, LocalDate date);

    /**
     * Obtiene los fichajes de una empresa y fecha como flujo de snapshots, en el
     * orden determinista usado para calcular el hash del cierre.
     * <p>
     * El consumidor es responsable de cerrar el {@link Stream} devuelto.
     *
     * @param companyId Identificador de la empresa.
     * @param date      Fecha laboral del cierre.
     * @return Un flujo de {@link TimeEntrySnapshot} ordenados para el cierre.
     */
    Stream<TimeEntrySnapshot> findOrderedForClosure(UUID companyId, LocalDate date);
}
