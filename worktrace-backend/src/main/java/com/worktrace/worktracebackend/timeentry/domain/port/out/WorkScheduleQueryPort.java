package com.worktrace.worktracebackend.timeentry.domain.port.out;

import com.worktrace.worktracebackend.timeentry.domain.model.ScheduledShift;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Puerto de salida para consultar los horarios de trabajo desde el dominio de
 * fichajes.
 * <p>
 * Da soporte al cálculo de puntualidad de los trabajadores activos, aportando el
 * turno previsto de cada empleado sin acoplar el dominio a la persistencia.
 */
public interface WorkScheduleQueryPort {

    /**
     * Obtiene el turno previsto de varios empleados para un día de la semana.
     * <p>
     * Resuelve en una sola consulta los horarios de todos los empleados
     * indicados, devolviéndolos indexados por empleado para un cruce eficiente.
     *
     * @param employeeIds Identificadores de los empleados a consultar.
     * @param dayOfWeek   Día de la semana cuyo horario se solicita.
     * @return Un mapa de identificador de empleado a su {@link ScheduledShift};
     * los empleados sin horario ese día no aparecen en el mapa.
     */
    Map<UUID, ScheduledShift> findByEmployeesAndDayOfWeek(
            List<UUID> employeeIds, DayOfWeek dayOfWeek);
}
