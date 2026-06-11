-- ============================================================
-- ÍNDICES DE RENDIMIENTO
-- Añadir a medida que se migran dominios a arquitectura hexagonal.
-- En producción usar CREATE INDEX CONCURRENTLY para no bloquear.
-- ============================================================

-- ------------------------------------------------------------
-- time_entries
-- Cubre: countByCompanyAndDate, getWorkedMinutesByCompanyAndDate
--        streamForClosure (dailyclosure), countOpenShifts (dailyclosure)
--        getFirstTimeEntryDateForEmployee, findTop5ByEmployee
--        getActiveWorkers
-- ------------------------------------------------------------

-- Consultas por empresa y fecha (el filtro más frecuente del sistema)
CREATE INDEX idx_time_entries_company_date
    ON time_entries (company_id, work_date)
    WHERE deleted_at IS NULL;

-- Fichajes abiertos por empresa y fecha (dailyclosure)
CREATE INDEX idx_time_entries_company_date_open
    ON time_entries (company_id, work_date)
    WHERE deleted_at IS NULL AND status = 'OPEN';

-- Mínimo de work_date por empleado (getFirstTimeEntryDateForEmployee)
CREATE INDEX idx_time_entries_employee_date
    ON time_entries (employee_id, work_date ASC)
    WHERE deleted_at IS NULL;

-- Últimos eventos de fichaje del empleado ordenados por start_at (findTop5ByEmployee)
CREATE INDEX idx_time_entries_employee_start
    ON time_entries (employee_id, start_at DESC)
    WHERE deleted_at IS NULL;

-- Fichajes abiertos por empresa sin end_at (getActiveWorkers)
CREATE INDEX idx_time_entries_company_open
    ON time_entries (company_id)
    WHERE deleted_at IS NULL AND end_at IS NULL;

-- ------------------------------------------------------------
-- daily_closures
-- Cubre: búsqueda del cierre anterior para encadenar el hash
-- ------------------------------------------------------------

-- Cierre más reciente por empresa y fecha
CREATE INDEX idx_daily_closures_company_date
    ON daily_closures (company_id, work_date DESC);

-- ------------------------------------------------------------
-- work_schedules
-- Cubre: getActiveWorkers (evita N+1 al resolver horarios por empleado)
-- ------------------------------------------------------------

-- Horarios por empleado y día de la semana
CREATE INDEX idx_work_schedules_employee_day
    ON work_schedules (employee_id, day_of_week);

-- ------------------------------------------------------------
-- incidences
-- Cubre: consultas de incidencias por usuario y fecha
-- ------------------------------------------------------------

-- Incidencias por usuario y fecha
CREATE INDEX idx_incidences_user_date
    ON incidences (user_id, date);
