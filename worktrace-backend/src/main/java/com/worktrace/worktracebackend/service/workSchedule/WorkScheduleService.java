package com.worktrace.worktracebackend.service.workSchedule;

import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleRequestDto;
import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleDayRequestDto;
import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleResponseDto;
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

import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

        if (!company.getId().equals(worker.getCompany().getId())) {
            throw new IllegalStateException("El trabajador no pertenece a tu empresa");
        }

        List<WorkScheduleDayRequestDto> requestedSchedules = dto.getSchedules() == null
                ? List.of()
                : dto.getSchedules();

        Set<DayOfWeek> seenDays = EnumSet.noneOf(DayOfWeek.class);
        Map<DayOfWeek, WorkScheduleDayRequestDto> requestedByDay = new EnumMap<>(DayOfWeek.class);
        for (WorkScheduleDayRequestDto requestedSchedule : requestedSchedules) {
            DayOfWeek day = requestedSchedule.getDayOfWeek();
            if (!seenDays.add(day)) {
                throw new IllegalArgumentException("Hay días de la semana duplicados en la solicitud");
            }

            requestedByDay.put(day, requestedSchedule);
        }

        List<WorkSchedule> existingSchedules = workScheduleRepository.findByEmployee_UserId(dto.getEmployeeId());
        Map<DayOfWeek, WorkSchedule> existingByDay = new EnumMap<>(DayOfWeek.class);
        for (WorkSchedule existingSchedule : existingSchedules) {
            existingByDay.put(existingSchedule.getDayOfWeek(), existingSchedule);
        }

        List<WorkSchedule> schedulesToDelete = existingSchedules.stream()
                .filter(existingSchedule -> !requestedByDay.containsKey(existingSchedule.getDayOfWeek()))
                .toList();

        if (!schedulesToDelete.isEmpty()) {
            workScheduleRepository.deleteAll(schedulesToDelete);
        }

        OffsetDateTime now = OffsetDateTime.now();
        List<WorkSchedule> schedulesToCreate = new ArrayList<>();

        for (Map.Entry<DayOfWeek, WorkScheduleDayRequestDto> entry : requestedByDay.entrySet()) {
            DayOfWeek day = entry.getKey();
            WorkScheduleDayRequestDto requestedSchedule = entry.getValue();

            WorkSite workSite = workSiteRepository.findById(requestedSchedule.getSiteId())
                    .orElseThrow(() -> new IllegalArgumentException("Centro de trabajo no encontrado"));

            if (!company.getId().equals(workSite.getCompany().getId())) {
                throw new IllegalStateException("El centro de trabajo no pertenece a tu empresa");
            }

            WorkSchedule existingSchedule = existingByDay.get(day);
            if (existingSchedule != null) {
                existingSchedule.setSite(workSite);
                existingSchedule.setStartTime(requestedSchedule.getStartTime());
                existingSchedule.setEndTime(requestedSchedule.getEndTime());
                existingSchedule.setUpdatedAt(now);
                continue;
            }

            WorkSchedule newWorkSchedule = new WorkSchedule();
            newWorkSchedule.setEmployee(worker.getProfile());
            newWorkSchedule.setSite(workSite);
            newWorkSchedule.setDayOfWeek(day);
            newWorkSchedule.setStartTime(requestedSchedule.getStartTime());
            newWorkSchedule.setEndTime(requestedSchedule.getEndTime());
            newWorkSchedule.setCreatedAt(now);
            newWorkSchedule.setUpdatedAt(now);
            newWorkSchedule.setCompany(company);
            schedulesToCreate.add(newWorkSchedule);
        }

        if (!schedulesToCreate.isEmpty()) {
            workScheduleRepository.saveAll(schedulesToCreate);
        }
    }

    @Transactional(readOnly = true)
    public List<WorkScheduleResponseDto> getEmployeeSchedules(UUID employeeId) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();
        User worker = userService.getUserById(employeeId);

        if (!company.getId().equals(worker.getCompany().getId())) {
            throw new IllegalStateException("El trabajador no pertenece a tu empresa");
        }

        return workScheduleRepository.findByEmployee_UserId(employeeId).stream()
                .sorted(Comparator.comparingInt(schedule -> schedule.getDayOfWeek().getValue()))
                .map(this::toWorkScheduleResponseDto)
                .toList();
    }

    private WorkScheduleResponseDto toWorkScheduleResponseDto(WorkSchedule schedule) {
        LocalTime start = schedule.getStartTime();
        LocalTime end = schedule.getEndTime();
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes < 0) {
            minutes += 24 * 60;
        }

        return new WorkScheduleResponseDto(
                schedule.getSite().getName(),
                schedule.getSite().getAddress(),
                schedule.getDayOfWeek(),
                start,
                end,
                minutes / 60
        );
    }

}
