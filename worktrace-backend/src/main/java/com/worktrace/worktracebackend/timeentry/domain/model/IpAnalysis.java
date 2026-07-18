package com.worktrace.worktracebackend.timeentry.domain.model;

import java.util.List;
import java.util.Map;

/**
 * Resultado del análisis de una dirección IP para un fichaje.
 * <p>
 * Modelo de dominio que aísla al caso de uso del servicio externo de
 * inteligencia de IP: transporta las banderas de seguridad detectadas (p. ej.
 * VPN o Tor) y los datos de geolocalización asociados.
 *
 * @param flags    Banderas de seguridad detectadas para la IP; puede ir vacía.
 * @param geoIpMap Datos de geolocalización y seguridad de la IP, o {@code null}
 *                 si no se han podido obtener (p. ej. direcciones locales).
 */
public record IpAnalysis(
        List<String> flags,
        Map<String, Object> geoIpMap
) {
}
