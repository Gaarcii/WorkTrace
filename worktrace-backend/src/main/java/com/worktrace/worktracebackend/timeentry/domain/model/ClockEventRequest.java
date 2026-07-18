package com.worktrace.worktracebackend.timeentry.domain.model;

import java.math.BigDecimal;

/**
 * Datos de entrada para registrar un evento de fichaje (entrada o salida).
 * <p>
 * Modelo de dominio que transporta la información capturada del cliente al
 * fichar, sin acoplar el caso de uso a los DTOs de la capa web. La decisión de
 * si el evento abre o cierra una jornada se resuelve dentro del dominio a partir
 * del estado del trabajador, no de este objeto.
 *
 * @param lat            Latitud de la ubicación del fichaje.
 * @param lng            Longitud de la ubicación del fichaje.
 * @param accuracyMeters Precisión del GPS en metros; puede ser {@code null} si el
 *                       cliente no la aporta.
 * @param realIp         Dirección IP real del cliente, para auditoría y análisis.
 * @param userAgent      User-Agent del dispositivo o navegador del cliente.
 */
public record ClockEventRequest(
        BigDecimal lat,
        BigDecimal lng,
        Integer accuracyMeters,
        String realIp,
        String userAgent
) {
}
