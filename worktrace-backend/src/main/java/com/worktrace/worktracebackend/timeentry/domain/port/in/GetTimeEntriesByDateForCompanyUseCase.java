package com.worktrace.worktracebackend.timeentry.domain.port.in;

import com.worktrace.worktracebackend.shared.model.PageResult;
import com.worktrace.worktracebackend.timeentry.domain.model.AdminByDate;

import java.time.LocalDate;

/**
 * Caso de uso (puerto de entrada) que obtiene, de forma paginada, los fichajes de
 * todos los empleados de la empresa para una fecha concreta.
 * <p>
 * Pensado para la vista de administración por día. La consulta se filtra siempre
 * por la empresa del usuario autenticado, garantizando el aislamiento
 * multi-tenant.
 */
public interface GetTimeEntriesByDateForCompanyUseCase {

    /**
     * Obtiene una página de fichajes de la empresa para la fecha indicada.
     *
     * @param date Fecha de los fichajes a consultar.
     * @param page Índice de la página solicitada (base 0).
     * @param size Tamaño de página solicitado.
     * @return Una {@link PageResult} de {@link AdminByDate} con los fichajes de la
     * página y los metadatos de paginación.
     */
    PageResult<AdminByDate> execute(LocalDate date, int page, int size);
}
