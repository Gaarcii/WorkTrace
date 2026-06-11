package com.worktrace.worktracebackend.infrastructure.out;

import com.worktrace.worktracebackend.repository.WorkScheduleRepository;
import com.worktrace.worktracebackend.timeentry.domain.model.ScheduledShift;
import com.worktrace.worktracebackend.timeentry.domain.port.out.WorkScheduleQueryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de salida que implementa {@link WorkScheduleQueryPort} sobre JPA.
 * <p>
 * Consulta los horarios de trabajo en modo solo lectura y traduce las entidades
 * JPA al modelo de dominio {@link ScheduledShift}, dando soporte al cálculo de
 * puntualidad de los trabajadores activos.
 */
@Component
@Transactional(readOnly = true)
public class WorkScheduleJpaAdapter implements WorkScheduleQueryPort {

    private final WorkScheduleRepository workScheduleRepository;

    public WorkScheduleJpaAdapter(WorkScheduleRepository workScheduleRepository) {
        this.workScheduleRepository = workScheduleRepository;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Recupera los horarios de los empleados indicados para el día dado y los
     * agrupa en un mapa por identificador de empleado.
     */
    @Override
    public Map<UUID, ScheduledShift> findByEmployeesAndDayOfWeek(List<UUID> employeeIds, DayOfWeek dayOfWeek) {
        return workScheduleRepository.findByEmployee_UserIdInAndDayOfWeek(employeeIds, dayOfWeek)
                .stream()
                .collect(Collectors.toMap(
                        ws -> ws.getEmployee().getUserId(),
                        ws -> new ScheduledShift(ws.getEmployee().getUserId(), ws.getStartTime(), ws.getEndTime())
                ));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Recupera el horario del empleado para el día indicado y lo traduce a
     * {@link ScheduledShift}, o devuelve vacío si no tiene horario ese día.
     */
    @Override
    public Optional<ScheduledShift> findByEmployeeAndDayOfWeek(UUID userId, DayOfWeek dayOfWeek) {
        return workScheduleRepository.findByEmployee_UserIdAndDayOfWeek(userId, dayOfWeek)
                .map(ws -> new ScheduledShift(
                        ws.getEmployee().getUserId(),
                        ws.getStartTime(),
                        ws.getEndTime()
                ));
    }
}
