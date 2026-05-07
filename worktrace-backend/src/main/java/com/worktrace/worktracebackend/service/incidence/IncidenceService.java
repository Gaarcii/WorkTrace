package com.worktrace.worktracebackend.service.incidence;

import com.worktrace.worktracebackend.dto.incidence.AdminIncidenceRequestDto;
import com.worktrace.worktracebackend.dto.incidence.AdminIncidenceResponseDto;
import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceRequestDto;
import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceResponseDto;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.IncidenceRepository;
import com.worktrace.worktracebackend.repository.IncidenceTypeRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncidenceService {

    private final IncidenceTypeRepository incidenceTypeRepository;
    private final IncidenceRepository incidenceRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<WorkerIncidenceResponseDto> getIncidencesByUserId() {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();

        List<Incidence> incidenceList = incidenceRepository
                .findByProfile_UserId(info.getUser().getId());

        return mapToWorkerIncidenceResponseDtos(incidenceList);
    }

    @Transactional
    public WorkerIncidenceResponseDto createIncidence(WorkerIncidenceRequestDto requestDto) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        IncidenceType incidenceType = incidenceTypeRepository
                .findByIdAndCompany_IdAndDeletedAtIsNull(requestDto.getTypeId(), info.getCompany().getId())
                .orElseThrow(() -> new IllegalStateException("Tipo de incidencia no encontrado"));

        Incidence incidence = new Incidence();
        incidence.setProfile(info.getProfile());
        incidence.setDate(requestDto.getAffectedDate());
        incidence.setComment(requestDto.getComment());
        incidence.setStatus(IncidenceStatus.PENDING);
        incidence.setCreatedAt(OffsetDateTime.now());
        incidence.setType(incidenceType);
        incidence.setIncidenceTime(requestDto.getTime());
        incidence.setCompany(info.getCompany());
        incidence.setUpdatedAt(OffsetDateTime.now());

        incidence = incidenceRepository.save(incidence);
        return new WorkerIncidenceResponseDto(
                incidence.getType().getName(),
                incidence.getDate(),
                incidence.getIncidenceTime(),
                incidence.getComment(),
                incidence.getStatus(),
                incidence.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<WorkerIncidenceResponseDto> getIncidencesByDateRange(LocalDate startDate, LocalDate endDate) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();

        List<Incidence> incidenceList = incidenceRepository
                .getIncidencesByProfile_UserIdAndDateBetween(
                        info.getUser().getId(),
                        startDate, endDate);

        return mapToWorkerIncidenceResponseDtos(incidenceList);
    }

    @Transactional(readOnly = true)
    public Page<AdminIncidenceResponseDto> getCompanyIncidencesByStatus(IncidenceStatus status, Pageable pageable) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();

        Page<Incidence> incidencePage = incidenceRepository
                .findByCompany_IdAndStatus(info.getCompany().getId(), status, pageable);

        return mapToAdminIncidenceResponseDtoPage(incidencePage);
    }

    @Transactional(readOnly = true)
    public Page<AdminIncidenceResponseDto> getCompanyIncidenceHistory(Pageable pageable) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();
        Page<Incidence> incidencePage = incidenceRepository.findByCompany_IdAndStatusIn
                (company.getId(), List.of(IncidenceStatus.RESOLVED, IncidenceStatus.REJECTED), pageable);

        return mapToAdminIncidenceResponseDtoPage(incidencePage);
    }

    @Transactional
    public void manageIncidence(UUID incidenceId, AdminIncidenceRequestDto dto) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();
        User user = info.getUser();

        Incidence incidence = incidenceRepository.findById(incidenceId)
                .orElseThrow(() -> new IllegalStateException("Incidencia no encontrada"));

        if (!incidence.getCompany().getId().equals(company.getId())) {
            throw new IllegalStateException("No tienes permisos sobre esta incidencia");
        }

        if (incidence.getStatus() == IncidenceStatus.PENDING) {
            incidence.setStatus(dto.getStatus());
            incidence.setUpdatedAt(OffsetDateTime.now());
            incidence.setResolvedBy(user);
            incidence.setAdminResponse(dto.getAdminResponse());

            incidenceRepository.save(incidence);
        } else {
            throw new IllegalStateException("La incidencia ya ha sido gestionada anteriormente");
        }
    }

    private List<WorkerIncidenceResponseDto> mapToWorkerIncidenceResponseDtos(List<Incidence> incidenceList) {
        return incidenceList.stream()
                .map(incident -> new WorkerIncidenceResponseDto(
                        incident.getType().getName(),
                        incident.getDate(),
                        incident.getIncidenceTime(),
                        incident.getComment(),
                        (incident.getStatus()),
                        incident.getCreatedAt()
                ))
                .toList();
    }

    private Page<AdminIncidenceResponseDto> mapToAdminIncidenceResponseDtoPage(Page<Incidence> incidencePage) {
        return incidencePage.map(incident -> new AdminIncidenceResponseDto(
                incident.getId(),
                incident.getProfile().getFullName(),
                getJobPositionTitle(incident),
                incident.getType().getName(),
                incident.getComment(),
                incident.getStatus(),
                incident.getDate(),
                incident.getCreatedAt(),
                incident.getProfile().getAvatarUrl(),
                incident.getAdminResponse()
        ));
    }

    private String getJobPositionTitle(Incidence incident) {
        if (incident.getProfile() == null || incident.getProfile().getPosition() == null) {
            return "Sin asignar";
        }
        return incident.getProfile().getPosition().getTitle();
    }
}
