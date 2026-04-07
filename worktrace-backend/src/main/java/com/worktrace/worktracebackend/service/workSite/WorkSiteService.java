package com.worktrace.worktracebackend.service.workSite;

import com.worktrace.worktracebackend.dto.workSite.WorkSiteRequestDto;
import com.worktrace.worktracebackend.dto.workSite.WorkSiteResponseDto;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.model.WorkSchedule;
import com.worktrace.worktracebackend.model.WorkSite;
import com.worktrace.worktracebackend.repository.WorkScheduleRepository;
import com.worktrace.worktracebackend.repository.WorkSiteRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkSiteService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final WorkSiteRepository workSiteRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<WorkSiteResponseDto> getMyWorkSites() {
        User admin = userService.getAuthenticatedUser();
        Company myCompany = admin.getCompany();

        return workSiteRepository.findByCompany_Id(myCompany.getId())
                .stream()
                .map(w -> new WorkSiteResponseDto(w.getId(), w.getName(), w.getAddress()))
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkSiteResponseDto createWorkSite(WorkSiteRequestDto dto) {
        User admin = userService.getAuthenticatedUser();
        Company myCompany = admin.getCompany();

        WorkSite workSite = new WorkSite();
        workSite.setName(dto.getName());
        workSite.setAddress(dto.getAddress());
        workSite.setCompany(myCompany);
        workSite.setUpdatedAt(OffsetDateTime.now());
        workSite.setCreatedAt(OffsetDateTime.now());
        workSite.setCompany(myCompany);

        WorkSite savedWorkSite = workSiteRepository.save(workSite);

        return new WorkSiteResponseDto(savedWorkSite.getId(), savedWorkSite.getName(), savedWorkSite.getAddress());
    }

    @Transactional
    public WorkSiteResponseDto updateWorkSite(UUID id, WorkSiteRequestDto dto) {
        User admin = userService.getAuthenticatedUser();

        WorkSite workSite = workSiteRepository.findByIdAndCompany_Id(id, admin.getCompany().getId())
                .orElseThrow(() ->
                        new RuntimeException("La sede no existe o no pertenece a tu empresa"));

        workSite.setName(dto.getName());
        workSite.setAddress(dto.getAddress());
        workSite.setUpdatedAt(OffsetDateTime.now());

        WorkSite updatedWorkSite = workSiteRepository.save(workSite);

        return new WorkSiteResponseDto(updatedWorkSite.getId(), updatedWorkSite.getName(), updatedWorkSite.getAddress());
    }

    @Transactional
    public void deleteWorkSite(UUID id) {
        User admin = userService.getAuthenticatedUser();

        WorkSite workSite = workSiteRepository.findByIdAndCompany_Id(id, admin.getCompany().getId())
                .orElseThrow(() -> new RuntimeException("La sede no existe o no pertenece a tu empresa"));

        List<WorkSchedule> schedules = workScheduleRepository.findBySite_Id(workSite.getId());
        if (!schedules.isEmpty()) {
            String scheduleDetails = schedules.stream()
                    .map(schedule -> schedule.getDayOfWeek() + " "
                            + schedule.getStartTime().format(TIME_FORMATTER) + "-"
                            + schedule.getEndTime().format(TIME_FORMATTER)
                            + " (" + schedule.getEmployee().getFullName() + ")")
                    .collect(Collectors.joining(", "));

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede eliminar la sede porque tiene horarios asignados: " + scheduleDetails
            );
        }

        workSiteRepository.delete(workSite);
    }
}