package com.worktrace.worktracebackend.timeentry.domain.port.out;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para consultar datos del perfil del trabajador desde el
 * dominio de fichajes.
 * <p>
 * Aporta la información de perfil necesaria para los cálculos de historial (p. ej.
 * las horas contratadas), sin acoplar el dominio a la persistencia.
 */
public interface ProfileQueryPort {

    /**
     * Obtiene las horas semanales contratadas de un trabajador.
     *
     * @param userId Identificador del usuario/empleado.
     * @return Un {@link Optional} con las horas semanales contratadas, o vacío si
     * el perfil no las tiene definidas.
     */
    Optional<BigDecimal> findWeeklyHoursByEmployee(UUID userId);
}
