package com.worktrace.worktracebackend.dailyclosure.infrastructure.adapter.in;

import com.worktrace.worktracebackend.dailyclosure.domain.port.in.RunDailyClosureUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

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
    private final ZoneId zoneId;

    public DailyClosureSchedulerAdapter(RunDailyClosureUseCase runDailyClosureUseCase, ZoneId zoneId) {
        this.runDailyClosureUseCase = runDailyClosureUseCase;
        this.zoneId = zoneId;
    }

    /**
     * Ejecuta automáticamente el cierre diario del día anterior.
     * <p>
     * Programado mediante cron para las 10:00 todos los días en la zona horaria de
     * la aplicación ({@code "0 0 10 * * ?"}, {@code zone = ${worktrace.timezone}}).
     * El día a cerrar se calcula también en esa zona, de modo que "ayer" sea
     * siempre el día natural del negocio con independencia de la zona de la JVM.
     * Procesa siempre la jornada del día previo, que ya está completa.
     */
    @Scheduled(cron = "0 0 10 * * ?", zone = "${worktrace.timezone}")
    public void runDailyClosure() {
        runDailyClosureUseCase.execute(LocalDate.now(zoneId).minusDays(1));
    }
}
