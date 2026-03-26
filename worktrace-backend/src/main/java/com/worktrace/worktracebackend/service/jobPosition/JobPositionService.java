package com.worktrace.worktracebackend.service.jobPosition;

import com.worktrace.worktracebackend.dto.jobPosition.JobPositionRequestDto;
import com.worktrace.worktracebackend.dto.jobPosition.JobPositionResponseDto;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.JobPosition;
import com.worktrace.worktracebackend.repository.JobPositionRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UsuarioYCompaniaInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobPositionService {

    private final JobPositionRepository jobPositionRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<JobPositionResponseDto> getPuestosTrabajo() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();

        List<JobPosition> jobPositionList = jobPositionRepository
                .getJobPositionsByCompany_Id(info.getCompany().getId());

        return jobPositionList.stream().map(
                position -> new JobPositionResponseDto(
                        position.getId(),
                        position.getTitle()
                )).toList();
    }

    @Transactional
    public JobPositionResponseDto crearNuevoPuesto(JobPositionRequestDto requestDto) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        String nombre = requestDto.getNombre().trim();
        boolean exists = jobPositionRepository.findByTitleIgnoreCaseAndCompany_Id(nombre, info.getCompany().getId()).isPresent();
        if (exists) {
            throw new IllegalArgumentException("Ya existe un puesto con ese nombre en la empresa");
        }
        JobPosition puesto = new JobPosition();
        puesto.setTitle(nombre);
        puesto.setCreatedAt(OffsetDateTime.now());
        puesto.setCompany(info.getCompany());
        jobPositionRepository.save(puesto);

        return new JobPositionResponseDto(
                puesto.getId(),
                puesto.getTitle());
    }

    @Transactional
    public void eliminarPuesto(UUID jobId) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();

        JobPosition puesto = jobPositionRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("Puesto de trabajo no encontrado"));

        if (puesto.getCompany().getId().equals(company.getId())) {
            jobPositionRepository.delete(puesto);
        }
    }
}
