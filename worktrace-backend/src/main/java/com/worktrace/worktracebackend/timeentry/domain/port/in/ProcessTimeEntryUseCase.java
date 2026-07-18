package com.worktrace.worktracebackend.timeentry.domain.port.in;

import com.worktrace.worktracebackend.timeentry.domain.model.ClockEventRequest;
import com.worktrace.worktracebackend.timeentry.domain.model.ClockEventResult;

/**
 * Caso de uso (puerto de entrada) que registra un fichaje del trabajador
 * autenticado, ya sea de entrada o de salida.
 * <p>
 * Si el trabajador tiene una jornada abierta, el evento la cierra; en caso
 * contrario, abre una nueva. Además, analiza la IP de origen para detectar
 * anomalías (VPN, Tor) y evalúa la precisión del GPS, añadiendo las banderas de
 * auditoría correspondientes. La empresa y el trabajador se resuelven desde el
 * usuario autenticado (aislamiento multi-tenant).
 */
public interface ProcessTimeEntryUseCase {

    /**
     * Registra un evento de fichaje para el trabajador autenticado.
     *
     * @param request Datos del fichaje (ubicación, precisión, IP y User-Agent).
     * @return El {@link ClockEventResult} con el estado del fichaje afectado.
     */
    ClockEventResult execute(ClockEventRequest request);
}
