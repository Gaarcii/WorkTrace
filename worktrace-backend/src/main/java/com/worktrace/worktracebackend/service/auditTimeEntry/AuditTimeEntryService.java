package com.worktrace.worktracebackend.service.auditTimeEntry;

import com.worktrace.worktracebackend.model.AuditTimeEntry;
import com.worktrace.worktracebackend.model.TimeEntry;
import com.worktrace.worktracebackend.repository.AuditTimeEntryRepository;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import com.worktrace.worktracebackend.service.auth.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio para gestionar la auditoría de los fichajes.
 * Su propósito es registrar cualquier modificación o anulación de un fichaje
 * para mantener un historial de cambios íntegro y trazable, lo cual es
 * fundamental para cumplir con normativas y para inspecciones.
 */
@Service
@RequiredArgsConstructor
public class AuditTimeEntryService {

    private static final String AUDIT_ACTION_VAR = "app.audit_action";

    private final AuditTimeEntryRepository auditRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final UserService userService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Registra un nuevo evento de auditoría para un fichaje.
     * Este método se invoca cuando un fichaje es modificado o anulado,
     * guardando un registro detallado de la operación.
     *
     * @param action      La acción realizada (p. ej., "UPDATE", "VOID").
     * @param reason      El motivo proporcionado por el actor para justificar el cambio.
     * @param oldDataJson Representación en JSON de los datos del fichaje antes del cambio.
     * @param newDataJson Representación en JSON de los datos del fichaje después del cambio.
     * @param timeEntryId El ID del fichaje que ha sido modificado.
     */
    @Transactional
    public void logTimeEntryChange(String action, String reason,
                                   String oldDataJson, String newDataJson, UUID timeEntryId) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();

        AuditTimeEntry auditEntry = AuditTimeEntry.builder()
                .timeEntryId(timeEntryId)
                .action(action)
                .actorUserId(info.getUser().getId())
                .reason(reason)
                .oldData(oldDataJson)
                .newData(newDataJson)
                .companyId(info.getCompany().getId())
                .build();

        auditRepository.save(auditEntry);
    }

    /**
     * Guarda un TimeEntry estableciendo variable de sesión PostgreSQL.
     * <p>
     * CRÍTICO para integridad: Permite al trigger de PostgreSQL distinguir entre:
     * - Cambios vía app (ADMIN_ADJUST, SOFT_DELETE) → Trigger NO registra (Java ya lo hizo)
     * - Cambios directos en BD (DB_DIRECT_MODIFY) → Trigger registra como fraude
     * <p>
     * Variable establecida:
     * - app.audit_action: El tipo de cambio (ADMIN_ADJUST, SOFT_DELETE)
     *
     * @param timeEntry El TimeEntry a guardar
     */
    @Transactional
    public void saveTimeEntry(TimeEntry timeEntry) {
        String auditAction = determineAuditAction(timeEntry);

        setPostgreSQLSessionVariable(auditAction);

        timeEntryRepository.save(timeEntry);
    }

    /**
     * Determina la acción de auditoría basada en los cambios en TimeEntry.
     * <p>
     * Reglas:
     * 1. Si deletedAt != null → SOFT_DELETE
     * 2. Si modificationReason != null → ADMIN_ADJUST
     * 3. Sino → ADMIN_ADJUST (default)
     */
    private String determineAuditAction(TimeEntry timeEntry) {
        if (timeEntry.getDeletedAt() != null) {
            return "SOFT_DELETE";
        } else if (timeEntry.getModificationReason() != null &&
                !timeEntry.getModificationReason().isBlank()) {
            return "ADMIN_ADJUST";
        } else {
            return "ADMIN_ADJUST";
        }
    }

    /**
     * Establece la variable de sesión PostgreSQL 'app.audit_action' que el trigger leerá.
     * <p>
     * La variable es local a la transacción y se limpia automáticamente.
     * El trigger la lee con current_setting('app.audit_action', TRUE).
     *
     * @param auditAction El tipo de acción (ADMIN_ADJUST, SOFT_DELETE)
     */
    private void setPostgreSQLSessionVariable(String auditAction) {
        try {
            Session session = entityManager.unwrap(Session.class);
            session.doWork(connection -> {
                try (java.sql.Statement stmt = connection.createStatement()) {
                    String sql = String.format("SET %s = '%s'", AUDIT_ACTION_VAR, escapeSqlString(auditAction));
                    stmt.execute(sql);
                }
            });
        } catch (Exception e) {
            System.err.println("Warning: Could not set PostgreSQL session variable " +
                    AUDIT_ACTION_VAR + " = " + auditAction + ": " + e.getMessage());
        }
    }

    /**
     * Escapa caracteres especiales en SQL strings.
     */
    private String escapeSqlString(String value) {
        return value.replace("'", "''");
    }
}
