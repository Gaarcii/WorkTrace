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
    public List<JobPositionResponseDto> getJobPositions() {
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
    public JobPositionResponseDto createJobPosition(JobPositionRequestDto requestDto) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        String name = requestDto.getName().trim();
        boolean exists = jobPositionRepository.findByTitleIgnoreCaseAndCompany_Id(name, info.getCompany().getId()).isPresent();
        if (exists) {
            throw new IllegalArgumentException("Ya existe un puesto con ese nombre en la empresa");
        }
        JobPosition jobPosition = new JobPosition();
        jobPosition.setTitle(name);
        jobPosition.setCreatedAt(OffsetDateTime.now());
        jobPosition.setCompany(info.getCompany());
        jobPositionRepository.save(jobPosition);

        return new JobPositionResponseDto(
                jobPosition.getId(),
                jobPosition.getTitle());
    }

    @Transactional
    public void deleteJobPosition(UUID jobPositionId) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();

        JobPosition jobPosition = jobPositionRepository.findById(jobPositionId)
                .orElseThrow(() -> new IllegalStateException("Puesto de trabajo no encontrado"));

        if (jobPosition.getCompany().getId().equals(company.getId())) {
            jobPositionRepository.delete(jobPosition);
        }
    }
}
