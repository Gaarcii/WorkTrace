package com.worktrace.worktracebackend.service.workSchedule;

import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleRequestDto;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.model.WorkSchedule;
import com.worktrace.worktracebackend.model.WorkSite;
import com.worktrace.worktracebackend.repository.WorkScheduleRepository;
import com.worktrace.worktracebackend.repository.WorkSiteRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UsuarioYCompaniaInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final UserService userService;
    private final WorkSiteRepository workSiteRepository;

    @Transactional
    public void assignWorkSchedule(WorkScheduleRequestDto dto) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();
        User worker = userService.getUserById(dto.getEmployeeId());

        WorkSite workSite = workSiteRepository.findById(dto.getSiteId())
                .orElseThrow(() -> new IllegalArgumentException("Centro de trabajo no encontrado"));

        Optional<WorkSchedule> horario = workScheduleRepository
                .findByEmployee_UserIdAndDayOfWeek(dto.getEmployeeId(), dto.getDayOfWeek());

        if (!company.getId().equals(worker.getCompany().getId())) {
            throw new IllegalStateException("El trabajador no pertenece a tu empresa");
        }

        if (!company.getId().equals(workSite.getCompany().getId())) {
            throw new IllegalStateException("El centro de trabajo no pertenece a tu empresa");
        }

        if (horario.isPresent()) {
            WorkSchedule workScheduleUpdate = horario.get();
            workScheduleUpdate.setSite(workSite);
            workScheduleUpdate.setStartTime(dto.getStartTime());
            workScheduleUpdate.setEndTime(dto.getEndTime());
            workScheduleUpdate.setUpdatedAt(OffsetDateTime.now());
        } else {
            WorkSchedule newWorkSchedule = new WorkSchedule();
            newWorkSchedule.setEmployee(worker.getProfile());
            newWorkSchedule.setSite(workSite);
            newWorkSchedule.setDayOfWeek(dto.getDayOfWeek());
            newWorkSchedule.setStartTime(dto.getStartTime());
            newWorkSchedule.setEndTime(dto.getEndTime());
            newWorkSchedule.setCreatedAt(OffsetDateTime.now());
            newWorkSchedule.setUpdatedAt(OffsetDateTime.now());
            newWorkSchedule.setCompany(company);
            workScheduleRepository.save(newWorkSchedule);
        }
    }

}
