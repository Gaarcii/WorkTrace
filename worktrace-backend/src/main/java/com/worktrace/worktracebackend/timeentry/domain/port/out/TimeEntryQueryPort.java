package com.worktrace.worktracebackend.timeentry.domain.port.out;

import com.worktrace.worktracebackend.timeentry.domain.model.ActiveTimeEntry;
import com.worktrace.worktracebackend.timeentry.domain.model.DailyEntryCount;
import com.worktrace.worktracebackend.timeentry.domain.model.LastTimeEntries;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

    /**
     * Cuenta los fichajes de una empresa agrupados por día dentro de un rango de
     * fechas.
     * <p>
     * Solo devuelve los días que tienen al menos un fichaje; los días sin
     * actividad no aparecen en el resultado.
     *
     * @param companyId Identificador de la empresa (aislamiento multi-tenant).
     * @param start     Fecha de inicio del rango (incluida).
     * @param end       Fecha de fin del rango (incluida).
     * @return Una lista de {@link DailyEntryCount} con el recuento por día,
     * únicamente para los días con fichajes.
     */
    List<DailyEntryCount> countByCompanyAndDateRange(UUID companyId, LocalDate start, LocalDate end);

    /**
     * Obtiene los fichajes actualmente abiertos (sin hora de salida) de una
     * empresa, junto con los datos de perfil de cada trabajador.
     *
     * @param companyId Identificador de la empresa (aislamiento multi-tenant).
     * @return Una lista de {@link ActiveTimeEntry}, una por cada jornada en curso;
     * lista vacía si no hay ninguna.
     */
    List<ActiveTimeEntry> findActiveByCompany(UUID companyId);

    /**
     * Obtiene los últimos eventos de fichaje de un empleado.
     * <p>
     * Cada fichaje puede aportar hasta dos eventos (entrada y salida); el
     * resultado se limita a los 5 más recientes.
     *
     * @param userId Identificador del usuario/empleado.
     * @return Una lista de hasta 5 {@link LastTimeEntries} en orden cronológico
     * inverso.
     */
    List<LastTimeEntries> findTop5ByEmployee(UUID userId);

    /**
     * Suma los minutos trabajados por un empleado en una fecha concreta.
     *
     * @param userId Identificador del usuario/empleado.
     * @param date   Fecha sobre la que se calculan los minutos trabajados.
     * @return El total de minutos trabajados por el empleado en esa fecha.
     */
    long getWorkedMinutesByEmployeeAndDate(UUID userId, LocalDate date);

    /**
     * Obtiene la jornada actualmente abierta (sin hora de salida) de un empleado.
     *
     * @param userId Identificador del usuario/empleado.
     * @return Un {@link Optional} con el {@link ActiveTimeEntry} en curso, o
     * vacío si el empleado no tiene ninguna jornada abierta.
     */
    Optional<ActiveTimeEntry> findOpenByEmployee(UUID userId);

    /**
     * Obtiene los eventos de fichaje de un empleado en una fecha concreta.
     * <p>
     * Cada fichaje del día se descompone en sus eventos (entrada y, si existe,
     * salida), ordenados cronológicamente de forma descendente.
     *
     * @param userId Identificador del usuario/empleado.
     * @param date   Fecha cuyos eventos se consultan.
     * @return Una lista de {@link LastTimeEntries} con los eventos del día.
     */
    List<LastTimeEntries> findEventsByEmployeeAndDate(UUID userId, LocalDate date);

    /**
     * Suma los minutos trabajados por un empleado dentro de un rango de fechas.
     *
     * @param userId Identificador del usuario/empleado.
     * @param start  Fecha de inicio del rango (incluida).
     * @param end    Fecha de fin del rango (incluida).
     * @return El total de minutos trabajados por el empleado en el rango.
     */
    long getWorkedMinutesByEmployeeAndDateRange(UUID userId, LocalDate start, LocalDate end);

}
