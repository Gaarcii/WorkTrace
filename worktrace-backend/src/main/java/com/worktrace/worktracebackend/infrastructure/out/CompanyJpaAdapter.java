package com.worktrace.worktracebackend.infrastructure.out;

import com.worktrace.worktracebackend.dailyclosure.domain.port.out.CompanyQueryPort;
import com.worktrace.worktracebackend.repository.CompanyRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Adaptador de salida que implementa {@link CompanyQueryPort} sobre JPA.
 * <p>
 * Expone al dominio las consultas de solo lectura sobre empresas que necesita
 * el proceso de cierre diario (p. ej. para iterar todas las compañías), sin
 * acoplarlo a Spring Data.
 */
@Component
@Transactional(readOnly = true)
public class CompanyJpaAdapter implements CompanyQueryPort {

    private final CompanyRepository companyRepository;

    public CompanyJpaAdapter(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Devuelve los identificadores de todas las empresas registradas.
     *
     * @return La lista de identificadores de empresa.
     */
    @Override
    public List<UUID> findAllCompanyIds() {
        return companyRepository.findAllIds();
    }
}
