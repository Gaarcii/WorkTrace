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
 * Caso de uso que orquesta el cierre diario de <strong>todas las compañías</strong>
 * para una fecha dada.
 * <p>
 * Su única responsabilidad es la coordinación: recupera la lista de compañías y
 * delega el cierre real de cada una en {@link CompanyClosureProcessor}. La lógica
 * de negocio y la frontera transaccional viven en el procesador, no aquí.
 *
 * <p><strong>Modelo de ejecución (concurrencia):</strong>
 * <ul>
 *   <li><em>Hilos virtuales (Java 21):</em> se lanza una tarea por compañía con
 *       {@link Executors#newVirtualThreadPerTaskExecutor()}. Son muy baratos, por
 *       lo que tener un hilo por compañía no degrada el rendimiento aunque haya
 *       miles.</li>
 *   <li><em>Concurrencia acotada:</em> un {@link Semaphore} limita cuántos
 *       cierres acceden a la base de datos a la vez, según
 *       {@code app.daily-closure.concurrency} (por defecto 8). Esto evita agotar
 *       el pool de conexiones; conviene calibrarlo a un valor &le; (tamaño del
 *       pool HikariCP − 2).</li>
 *   <li><em>Aislamiento de fallos:</em> cada compañía se procesa en su propia
 *       transacción ({@code REQUIRES_NEW} en el procesador) y sus errores se
 *       capturan y registran sin propagarse, de modo que un fallo individual no
 *       interrumpe el cierre del resto.</li>
 *   <li><em>Sincronía:</em> el método bloquea hasta que todas las tareas
 *       terminan, gracias al cierre automático del {@link ExecutorService} en el
 *       try-with-resources, que invoca {@code shutdown()} + {@code awaitTermination}.</li>
 * </ul>
 *
 * <p>No lleva {@code @Transactional} a nivel de clase de forma deliberada: abrir
 * una transacción aquí mantendría una conexión retenida durante todo el lote y
 * entraría en conflicto con el modelo de una transacción por compañía.
 *
 * @see CompanyClosureProcessor
 * @see RunDailyClosureUseCase
 */
@Slf4j
@Service
public class RunDailyClosureUseCaseImpl implements RunDailyClosureUseCase {

    private final CompanyQueryPort companyQueryPort;
    private final CompanyClosureProcessor companyClosureProcessor;

    /** Número máximo de cierres de compañía procesados simultáneamente. */
    private final int concurrency;

    /**
     * @param companyQueryPort        Puerto para recuperar los identificadores
     *                                de todas las compañías a cerrar.
     * @param companyClosureProcessor Procesador que ejecuta el cierre de una
     *                                única compañía.
     * @param concurrency             Límite de concurrencia leído de
     *                                {@code app.daily-closure.concurrency}
     *                                (por defecto 8).
     */
    public RunDailyClosureUseCaseImpl(
            CompanyQueryPort companyQueryPort,
            CompanyClosureProcessor companyClosureProcessor,
            @Value("${app.daily-closure.concurrency:8}") int concurrency) {
        this.companyQueryPort = companyQueryPort;
        this.companyClosureProcessor = companyClosureProcessor;
        this.concurrency = concurrency;
    }

    /**
     * Ejecuta el cierre diario de todas las compañías para la fecha indicada.
     * <p>
     * Recupera todas las compañías y lanza una tarea por cada una sobre hilos
     * virtuales, limitando la concurrencia con un semáforo. El método es
     * <strong>síncrono</strong>: no retorna hasta que todas las tareas han
     * finalizado (con éxito o error).
     * <p>
     * <strong>Tratamiento de errores:</strong> el fallo al cerrar una compañía
     * se registra en el log pero <em>no</em> se propaga ni detiene el resto; una
     * {@link InterruptedException} restaura el flag de interrupción del hilo.
     *
     * @param targetDate Fecha laboral cuyo cierre se procesa (p. ej.
     *                   {@code 2026-06-02} cierra las jornadas de ese día).
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
