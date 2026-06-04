-- ============================================================
-- ÍNDICES DE RENDIMIENTO
-- Añadir a medida que se migran dominios a arquitectura hexagonal.
-- En producción usar CREATE INDEX CONCURRENTLY para no bloquear.
-- ============================================================

-- ------------------------------------------------------------
-- time_entries
-- Cubre: countByCompanyAndDate, getWorkedMinutesByCompanyAndDate
--        streamForClosure (dailyclosure), countOpenShifts (dailyclosure)
-- ------------------------------------------------------------

-- Consultas por empresa y fecha (el filtro más frecuente del sistema)
CREATE INDEX idx_time_entries_company_date
    ON time_entries (company_id, work_date)
    WHERE deleted_at IS NULL;

-- Consultas de dailyclosure: fichajes abiertos por empresa y fecha
CREATE INDEX idx_time_entries_company_date_open
    ON time_entries (company_id, work_date)
    WHERE deleted_at IS NULL AND status = 'OPEN';

-- ------------------------------------------------------------
-- daily_closures
-- Cubre: búsqueda del cierre anterior para encadenar el hash
-- ------------------------------------------------------------
CREATE INDEX idx_daily_closures_company_date
    ON daily_closures (company_id, work_date DESC);

-- getFirstTimeEntryDateForEmployee: mínimo de work_date por empleado
CREATE INDEX idx_time_entries_employee_date
    ON time_entries (employee_id, work_date ASC)
    WHERE deleted_at IS NULL;

    -- getActiveWorkers: fichajes abiertos por empresa (sin end_at)
CREATE INDEX idx_time_entries_company_open
    ON time_entries (company_id)
    WHERE deleted_at IS NULL AND end_at IS NULL;

-- WorkSchedule: horarios por empleado y día (evita N+1 en getActiveWorkers)
CREATE INDEX idx_work_schedules_employee_day
    ON work_schedules (employee_id, day_of_week);