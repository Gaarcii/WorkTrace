package com.worktrace.worktracebackend.service.ip;

import com.worktrace.worktracebackend.dto.ip.IpResponseDto;
import com.worktrace.worktracebackend.model.KnownIp;
import com.worktrace.worktracebackend.repository.KnownIpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio encargado de analizar direcciones IP para detectar posibles riesgos de seguridad y obtener datos de geolocalización.
 * Su propósito es identificar si una IP proviene de una red anónima (VPN, Tor, Proxy), lo cual es crucial
 * para la integridad de los fichajes. Para optimizar el rendimiento y reducir costes, utiliza un sistema de caché
 * que almacena los resultados de las consultas a la API externa.
 */
@Service
@RequiredArgsConstructor
public class IpDetectionService {

    @Value("${abstract.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final KnownIpRepository knownIpRepository;

    /**
     * Contenedor de datos para el resultado del análisis de una dirección IP.
     * @param flags Lista de banderas de seguridad detectadas (p. ej., "VPN_DETECTED").
     * @param geoIpMap Mapa con los datos de geolocalización y seguridad obtenidos de la API.
     */
    public record IpAnalysisResult(List<String> flags, Map<String, Object> geoIpMap) {
    }

    /**
     * Analiza una dirección IP para obtener sus detalles de seguridad y geolocalización.
     * El método sigue un flujo de trabajo optimizado:
     * 1. Ignora las direcciones locales (localhost).
     * 2. Busca la IP en la caché local para una respuesta inmediata.
     * 3. Si no está en caché, consulta la API externa de inteligencia de IP.
     * 4. Procesa la respuesta, extrae las banderas de seguridad (VPN, Tor, etc.) y almacena el resultado en la caché para futuras consultas.
     *
     * @param ipAddress La dirección IP que se va a analizar.
     * @return Un objeto {@link IpAnalysisResult} que contiene las banderas de seguridad y los datos de geolocalización.
     */
    public IpAnalysisResult analyzeIpWithDetails(String ipAddress) {
        if (ipAddress.equals("127.0.0.1") ||
                ipAddress.equals("0:0:0:0:0:0:0:1") ||
                ipAddress.equals("::1")) {
            return new IpAnalysisResult(new ArrayList<>(), null);
        }

        Optional<KnownIp> cachedIp = knownIpRepository.findByIp(ipAddress);

        if (cachedIp.isPresent()) {
            Map<String, Object> cachedData = cachedIp.get().getGeoIpData();
            List<String> flags = extractFlagsFromMap(cachedData);

            System.out.println("IP " + ipAddress + " rescatada de la caché local.");
            return new IpAnalysisResult(flags, cachedData);
        }

        System.out.println("IP " + ipAddress + " no conocida. Consultando a Abstract API...");
        IpResponseDto ipInfo = analyzeIp(ipAddress);

        List<String> flags = new ArrayList<>();
        Map<String, Object> geoIpMap = null;

        if (ipInfo != null) {
            IpResponseDto.SecurityData security = ipInfo.getSecurity();
            if (security != null) {
                if (security.isVpn()) flags.add("VPN_DETECTED");
                if (security.isTor()) flags.add("TOR_NETWORK");
                if (security.isProxy()) flags.add("PROXY_DETECTED");
                if (security.isRelay()) flags.add(("RELAY_DETECTED"));
                if (security.isAbuse()) flags.add("ABUSE_DETECTED");
            }

            ObjectMapper mapper = new ObjectMapper();
            geoIpMap = mapper.convertValue(ipInfo, new TypeReference<>() {
            });

            KnownIp newIp = new KnownIp();
            newIp.setIp(ipAddress);
            newIp.setGeoIpData(geoIpMap);
            try {
                String jsonString = mapper.writeValueAsString(geoIpMap);

                knownIpRepository.saveNativeIp(ipAddress, jsonString, OffsetDateTime.now());
            } catch (Exception e) {
                System.err.println("No se pudo guardar la IP en caché: " + e.getMessage());
            }
        }

        return new IpAnalysisResult(flags, geoIpMap);
    }

    private IpResponseDto analyzeIp(String ipAddress) {
        try {
            String url = "https://ip-intelligence.abstractapi.com/v1/?api_key=" + apiKey
                    + "&ip_address=" + ipAddress
                    + "&fields=security,location";
            return restTemplate.getForObject(url, IpResponseDto.class);
        } catch (Exception e) {
            System.err.println("Error al consultar la API de IP: " + e.getMessage());
            return null;
        }
    }

    private List<String> extractFlagsFromMap(Map<String, Object> cachedData) {
        List<String> flags = new ArrayList<>();
        if (cachedData != null && cachedData.containsKey("security")) {
            Object secObj = cachedData.get("security");
            if (secObj instanceof Map<?, ?> securityMap) {
                if (Boolean.TRUE.equals(securityMap.get("is_vpn"))) flags.add("VPN_DETECTED");
                if (Boolean.TRUE.equals(securityMap.get("is_tor"))) flags.add("TOR_NETWORK");
                if (Boolean.TRUE.equals(securityMap.get("is_proxy"))) flags.add("PROXY_DETECTED");
                if (Boolean.TRUE.equals(securityMap.get("is_relay"))) flags.add("RELAY_DETECTED");
                if (Boolean.TRUE.equals(securityMap.get("is_abuse"))) flags.add("ABUSE_DETECTED");
            }
        }
        return flags;
    }
}
