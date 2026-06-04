package com.worktrace.worktracebackend.shared.port;

import com.worktrace.worktracebackend.shared.model.AuthenticatedUser;

public interface AuthenticatedUserPort {
    AuthenticatedUser getAuthenticatedUser();
}
