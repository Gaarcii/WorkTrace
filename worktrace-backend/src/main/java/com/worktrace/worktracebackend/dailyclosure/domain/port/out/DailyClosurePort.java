package com.worktrace.worktracebackend.dailyclosure.domain.port.out;

import com.worktrace.worktracebackend.dailyclosure.domain.model.DailyClosureRecord;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistir y consultar los registros de cierre diario.
 * <p>
 * Abstrae el almacenamiento de los cierres, dando soporte al encadenamiento de
 * hashes entre días consecutivos sin acoplar el dominio a JPA.
 */
public interface DailyClosurePort {

    /**
     * Indica si ya existe un cierre para la empresa y fecha dadas.
     *
     * @param companyId Identificador de la empresa.
     * @param date      Fecha del cierre.
     * @return {@code true} si ya existe un cierre para esa empresa y fecha.
     */
    boolean existsForDate(UUID companyId, LocalDate date);

    /**
     * Obtiene el hash del último cierre anterior a la fecha indicada, para
     * encadenarlo con el nuevo cierre.
     *
     * @param companyId  Identificador de la empresa.
     * @param beforeDate Fecha límite; se busca el último cierre anterior a ella.
     * @return El hash del cierre previo, o vacío si no existe ninguno.
     */
    Optional<String> findPreviousHash(UUID companyId, LocalDate beforeDate);

    /**
     * Persiste un registro de cierre diario.
     *
     * @param record Datos del cierre a guardar.
     */
    void save(DailyClosureRecord record);

    /**
     * Busca el cierre de una empresa para una fecha concreta.
     *
     * @param companyId Identificador de la empresa.
     * @param date      Fecha del cierre.
     * @return El cierre encontrado, o vacío si no existe.
     */
    Optional<DailyClosureRecord> findByDate(UUID companyId, LocalDate date);
}
