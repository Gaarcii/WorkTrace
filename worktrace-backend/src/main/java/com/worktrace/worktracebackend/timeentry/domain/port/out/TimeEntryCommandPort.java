package com.worktrace.worktracebackend.timeentry.domain.port.out;

import com.worktrace.worktracebackend.timeentry.domain.model.ClockEventCommand;
import com.worktrace.worktracebackend.timeentry.domain.model.ClockEventResult;

/**
 * Puerto de salida para las operaciones de escritura de fichajes.
 * <p>
 * Define las mutaciones sobre la persistencia que necesita el dominio de
 * fichajes, sin acoplarlo a la tecnología concreta (JPA). La implementación
 * reside en infraestructura y garantiza la atomicidad de cada operación.
 */
public interface TimeEntryCommandPort {

    /**
     * Registra un evento de fichaje de forma atómica.
     * <p>
     * Si el trabajador tiene una jornada abierta, la cierra con los datos del
     * evento; en caso contrario, abre una nueva jornada.
     *
     * @param command Orden con los datos del evento y los identificadores del
     *                trabajador y la empresa.
     * @return El {@link ClockEventResult} con el estado del fichaje afectado.
     */
    ClockEventResult registerClockEvent(ClockEventCommand command);
}
