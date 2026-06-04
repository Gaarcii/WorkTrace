package com.worktrace.worktracebackend.dailyclosure.domain.port.out;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida para consultar empresas desde el dominio de cierre diario.
 * <p>
 * Permite obtener todas las compañías sobre las que iterar durante el cierre
 * diario global, sin acoplar el dominio a la persistencia.
 */
public interface CompanyQueryPort {

    /**
     * Devuelve los identificadores de todas las empresas registradas.
     *
     * @return La lista de identificadores de empresa.
     */
    List<UUID> findAllCompanyIds();
}
