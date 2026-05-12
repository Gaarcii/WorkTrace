package com.worktrace.worktracebackend.service.files;

import com.worktrace.worktracebackend.dto.auditTimeEntry.AuditRecordDto;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.Profile;
import com.worktrace.worktracebackend.model.TimeEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPdfGeneratorServiceTest {

    @Mock
    private PdfHelperService pdfHelper;

    @InjectMocks
    private AdminPdfGeneratorService adminPdfGeneratorService;

    private Company testCompany;
    private TimeEntry testTimeEntry;
    private AuditRecordDto testAuditRecord;

    @BeforeEach
    void setUp() {
        testCompany = new Company();
        testCompany.setCompanyName("Mi Empresa de Prueba");
        testCompany.setCif("B12345678");
        testCompany.setLogoUrl("http://example.com/logo.png");

        Profile employeeProfile = new Profile();
        employeeProfile.setUserId(UUID.randomUUID());
        employeeProfile.setFullName("Juan Empleado");
        employeeProfile.setEmployeeCode("E123");

        testTimeEntry = new TimeEntry();
        testTimeEntry.setId(UUID.randomUUID());
        testTimeEntry.setEmployee(employeeProfile);
        testTimeEntry.setWorkDate(LocalDate.now());
        testTimeEntry.setStartAt(OffsetDateTime.now().minusHours(8));
        testTimeEntry.setEndAt(OffsetDateTime.now());
        testTimeEntry.setModificationReason(null);

        testAuditRecord = new AuditRecordDto(
                OffsetDateTime.now(),
                "Admin",
                "Corrección de error manual",
                "Cambio de hora de salida",
                testTimeEntry.getId()
        );
    }

    @Test
    void testGenerateCompanyTimeEntriesPdf_SuccessWithAllData() {
        when(pdfHelper.generateSha256Hash(anyString())).thenReturn("mocked_hash_value");
        try {
            doNothing().when(pdfHelper).addCompanyHeader(any(), any(Company.class), anyString());
        } catch (Exception e) {
        }

        byte[] result = adminPdfGeneratorService.generateCompanyTimeEntriesPdf(
                testCompany,
                List.of(testTimeEntry),
                LocalDate.now().minusDays(1),
                LocalDate.now(),
                List.of(testAuditRecord)
        );

        assertNotNull(result, "El PDF generado no debería ser nulo");
        assertTrue(result.length > 0, "El PDF generado debería tener contenido");
    }

    @Test
    void testGenerateCompanyTimeEntriesPdf_SuccessWithNoAuditTrail() {
        when(pdfHelper.generateSha256Hash(anyString())).thenReturn("mocked_hash_value_no_audit");
        try {
            doNothing().when(pdfHelper).addCompanyHeader(any(), any(Company.class), anyString());
        } catch (Exception e) {
        }

        byte[] result = adminPdfGeneratorService.generateCompanyTimeEntriesPdf(
                testCompany,
                List.of(testTimeEntry),
                LocalDate.now().minusDays(1),
                LocalDate.now(),
                Collections.emptyList()
        );

        assertNotNull(result, "El PDF generado sin auditoría no debería ser nulo");
        assertTrue(result.length > 0, "El PDF generado sin auditoría debería tener contenido");
    }

    @Test
    void testGenerateCompanyTimeEntriesPdf_SuccessWithNoTimeEntries() {
        when(pdfHelper.generateSha256Hash(anyString())).thenReturn("mocked_hash_value_no_entries");
        try {
            doNothing().when(pdfHelper).addCompanyHeader(any(), any(Company.class), anyString());
        } catch (Exception e) {
        }

        byte[] result = adminPdfGeneratorService.generateCompanyTimeEntriesPdf(
                testCompany,
                Collections.emptyList(),
                LocalDate.now().minusDays(1),
                LocalDate.now(),
                Collections.emptyList()
        );

        assertNotNull(result, "El PDF generado sin fichajes no debería ser nulo");
        assertTrue(result.length > 0, "El PDF generado sin fichajes debería tener contenido");
    }
}
