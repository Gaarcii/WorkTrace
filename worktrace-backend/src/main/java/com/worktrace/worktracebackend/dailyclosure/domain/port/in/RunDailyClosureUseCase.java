package com.worktrace.worktracebackend.dailyclosure.domain.port.in;

import java.time.LocalDate;

public interface RunDailyClosureUseCase {
    void execute(LocalDate targetDate);
}
