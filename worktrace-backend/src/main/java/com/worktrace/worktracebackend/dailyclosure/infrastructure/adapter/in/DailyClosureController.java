package com.worktrace.worktracebackend.dailyclosure.infrastructure.adapter.in;

import com.worktrace.worktracebackend.dailyclosure.domain.model.IntegrityResult;
import com.worktrace.worktracebackend.dailyclosure.domain.port.in.RunDailyClosureUseCase;
import com.worktrace.worktracebackend.dailyclosure.domain.port.in.VerifyIntegrityUseCase;
import com.worktrace.worktracebackend.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/daily-closures")
@RequiredArgsConstructor
public class DailyClosureController {

    private final RunDailyClosureUseCase runDailyClosureUseCase;
    private final VerifyIntegrityUseCase verifyIntegrityUseCase;
    private final UserService userService;

    @PostMapping("/run")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Void> runClosure(@RequestParam LocalDate date) {
        runDailyClosureUseCase.execute(date);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/integrity")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSPECTOR')")
    ResponseEntity<IntegrityResult> verifyIntegrity(@RequestParam LocalDate date){
        UUID companyId = userService.getAuthenticatedUserAndCompanyInfo().getCompany().getId();
        IntegrityResult result = verifyIntegrityUseCase.execute(companyId,date);
        return ResponseEntity.ok(result);
    }
}
