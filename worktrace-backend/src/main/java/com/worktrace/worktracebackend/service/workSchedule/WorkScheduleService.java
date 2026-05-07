package com.worktrace.worktracebackend.service.workSchedule;

import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleRequestDto;
import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleDayRequestDto;
import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleResponseDto;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.Profile;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.model.WorkSchedule;
import com.worktrace.worktracebackend.model.WorkSite;
import com.worktrace.worktracebackend.repository.WorkScheduleRepository;
import com.worktrace.worktracebackend.repository.ProfileRepository;
import com.worktrace.worktracebackend.repository.WorkSiteRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
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

/**
 * Servicio para gestionar la lógica de negocio de los horarios de trabajo de los empleados.
 * Su propósito es permitir a los administradores asignar, actualizar y consultar los horarios
 * semanales de los trabajadores, definiendo qué días trabajan, en qué centro y en qué franja horaria.
 */
@Service
@RequiredArgsConstructor
public class WorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final ProfileRepository profileRepository;
    private final UserService userService;
    private final WorkSiteRepository workSiteRepository;

    /**
     * Asigna o actualiza el horario de trabajo semanal de un empleado.
     * Este método realiza una sincronización completa:
     * 1.  Compara los horarios existentes del empleado con los nuevos proporcionados.
     * 2.  Elimina los horarios de los días que ya no están en la nueva solicitud.
     * 3.  Actualiza los horarios de los días que han cambiado (centro de trabajo, horas).
     * 4.  Crea nuevos horarios para los días que no existían previamente.
     * Esto asegura que el horario del empleado siempre refleje exactamente lo enviado en la última petición.
     *
     * @param dto El DTO que contiene el ID del empleado y la lista de sus horarios por día de la semana.
     */
    @Transactional
    public void assignWorkSchedule(WorkScheduleRequestDto dto) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();
        User worker = userService.getUserById(dto.getEmployeeId());
        Profile employeeProfile = profileRepository.findById(dto.getEmployeeId())
            .orElseThrow(() -> new IllegalArgumentException("Perfil del trabajador no encontrado"));

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
            newWorkSchedule.setEmployee(employeeProfile);
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
            workScheduleRepository.saveAllAndFlush(schedulesToCreate);
        }

        List<WorkSchedule> schedulesToUpdate = existingSchedules.stream()
                .filter(es -> requestedByDay.containsKey(es.getDayOfWeek()))
                .toList();

        if (!schedulesToUpdate.isEmpty()) {
            workScheduleRepository.saveAllAndFlush(schedulesToUpdate);
        }
    }

    /**
     * Obtiene la lista de horarios de trabajo asignados a un empleado específico.
     * La lista se devuelve ordenada por el día de la semana (de lunes a domingo) para una
     * visualización coherente en la interfaz de usuario.
     *
     * @param employeeId El UUID del empleado cuyos horarios se quieren consultar.
     * @return Una lista de {@link WorkScheduleResponseDto} con los horarios del empleado.
     */
    @Transactional(readOnly = true)
    public List<WorkScheduleResponseDto> getEmployeeSchedules(UUID employeeId) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
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
