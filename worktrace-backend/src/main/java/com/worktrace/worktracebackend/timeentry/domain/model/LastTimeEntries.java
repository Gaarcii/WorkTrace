package com.worktrace.worktracebackend.timeentry.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Evento individual de fichaje (una entrada o una salida) dentro del historial
 * reciente de un trabajador.
 * <p>
 * Un mismo fichaje puede generar dos eventos: uno de "Entrada" y otro de
 * "Salida", de ahí que varios eventos compartan el mismo {@code timeEntryId}. Se
 * usa en el resumen diario para mostrar los últimos movimientos del trabajador.
 *
 * @param timeEntryId Identificador del fichaje al que pertenece el evento.
 * @param eventType   Tipo de evento ("Entrada" o "Salida").
 * @param date        Marca temporal del evento.
 */
public record LastTimeEntries(
        UUID timeEntryId,
        String eventType,
        OffsetDateTime date
) {
}
