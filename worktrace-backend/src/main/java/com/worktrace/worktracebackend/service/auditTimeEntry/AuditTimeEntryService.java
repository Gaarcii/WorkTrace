package com.worktrace.worktracebackend.service.auditTimeEntry;

import com.worktrace.worktracebackend.model.AuditTimeEntry;
import com.worktrace.worktracebackend.repository.AuditTimeEntryRepository;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import com.worktrace.worktracebackend.service.auth.UserService;
import lombok.RequiredArgsConstructor;
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

    private final AuditTimeEntryRepository auditRepository;
    private final UserService userService;

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
}
