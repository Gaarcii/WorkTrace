package com.worktrace.worktracebackend.infrastructure.out;

import com.worktrace.worktracebackend.dailyclosure.domain.port.out.CompanyQueryPort;
import com.worktrace.worktracebackend.repository.CompanyRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
public class CompanyJpaAdapter implements CompanyQueryPort {

    private final CompanyRepository companyRepository;

    public CompanyJpaAdapter(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public List<UUID> findAllCompanyIds() {
        return companyRepository.findAllIds();
    }
}
