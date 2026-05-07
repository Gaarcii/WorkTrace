package com.worktrace.worktracebackend.service.incidenceType;

import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeItemDto;
import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeRequestDto;
import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeResponseDto;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.IncidenceType;
import com.worktrace.worktracebackend.repository.IncidenceRepository;
import com.worktrace.worktracebackend.repository.IncidenceTypeRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncidenceTypeService {
    private final IncidenceTypeRepository incidenceTypeRepository;
    private final IncidenceRepository incidenceRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public IncidenceTypeResponseDto getIncidenceTypes() {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        IncidenceTypeResponseDto typeResponseDto = new IncidenceTypeResponseDto();
        typeResponseDto.setTypes(
                incidenceTypeRepository.findByCompany_IdAndDeletedAtIsNull(
                        info.getCompany().getId()));

        return typeResponseDto;
    }

    @Transactional
    public IncidenceTypeItemDto createIncidenceType(IncidenceTypeRequestDto dto) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        String name = dto.getName().trim();
        IncidenceType existing = incidenceTypeRepository
                .findByNameIgnoreCaseAndCompany_Id(name, company.getId())
                .orElse(null);

        if (existing != null) {
            if (existing.getDeletedAt() == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ya existe un tipo de incidencia con ese nombre"
                );
            }

            existing.setDeletedAt(null);
            existing.setName(name);
            IncidenceType reactivated = incidenceTypeRepository.save(existing);
            return new IncidenceTypeItemDto(reactivated.getId(), reactivated.getName());
        }

        IncidenceType incidenceType = new IncidenceType();
        incidenceType.setName(name);
        incidenceType.setCompany(company);
        incidenceType.setCreatedAt(OffsetDateTime.now());

        IncidenceType saved = incidenceTypeRepository.save(incidenceType);
        return new IncidenceTypeItemDto(saved.getId(), saved.getName());
    }

    @Transactional
    public IncidenceTypeItemDto updateIncidenceType(UUID id, IncidenceTypeRequestDto dto) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        IncidenceType incidenceType = incidenceTypeRepository.findByIdAndCompany_IdAndDeletedAtIsNull(id, company.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El tipo de incidencia no existe o no pertenece a tu empresa"
                ));

        String name = dto.getName().trim();
        incidenceTypeRepository.findByNameIgnoreCaseAndCompany_Id(name, company.getId())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Ya existe un tipo de incidencia con ese nombre"
                    );
                });

        incidenceType.setName(name);
        IncidenceType updated = incidenceTypeRepository.save(incidenceType);

        return new IncidenceTypeItemDto(updated.getId(), updated.getName());
    }

    @Transactional
    public void deleteIncidenceType(UUID id) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        IncidenceType incidenceType = incidenceTypeRepository.findByIdAndCompany_IdAndDeletedAtIsNull(id, company.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El tipo de incidencia no existe o no pertenece a tu empresa"
                ));

        boolean hasIncidences = incidenceRepository.existsByType_IdAndCompany_Id(incidenceType.getId(), company.getId());

        if (hasIncidences) {
            incidenceType.setDeletedAt(OffsetDateTime.now());
            incidenceTypeRepository.save(incidenceType);
        } else {
            incidenceTypeRepository.delete(incidenceType);
        }
    }
}
