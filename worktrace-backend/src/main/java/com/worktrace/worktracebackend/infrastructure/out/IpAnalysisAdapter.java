package com.worktrace.worktracebackend.infrastructure.out;

import com.worktrace.worktracebackend.service.ip.IpDetectionService;
import com.worktrace.worktracebackend.timeentry.domain.model.IpAnalysis;
import com.worktrace.worktracebackend.timeentry.domain.port.out.IpAnalysisPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adaptador de salida que implementa {@link IpAnalysisPort} sobre el
 * {@link IpDetectionService}.
 * <p>
 * Traduce el resultado del servicio de inteligencia de IP al modelo de dominio
 * {@link IpAnalysis}, aislando al dominio de fichajes del proveedor externo y
 * de su caché.
 */
@Component
public class IpAnalysisAdapter implements IpAnalysisPort {

    private final IpDetectionService ipDetectionService;

    public IpAnalysisAdapter(IpDetectionService ipDetectionService) {
        this.ipDetectionService = ipDetectionService;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delega en {@link IpDetectionService#analyzeIpWithDetails(String, UUID)} y
     * mapea las banderas y los datos de geolocalización al modelo de dominio.
     */
    @Override
    public IpAnalysis analyze(String ipAddress, UUID companyId) {
        IpDetectionService.IpAnalysisResult result = ipDetectionService.analyzeIpWithDetails(ipAddress, companyId);
        return new IpAnalysis(result.flags(), result.geoIpMap());
    }
}
