package com.worktrace.worktracebackend.infrastructure.out;

import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.shared.port.AuthenticatedUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SpringSecurityUserAdapter implements AuthenticatedUserPort {

    private final UserService userService;

    @Override
    public UUID getCompanyId() {
        return userService.getAuthenticatedUserAndCompanyInfo()
                .getCompany().getId();
    }
}
