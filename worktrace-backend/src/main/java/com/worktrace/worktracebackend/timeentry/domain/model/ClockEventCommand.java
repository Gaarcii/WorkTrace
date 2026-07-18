package com.worktrace.worktracebackend.timeentry.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Orden para persistir un evento de fichaje ya resuelto por el dominio.
 * <p>
 * Modelo de dominio que el caso de uso entrega al puerto de escritura con toda
 * la información necesaria para abrir o cerrar la jornada: los identificadores
 * del trabajador y la empresa (aislamiento multi-tenant), los datos de
 * ubicación y dispositivo, y el resultado ya consolidado del análisis de IP
 * (banderas y geolocalización). La decisión de abrir o cerrar depende de si el
 * trabajador tiene una jornada abierta, y la resuelve el adaptador de forma
 * atómica.
 *
 * @param companyId       Identificador de la empresa del fichaje.
 * @param employeeUserId  Identificador del trabajador que ficha (coincide con el
 *                        identificador de su cuenta de usuario).
 * @param lat             Latitud de la ubicación del fichaje.
 * @param lng             Longitud de la ubicación del fichaje.
 * @param accuracyMeters  Precisión del GPS en metros; puede ser {@code null}.
 * @param realIp          Dirección IP real del cliente.
 * @param userAgent       User-Agent del dispositivo o navegador del cliente.
 * @param geoIpMap        Datos de geolocalización de la IP, o {@code null}.
 * @param flags           Banderas de auditoría a registrar en el evento.
 */
public record ClockEventCommand(
        UUID companyId,
        UUID employeeUserId,
        BigDecimal lat,
        BigDecimal lng,
        Integer accuracyMeters,
        String realIp,
        String userAgent,
        Map<String, Object> geoIpMap,
        List<String> flags
) {
}
