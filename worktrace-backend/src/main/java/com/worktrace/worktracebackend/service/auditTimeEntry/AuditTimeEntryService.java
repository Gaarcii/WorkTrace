package com.worktrace.worktracebackend.service.auditTimeEntry;

import com.worktrace.worktracebackend.model.AuditTimeEntry;
import com.worktrace.worktracebackend.repository.AuditTimeEntryRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UsuarioYCompaniaInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditTimeEntryService {

    private final AuditTimeEntryRepository auditRepository;
    private final UserService userService;

    @Transactional
    public void logTimeEntryChange(String action, String reason,
                                   String oldDataJson, String newDataJson, UUID timeEntryId) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();

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

