package com.worktrace.worktracebackend.service.files;

import com.worktrace.worktracebackend.dto.auditTimeEntry.AuditRecordDto;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.Profile;
import com.worktrace.worktracebackend.model.TimeEntry;
import com.worktrace.worktracebackend.model.User;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelGeneratorServiceTest {

    @Mock
    private PdfHelperService pdfHelper;

    @InjectMocks
    private ExcelGeneratorService excelGeneratorService;

    private List<TimeEntry> timeEntries;
    private List<AuditRecordDto> auditTrail;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID timeEntryId;

    @BeforeEach
    void setUp() {
        timeEntryId = UUID.randomUUID();
        startDate = LocalDate.of(2023, 1, 1);
        endDate = LocalDate.of(2023, 1, 31);

        Company company = new Company();
        company.setCompanyName("Nombre de Empresa de Prueba");

        User user = new User();

        Profile employeeProfile = new Profile();
        employeeProfile.setUser(user);
        employeeProfile.setFullName("Nombre Completo Empleado");
        employeeProfile.setEmployeeCode("EMP-001");
        user.setProfile(employeeProfile);

        TimeEntry timeEntry = TimeEntry.builder()
                .id(timeEntryId)
                .employee(employeeProfile)
                .company(company)
                .workDate(LocalDate.of(2023, 1, 15))
                .startAt(OffsetDateTime.of(2023, 1, 15, 9, 0, 0, 0, ZoneOffset.UTC))
                .endAt(OffsetDateTime.of(2023, 1, 15, 17, 0, 0, 0, ZoneOffset.UTC))
                .startLat(new BigDecimal("40.416775"))
                .startLng(new BigDecimal("-3.703790"))
                .startAccuracyM(10)
                .startIp("192.168.1.1")
                .build();
        timeEntries = Collections.singletonList(timeEntry);

        AuditRecordDto auditRecord = new AuditRecordDto(
                OffsetDateTime.of(2023, 1, 16, 10, 0, 0, 0, ZoneOffset.UTC),
                "Admin (Super Admin)",
                "Corrección de error manual",
                "Hora de salida ajustada de 16:55 a 17:00",
                timeEntryId
        );
        auditTrail = Collections.singletonList(auditRecord);
    }

    @Test
    void testGenerateCompanyTimeEntriesExcelSuccess() throws IOException {
        when(pdfHelper.generateSha256Hash(anyString())).thenReturn("mocked-hash-value");

        byte[] excelBytes = excelGeneratorService.generateCompanyTimeEntriesExcel(timeEntries, auditTrail, startDate, endDate);

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(excelBytes))) {
            assertAll(
                    () -> assertEquals(3, workbook.getNumberOfSheets(), "El libro debe contener 3 hojas"),
                    () -> assertNotNull(workbook.getSheet("Registro_Horario"), "Debe existir la hoja 'Registro_Horario'"),
                    () -> assertNotNull(workbook.getSheet("Traza_Auditoria"), "Debe existir la hoja 'Traza_Auditoria'"),
                    () -> assertNotNull(workbook.getSheet("Certificado_Integridad"), "Debe existir la hoja 'Certificado_Integridad'")
            );

            Sheet timeEntriesSheet = workbook.getSheet("Registro_Horario");
            assertEquals(2, timeEntriesSheet.getPhysicalNumberOfRows(), "La hoja de registros debe tener cabecera y una fila de datos");
            assertEquals("MODIFICADO (Ver Hoja Auditoría)", timeEntriesSheet.getRow(1).getCell(10).getStringCellValue(), "El estado de integridad debe ser 'MODIFICADO (Ver Hoja Auditoría)'");

            Sheet auditSheet = workbook.getSheet("Traza_Auditoria");
            assertEquals(2, auditSheet.getPhysicalNumberOfRows(), "La hoja de auditoría debe tener cabecera y una fila de datos");
            assertEquals(timeEntryId.toString(), auditSheet.getRow(1).getCell(4).getStringCellValue());

            Sheet certificateSheet = workbook.getSheet("Certificado_Integridad");
            assertEquals("mocked-hash-value", certificateSheet.getRow(4).getCell(1).getStringCellValue(), "El hash SHA-256 debe estar en el certificado");
        }
    }

    @Test
    void testGenerateExcelWithNoAuditTrail() throws IOException {
        when(pdfHelper.generateSha256Hash(anyString())).thenReturn("mocked-hash-value-no-audit");

        byte[] excelBytes = excelGeneratorService.generateCompanyTimeEntriesExcel(timeEntries, Collections.emptyList(), startDate, endDate);

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(excelBytes))) {
            assertAll(
                    () -> assertEquals(2, workbook.getNumberOfSheets(), "El libro debe contener 2 hojas si no hay auditoría"),
                    () -> assertNotNull(workbook.getSheet("Registro_Horario")),
                    () -> assertNull(workbook.getSheet("Traza_Auditoria"), "No debe existir la hoja de auditoría"),
                    () -> assertNotNull(workbook.getSheet("Certificado_Integridad"))
            );

            Sheet timeEntriesSheet = workbook.getSheet("Registro_Horario");
            assertEquals("OK", timeEntriesSheet.getRow(1).getCell(10).getStringCellValue(), "El estado de integridad debe ser 'OK'");
        }
    }

    @Test
    void testGenerateExcelFailsWhenDependencyThrowsException() {
        String expectedErrorMessage = "Fallo simulado en la generación del hash";
        when(pdfHelper.generateSha256Hash(anyString())).thenThrow(new RuntimeException(expectedErrorMessage));

        Exception exception = assertThrows(RuntimeException.class, () -> excelGeneratorService.generateCompanyTimeEntriesExcel(timeEntries, auditTrail, startDate, endDate));

        assertEquals(expectedErrorMessage, exception.getMessage());
    }
}
