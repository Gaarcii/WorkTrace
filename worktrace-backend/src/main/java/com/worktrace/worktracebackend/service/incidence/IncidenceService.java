package com.worktrace.worktracebackend.service.incidence;

import com.worktrace.worktracebackend.dto.incidence.AdminIncidenceRequestDto;
import com.worktrace.worktracebackend.dto.incidence.AdminIncidenceResponseDto;
import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceRequestDto;
import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceResponseDto;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.IncidenceRepository;
import com.worktrace.worktracebackend.repository.IncidenceTypeRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UsuarioYCompaniaInfo;
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
    public List<WorkerIncidenceResponseDto> getIncidenciasByUserID() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();

        List<Incidence> incidenceList = incidenceRepository
                .findByProfile_UserId(info.getUser().getId());

        return mapearAIncidenceResponse(incidenceList);
    }

    @Transactional
    public WorkerIncidenceResponseDto postIncidence(WorkerIncidenceRequestDto requestDto) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        IncidenceType tipoRef = incidenceTypeRepository
                .findByIdAndCompany_IdAndDeletedAtIsNull(requestDto.getTypeId(), info.getCompany().getId())
                .orElseThrow(() -> new IllegalStateException("Tipo de incidencia no encontrado"));

        Incidence incidence = new Incidence();
        incidence.setProfile(info.getProfile());
        incidence.setDate(requestDto.getFechaAfectada());
        incidence.setComment(requestDto.getComentario());
        incidence.setStatus(EstadoIncidencia.PENDING);
        incidence.setCreatedAt(OffsetDateTime.now());
        incidence.setType(tipoRef);
        incidence.setIncidenceTime(requestDto.getHora());
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
    public List<WorkerIncidenceResponseDto> getIncidenciaPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();

        List<Incidence> incidenceList = incidenceRepository
                .getIncidencesByProfile_UserIdAndDateBetween(
                        info.getUser().getId(),
                        fechaInicio, fechaFin);

        return mapearAIncidenceResponse(incidenceList);
    }

    @Transactional(readOnly = true)
    public Page<AdminIncidenceResponseDto> getIncidenciasByCompanyAndStatus(EstadoIncidencia status, Pageable pageable) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();

        Page<Incidence> incidencePage = incidenceRepository
                .findByCompany_IdAndStatus(info.getCompany().getId(), status, pageable);

        return mapearAdminResponse(incidencePage);
    }

    @Transactional(readOnly = true)
    public Page<AdminIncidenceResponseDto> getIncidenciasByCompanyStatusIn(Pageable pageable) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();
        Page<Incidence> incidencePage = incidenceRepository.findByCompany_IdAndStatusIn
                (company.getId(), List.of(EstadoIncidencia.RESOLVED, EstadoIncidencia.REJECTED), pageable);

        return mapearAdminResponse(incidencePage);
    }

    @Transactional
    public void gestionarIncidencia(UUID incidenciaId, AdminIncidenceRequestDto dto) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();
        User user = info.getUser();

        Incidence incidence = incidenceRepository.findById(incidenciaId)
                .orElseThrow(() -> new IllegalStateException("Incidencia no encontrada"));

        if (!incidence.getCompany().getId().equals(company.getId())) {
            throw new IllegalStateException("No tienes permisos sobre esta incidencia");
        }

        if (incidence.getStatus() == EstadoIncidencia.PENDING) {
            incidence.setStatus(dto.getEstado());
            incidence.setUpdatedAt(OffsetDateTime.now());
            incidence.setResolvedBy(user);
            incidence.setAdminResponse(dto.getRespuestaAdmin());

            incidenceRepository.save(incidence);
        } else {
            throw new IllegalStateException("La incidencia ya ha sido gestionada anteriormente");
        }
    }

    private List<WorkerIncidenceResponseDto> mapearAIncidenceResponse(List<Incidence> incidenceList) {
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

    private Page<AdminIncidenceResponseDto> mapearAdminResponse(Page<Incidence> incidencePage) {
        return incidencePage.map(incident -> new AdminIncidenceResponseDto(
                incident.getId(),
                incident.getProfile().getFullName(),
                obtenerPuestoTrabajo(incident),
                incident.getType().getName(),
                incident.getComment(),
                incident.getStatus(),
                incident.getDate(),
                incident.getCreatedAt(),
                incident.getProfile().getAvatarUrl(),
                incident.getAdminResponse()
        ));
    }

    private String obtenerPuestoTrabajo(Incidence incident) {
        if (incident.getProfile() == null || incident.getProfile().getPosition() == null) {
            return "Sin asignar";
        }
        return incident.getProfile().getPosition().getTitle();
    }
}
