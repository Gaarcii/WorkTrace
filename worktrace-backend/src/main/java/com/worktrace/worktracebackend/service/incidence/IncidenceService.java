package com.worktrace.worktracebackend.service.incidence;

import com.worktrace.worktracebackend.dto.incidence.IncidenceRequestDto;
import com.worktrace.worktracebackend.dto.incidence.IncidenceResponseDto;
import com.worktrace.worktracebackend.model.EstadoIncidencia;
import com.worktrace.worktracebackend.model.Incidence;
import com.worktrace.worktracebackend.model.IncidenceType;
import com.worktrace.worktracebackend.repository.IncidenceRepository;
import com.worktrace.worktracebackend.repository.IncidenceTypeRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UsuarioYCompaniaInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

        List<Incidence> incidenceList = incidenceRepository
                .findByProfile_UserId(info.getUser().getId());

        return mapearAIncidenceResponse(incidenceList);
    }

    @Transactional
    public IncidenceResponseDto postIncidence(IncidenceRequestDto requestDto) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        IncidenceType tipoRef = incidenceTypeRepository.getReferenceById(requestDto.getTypeId());

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
        return new IncidenceResponseDto(
                incidence.getType().getName(),
                incidence.getDate(),
                incidence.getIncidenceTime(),
                incidence.getComment(),
                incidence.getStatus(),
                incidence.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<IncidenceResponseDto> getIncidenciaPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();

        List<Incidence> incidenceList = incidenceRepository
                .getIncidencesByProfile_UserIdAndDateBetween(
                        info.getUser().getId(),
                        fechaInicio, fechaFin);

        return mapearAIncidenceResponse(incidenceList);
    }

    private List<IncidenceResponseDto> mapearAIncidenceResponse(List<Incidence> incidenceList) {
        return incidenceList.stream()
                .map(incident -> new IncidenceResponseDto(
                        incident.getType().getName(),
                        incident.getDate(),
                        incident.getIncidenceTime(),
                        incident.getComment(),
                        (incident.getStatus()),
                        incident.getCreatedAt()
                ))
                .toList();
    }
}
