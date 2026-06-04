package com.worktrace.worktracebackend.timeentry.domain.port.in;

import java.time.LocalDate;

/**
 * Caso de uso (puerto de entrada) que obtiene la fecha del primer fichaje
 * registrado por el trabajador autenticado.
 * <p>
 * Se utiliza como valor por defecto del inicio de rango en las consultas de
 * estadísticas e informes cuando el cliente no especifica una fecha de inicio.
 */
public interface GetFirstTimeEntryDateForEmployeeUseCase {

    /**
     * Calcula la fecha del primer fichaje del trabajador autenticado.
     *
     * @return La fecha del primer fichaje, o la fecha actual si el trabajador
     * todavía no tiene ningún fichaje registrado.
     */
    LocalDate execute();
}
