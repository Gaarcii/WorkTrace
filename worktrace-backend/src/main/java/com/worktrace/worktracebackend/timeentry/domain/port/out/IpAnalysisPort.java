package com.worktrace.worktracebackend.timeentry.domain.port.out;

import com.worktrace.worktracebackend.timeentry.domain.model.IpAnalysis;

import java.util.UUID;

/**
 * Puerto de salida para el análisis de direcciones IP en los fichajes.
 * <p>
 * Abstrae el servicio de inteligencia de IP (detección de VPN/Tor y
 * geolocalización) para que el dominio no dependa de la tecnología ni del
 * proveedor externo concretos. La implementación reside en infraestructura.
 */
public interface IpAnalysisPort {

    /**
     * Analiza una dirección IP en el contexto de una empresa.
     *
     * @param ipAddress Dirección IP a analizar.
     * @param companyId Identificador de la empresa asociada a la IP.
     * @return Un {@link IpAnalysis} con las banderas de seguridad detectadas y
     * los datos de geolocalización.
     */
    IpAnalysis analyze(String ipAddress, UUID companyId);
}
