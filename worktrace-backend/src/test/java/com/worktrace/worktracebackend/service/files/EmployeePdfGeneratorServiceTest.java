package com.worktrace.worktracebackend.service.files;

import com.worktrace.worktracebackend.model.*;
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
class EmployeePdfGeneratorServiceTest {

    @Mock
    private PdfHelperService pdfHelper;

    @InjectMocks
    private EmployeePdfGeneratorService employeePdfGeneratorService;

    private Profile testProfile;
    private User testUser;
    private TimeEntry testTimeEntry;

    @BeforeEach
    void setUp() {
        Company testCompany = new Company();
        testCompany.setCompanyName("Mi Empresa de Prueba");
        testCompany.setCif("B12345678");
        testCompany.setLogoUrl("http://example.com/logo.png");

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setCompany(testCompany);

        testProfile = new Profile();
        testProfile.setUserId(testUser.getId());
        testProfile.setFullName("Juan Empleado");
        testProfile.setEmployeeCode("E123");
        testUser.setProfile(testProfile);

        testTimeEntry = new TimeEntry();
        testTimeEntry.setId(UUID.randomUUID());
        testTimeEntry.setEmployee(testProfile);
        testTimeEntry.setWorkDate(LocalDate.now());
        testTimeEntry.setStartAt(OffsetDateTime.now().minusHours(8));
        testTimeEntry.setEndAt(OffsetDateTime.now());
        testTimeEntry.setTimeEntryStatus(TimeEntryStatus.CLOSED);
    }

    @Test
    void testGenerateTimeEntriesPdf_SuccessWithData() {
        when(pdfHelper.generateSha256Hash(anyString())).thenReturn("mocked_hash_value");
        try {
            doNothing().when(pdfHelper).addCompanyHeader(any(), any(Company.class), anyString());
        } catch (Exception e) {
        }

        byte[] result = employeePdfGeneratorService.generateTimeEntriesPdf(
                testProfile,
                testUser,
                List.of(testTimeEntry),
                LocalDate.now().minusDays(1),
                LocalDate.now()
        );

        assertNotNull(result, "El PDF generado no debería ser nulo");
        assertTrue(result.length > 0, "El PDF generado debería tener contenido");
    }

    @Test
    void testGenerateTimeEntriesPdf_SuccessWithNoTimeEntries() {
        when(pdfHelper.generateSha256Hash(anyString())).thenReturn("mocked_hash_value_no_entries");
        try {
            doNothing().when(pdfHelper).addCompanyHeader(any(), any(Company.class), anyString());
        } catch (Exception e) {
        }

        byte[] result = employeePdfGeneratorService.generateTimeEntriesPdf(
                testProfile,
                testUser,
                Collections.emptyList(),
                LocalDate.now().minusDays(1),
                LocalDate.now()
        );

        assertNotNull(result, "El PDF generado sin fichajes no debería ser nulo");
        assertTrue(result.length > 0, "El PDF generado sin fichajes debería tener contenido");
    }
}
