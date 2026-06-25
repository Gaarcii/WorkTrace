package com.worktrace.worktracebackend.timeentry.domain.port.in;

import com.worktrace.worktracebackend.shared.model.PageResult;
import com.worktrace.worktracebackend.timeentry.domain.model.TimeEntryRow;

import java.util.UUID;

/**
 * Caso de uso (puerto de entrada) que obtiene, de forma paginada, los fichajes de
 * un empleado concreto.
 * <p>
 * Pensado para la tabla de fichajes del panel de administración. La consulta se
 * filtra siempre por la empresa del usuario autenticado, garantizando el
 * aislamiento multi-tenant.
 */
public interface GetTimeEntriesByEmployeeUseCase {

    /**
     * Obtiene una página de fichajes del empleado indicado.
     *
     * @param employeeId Identificador del empleado.
     * @param page       Índice de la página solicitada (base 0).
     * @param size       Tamaño de página solicitado.
     * @return Una {@link PageResult} de {@link TimeEntryRow} con los fichajes de
     * la página y los metadatos de paginación.
     */
    PageResult<TimeEntryRow> execute(UUID employeeId, int page, int size);
}
