package com.worktrace.worktracebackend.dailyclosure.infrastructure.adapter.in;

import com.worktrace.worktracebackend.dailyclosure.domain.port.in.RunDailyClosureUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Adaptador de entrada que dispara el cierre diario de forma programada.
 * <p>
 * Actúa como driver del caso de uso {@link RunDailyClosureUseCase}: en lugar de
 * una petición HTTP, el detonante es el planificador de Spring. Mantiene la
 * lógica de negocio fuera de la infraestructura, limitándose a invocar el caso
 * de uso con la fecha adecuada.
 */
@Component
public class DailyClosureSchedulerAdapter {

    private final RunDailyClosureUseCase runDailyClosureUseCase;

    public DailyClosureSchedulerAdapter(RunDailyClosureUseCase runDailyClosureUseCase) {
        this.runDailyClosureUseCase = runDailyClosureUseCase;
    }

    /**
     * Ejecuta automáticamente el cierre diario del día anterior.
     * <p>
     * Programado mediante cron para las 10:00 todos los días
     * ({@code "0 0 10 * * ?"}). Procesa siempre la jornada del día previo, que ya
     * está completa.
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void runDailyClosure() {
        runDailyClosureUseCase.execute(LocalDate.now().minusDays(1));
    }
}
