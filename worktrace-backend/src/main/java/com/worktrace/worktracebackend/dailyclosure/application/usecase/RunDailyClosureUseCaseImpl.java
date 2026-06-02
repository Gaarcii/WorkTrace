package com.worktrace.worktracebackend.dailyclosure.application.usecase;

import com.worktrace.worktracebackend.dailyclosure.domain.port.in.RunDailyClosureUseCase;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.CompanyQueryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Servicio responsable de orquestar el cierre diario de registros de tiempo y jornadas
 * laborales para todas las compañías en el sistema.
 *
 * <p>
 * Este caso de uso implementa la regla de negocio que garantiza que todas las jornadas
 * laborales de un día determinado sean cerradas de forma automática y segura. El cierre
 * diario incluye la validación de integridad, la generación de hashes criptográficos
 * para auditoría anti-fraude y la inmutabilización de los registros de entrada/salida
 * del personal.
 * </p>
 *
 * <p>
 * <strong>Decisiones de diseño (Clean Architecture):</strong>
 * <ul>
 *   <li>Sin anotación @Transactional a nivel de clase: el límite transaccional se define
 *       por compañía dentro de {@link CompanyClosureProcessor} con REQUIRES_NEW. Este
 *       servicio solo orquesta la paralización.</li>
 *   <li>Hilos virtuales de Java 21: son económicos en recursos, permitiendo un hilo
 *       por compañía sin degradación de rendimiento.</li>
 *   <li>Control de concurrencia: un Semáforo limita accesos simultáneos a la base de
 *       datos según {@code app.daily-closure.concurrency} (default 8), evitando
 *       congestión del pool HikariCP. Calibrar ≤ (hikaricp.maximum-pool-size - 2).</li>
 *   <li>Bloqueo garantizado: {@link ExecutorService#close()} invoca shutdown() +
 *       awaitTermination(MAX), asegurando que el método retorna solo cuando todas
 *       las compañías terminan.</li>
 * </ul>
 * </p>
 *
 * @see CompanyClosureProcessor
 * @see RunDailyClosureUseCase
 */
@Slf4j
@Service
public class RunDailyClosureUseCaseImpl implements RunDailyClosureUseCase {

    private final CompanyQueryPort companyQueryPort;
    private final CompanyClosureProcessor companyClosureProcessor;
    private final int concurrency;

    public RunDailyClosureUseCaseImpl(
            CompanyQueryPort companyQueryPort,
            CompanyClosureProcessor companyClosureProcessor,
            @Value("${app.daily-closure.concurrency:8}") int concurrency) {
        this.companyQueryPort = companyQueryPort;
        this.companyClosureProcessor = companyClosureProcessor;
        this.concurrency = concurrency;
    }

    /**
     * Ejecuta el cierre diario de jornadas laborales para todas las compañías en la fecha especificada.
     *
     * <p>
     * <strong>Regla de negocio:</strong> Este método implementa el flujo de cierre automático que:
     * <ul>
     *   <li>Recupera la lista de todas las compañías activas en el sistema.</li>
     *   <li>Crea un hilo virtual aislado para cada compañía, evitando que fallos en una
     *       empresa impacten el cierre de otras.</li>
     *   <li>Controla la concurrencia mediante un semáforo para no saturar el pool de conexiones
     *       de base de datos.</li>
     *   <li>Invoca el procesador de cierre para realizar validación de integridad, generación
     *       de hashes y cierre transaccional de la jornada.</li>
     *   <li>Registra éxitos y errores en logs para auditoría operativa.</li>
     *   <li>Bloquea el hilo actual hasta que todas las compañías terminen (síncrono).</li>
     * </ul>
     * </p>
     *
     * <p>
     * <strong>Comportamiento en caso de error:</strong> Si una compañía falla durante su
     * cierre (ej. validación de integridad rechazada, error de base de datos), se registra
     * el error en logs pero NO detiene el cierre de otras compañías. Las excepciones internas
     * no se propagan al llamador; se consideran eventos de error capturados y registrados
     * para auditoría.
     * </p>
     *
     * <p>
     * <strong>Sincronización:</strong> Este método es síncrono y bloquea el hilo actual
     * hasta que todos los cierres de compañías se completan (exitosamente o con error),
     * garantizado por {@link ExecutorService#close()}.
     * </p>
     *
     * @param targetDate la fecha del cierre a procesar. Determina qué registros de tiempo
     *                   y jornadas laborales se incluyen en la operación. Por ejemplo,
     *                   si es {@code 2026-06-02}, se cierran todas las jornadas de ese día.
     */
    @Override
    public void execute(LocalDate targetDate) {
        List<UUID> companyIds = companyQueryPort.findAllCompanyIds();
        log.info("Daily closure starting: {} companies, date={}, concurrency={}",
                companyIds.size(), targetDate, concurrency);

        Semaphore semaphore = new Semaphore(concurrency);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (UUID companyId : companyIds) {
                executor.submit(() -> {
                    try {
                        semaphore.acquire();
                        try {
                            companyClosureProcessor.process(companyId, targetDate);
                        } finally {
                            semaphore.release();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("Daily closure interrupted for company {}", companyId);
                    } catch (Exception e) {
                        log.error("Daily closure failed for company {}: {}", companyId, e.getMessage());
                    }
                });
            }
        }
        log.info("Daily closure finished for date={}", targetDate);
    }
}
