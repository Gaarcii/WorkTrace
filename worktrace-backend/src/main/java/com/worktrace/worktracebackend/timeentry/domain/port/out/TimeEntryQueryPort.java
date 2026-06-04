package com.worktrace.worktracebackend.timeentry.domain.port.out;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Puerto de salida para las consultas de fichajes que necesitan los casos de
 * uso del dominio.
 * <p>
 * Define las operaciones de solo lectura sobre la persistencia, sin acoplar el
 * dominio a la tecnología concreta (JPA). La implementación reside en la capa
 * de infraestructura.
 */
public interface TimeEntryQueryPort {

    /**
     * Cuenta los fichajes de una empresa en una fecha concreta.
     *
     * @param companyId Identificador de la empresa (aislamiento multi-tenant).
     * @param date      Fecha sobre la que se realiza el recuento.
     * @return El número de fichajes registrados por la empresa en esa fecha.
     */
    Long countByCompanyAndDate(UUID companyId, LocalDate date);

    /**
     * Suma los minutos trabajados por una empresa en una fecha concreta.
     *
     * @param companyId Identificador de la empresa (aislamiento multi-tenant).
     * @param date      Fecha sobre la que se calculan los minutos trabajados.
     * @return El total de minutos trabajados por la empresa en esa fecha.
     */
    Long getWorkedMinutesByCompanyAndDate(UUID companyId, LocalDate date);

    /**
     * Obtiene la fecha del primer fichaje de un empleado.
     *
     * @param userId Identificador del usuario/empleado.
     * @return La fecha del primer fichaje registrado, o {@code null} si el
     * empleado todavía no tiene ningún fichaje.
     */
    LocalDate findFirstWorkDateByEmployee(UUID userId);
}
