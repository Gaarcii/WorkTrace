package com.worktrace.worktracebackend.infrastructure.out;

import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.shared.model.AuthenticatedUser;
import com.worktrace.worktracebackend.shared.port.AuthenticatedUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringSecurityUserAdapter implements AuthenticatedUserPort {

    private final UserService userService;
    @Override
    public AuthenticatedUser getAuthenticatedUser() {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        return new AuthenticatedUser(
                info.getCompany().getId(),
                info.getProfile().getUserId(),
                info.getUser().getId()
        );
    }
}
