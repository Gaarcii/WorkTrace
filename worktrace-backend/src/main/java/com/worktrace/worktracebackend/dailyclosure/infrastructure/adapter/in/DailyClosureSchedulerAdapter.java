package com.worktrace.worktracebackend.dailyclosure.infrastructure.adapter.in;

import com.worktrace.worktracebackend.dailyclosure.domain.port.in.RunDailyClosureUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DailyClosureSchedulerAdapter {

    private final RunDailyClosureUseCase runDailyClosureUseCase;

    public DailyClosureSchedulerAdapter(RunDailyClosureUseCase runDailyClosureUseCase) {
        this.runDailyClosureUseCase = runDailyClosureUseCase;
    }

    @Scheduled(cron = "0 0 10 * * ?")
    public void runDailyClosure() {
        runDailyClosureUseCase.execute(LocalDate.now().minusDays(1));
    }
}
