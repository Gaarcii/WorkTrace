package com.worktrace.worktracebackend.service.incidence;

import com.worktrace.worktracebackend.dto.incidence.IncidenceRequestDto;
import com.worktrace.worktracebackend.dto.incidence.IncidenceResponseDto;
import com.worktrace.worktracebackend.model.EstadoIncidencia;
import com.worktrace.worktracebackend.model.IncidenceType;
import com.worktrace.worktracebackend.model.Incident;
import com.worktrace.worktracebackend.repository.IncidenceRepository;
import com.worktrace.worktracebackend.repository.IncidenceTypeRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UsuarioYCompaniaInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidenceService {

    private final IncidenceTypeRepository incidenceTypeRepository;
    private final IncidenceRepository incidenceRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<IncidenceResponseDto> getIncidenciasByUserID() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();

        List<Incident> incidentList = incidenceRepository
                .findByProfile_UserId(info.getUser().getId());

        return incidentList.stream()
                .map(incident -> new IncidenceResponseDto(
                        incident.getType().getName(),
                        incident.getDate(),
                        incident.getIncidentTime(),
                        incident.getComment(),
                        (incident.getStatus()),
                        incident.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public IncidenceResponseDto postIncidence(IncidenceRequestDto requestDto) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        IncidenceType tipoRef = incidenceTypeRepository.getReferenceById(requestDto.getTypeId());

        Incident incident = new Incident();
        incident.setProfile(info.getProfile());
        incident.setDate(requestDto.getFechaAfectada());
        incident.setComment(requestDto.getComentario());
        incident.setStatus(EstadoIncidencia.PENDING);
        incident.setCreatedAt(OffsetDateTime.now());
        incident.setType(tipoRef);
        incident.setIncidentTime(requestDto.getHora());
        incident.setCompany(info.getCompany());
        incident.setUpdatedAt(OffsetDateTime.now());

        incident = incidenceRepository.save(incident);
        return new IncidenceResponseDto(
                incident.getType().getName(),
                incident.getDate(),
                incident.getIncidentTime(),
                incident.getComment(),
                incident.getStatus(),
                incident.getCreatedAt()
        );
    }
}
