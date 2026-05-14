package com.worktrace.worktracebackend.service.ip;

import com.worktrace.worktracebackend.dto.ip.IpResponseDto;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.KnownIp;
import com.worktrace.worktracebackend.repository.CompanyRepository;
import com.worktrace.worktracebackend.repository.KnownIpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IpDetectionServiceTest {

    @Mock
    private KnownIpRepository knownIpRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private IpDetectionService ipDetectionService;

    private final UUID companyId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ipDetectionService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(ipDetectionService, "restTemplate", restTemplate);
    }

    @Test
    void testAnalyzeIpWithDetailsForLocalhost() {
        IpDetectionService.IpAnalysisResult result = ipDetectionService.analyzeIpWithDetails("127.0.0.1", companyId);

        assertTrue(result.flags().isEmpty(), "No deben generarse flags para localhost");
        assertNull(result.geoIpMap(), "No debe haber mapa de GeoIP para localhost");
        verify(knownIpRepository, never()).findByIp(anyString());
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    void testAnalyzeIpWithDetailsFromCache() {
        String ipAddress = "8.8.8.8";
        Map<String, Object> cachedData = new HashMap<>();
        Map<String, Object> securityData = new HashMap<>();
        securityData.put("is_vpn", true);
        cachedData.put("security", securityData);

        KnownIp cachedIp = new KnownIp();
        cachedIp.setIp(ipAddress);
        cachedIp.setGeoIpData(cachedData);

        when(knownIpRepository.findByIp(ipAddress)).thenReturn(Optional.of(cachedIp));

        IpDetectionService.IpAnalysisResult result = ipDetectionService.analyzeIpWithDetails(ipAddress, companyId);

        assertFalse(result.flags().isEmpty(), "Debe haber flags recuperados de la caché");
        assertTrue(result.flags().contains("VPN_DETECTED"), "El flag de VPN debe estar presente");
        assertEquals(cachedData, result.geoIpMap(), "El mapa de GeoIP debe ser el de la caché");
        verify(restTemplate, never()).getForObject(anyString(), any());
        verify(knownIpRepository, never()).saveNativeIp(anyString(), anyString(), any(), any(UUID.class));
    }

    @Test
    void testAnalyzeIpWithDetailsFromApiSuccess() {
        String ipAddress = "1.1.1.1";
        IpResponseDto apiResponse = new IpResponseDto();
        IpResponseDto.SecurityData securityData = new IpResponseDto.SecurityData();
        securityData.setVpn(false);
        securityData.setProxy(true);
        apiResponse.setSecurity(securityData);

        when(knownIpRepository.findByIp(ipAddress)).thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(IpResponseDto.class))).thenReturn(apiResponse);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(new Company()));


        IpDetectionService.IpAnalysisResult result = ipDetectionService.analyzeIpWithDetails(ipAddress, companyId);

        assertFalse(result.flags().isEmpty(), "Debe haber flags generados desde la API");
        assertTrue(result.flags().contains("PROXY_DETECTED"), "El flag de Proxy debe estar presente");
        assertFalse(result.flags().contains("VPN_DETECTED"), "El flag de VPN no debe estar presente");
        assertNotNull(result.geoIpMap(), "El mapa de GeoIP no debe ser nulo");
        verify(knownIpRepository, times(1)).saveNativeIp(eq(ipAddress), anyString(), any(), eq(companyId));
    }

    @Test
    void testAnalyzeIpWithDetailsApiCallFails() {
        String ipAddress = "2.2.2.2";
        when(knownIpRepository.findByIp(ipAddress)).thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(IpResponseDto.class))).thenThrow(new RuntimeException("Error de API"));

        IpDetectionService.IpAnalysisResult result = ipDetectionService.analyzeIpWithDetails(ipAddress, companyId);

        assertTrue(result.flags().isEmpty(), "No debe haber flags si la llamada a la API falla");
        assertNull(result.geoIpMap(), "El mapa de GeoIP debe ser nulo si la llamada a la API falla");
        verify(knownIpRepository, never()).saveNativeIp(anyString(), anyString(), any(), any(UUID.class));
    }

    @Test
    void testExtractFlagsFromMap() {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> securityData = new HashMap<>();
        securityData.put("is_vpn", true);
        securityData.put("is_tor", false);
        securityData.put("is_proxy", true);
        data.put("security", securityData);

        java.util.List<String> flags = ReflectionTestUtils.invokeMethod(ipDetectionService, "extractFlagsFromMap", data);

        assertNotNull(flags);
        assertEquals(2, flags.size());
        assertTrue(flags.contains("VPN_DETECTED"));
        assertTrue(flags.contains("PROXY_DETECTED"));
        assertFalse(flags.contains("TOR_NETWORK"));
    }

    @Test
    void testExtractFlagsFromMapWithNoSecurityData() {
        Map<String, Object> data = new HashMap<>();
        data.put("location", Collections.singletonMap("city", "Test City"));

        java.util.List<String> flags = ReflectionTestUtils.invokeMethod(ipDetectionService, "extractFlagsFromMap", data);

        assert flags != null;
        assertTrue(flags.isEmpty(), "No deben extraerse flags si no hay datos de seguridad");
    }
}
