package com.worktrace.worktracebackend.service.files;

import com.worktrace.worktracebackend.model.Profile;
import com.worktrace.worktracebackend.model.TimeEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeEntryExportSupportTest {

    private TimeEntry baseTimeEntry;
    private Profile employeeProfile;

    @BeforeEach
    void setUp() {
        employeeProfile = new Profile();
        employeeProfile.setFullName("Juan Empleado de Prueba");
        employeeProfile.setEmployeeCode("EMP-123");

        baseTimeEntry = new TimeEntry();
        baseTimeEntry.setEmployee(employeeProfile);
        baseTimeEntry.setStartLat(new BigDecimal("40.123"));
        baseTimeEntry.setModificationReason(null);
        baseTimeEntry.setFlags(Collections.emptyList());
    }

    @Test
    void testEmployeeName() {
        assertAll("Pruebas para obtener el nombre del empleado",
                () -> assertEquals("Juan Empleado de Prueba", TimeEntryExportSupport.employeeName(baseTimeEntry), "Debe devolver el nombre completo si el perfil existe"),
                () -> {
                    baseTimeEntry.setEmployee(null);
                    assertEquals("?", TimeEntryExportSupport.employeeName(baseTimeEntry), "Debe devolver '?' si el perfil es nulo");
                }
        );
    }

    @Test
    void testEmployeeCode() {
        assertAll("Pruebas para obtener el código del empleado",
                () -> assertEquals("EMP-123", TimeEntryExportSupport.employeeCode(baseTimeEntry), "Debe devolver el código si existe"),
                () -> {
                    employeeProfile.setEmployeeCode(null);
                    assertEquals("-", TimeEntryExportSupport.employeeCode(baseTimeEntry), "Debe devolver '-' si el código es nulo");
                },
                () -> {
                    baseTimeEntry.setEmployee(null);
                    assertEquals("-", TimeEntryExportSupport.employeeCode(baseTimeEntry), "Debe devolver '-' si el perfil es nulo");
                }
        );
    }

    @Test
    void testFormatTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        OffsetDateTime time = OffsetDateTime.now();
        String expectedTime = formatter.format(time);

        assertAll("Pruebas para formatear tiempo",
                () -> assertEquals(expectedTime, TimeEntryExportSupport.formatTime(time, formatter, "N/A"), "Debe formatear la hora correctamente si no es nula"),
                () -> assertEquals("EN CURSO", TimeEntryExportSupport.formatTime(null, formatter, "EN CURSO"), "Debe devolver el valor por defecto si la hora es nula")
        );
    }

    @Test
    void testBooleanChecks() {
        assertAll("Pruebas para las comprobaciones booleanas de estado",
                () -> {
                    baseTimeEntry.setFlags(List.of("LOW_GPS_ACCURACY"));
                    assertTrue(TimeEntryExportSupport.hasLowGpsAccuracy(baseTimeEntry), "Debe ser verdadero si la bandera 'LOW_GPS_ACCURACY' existe");
                },
                () -> {
                    baseTimeEntry.setFlags(Collections.emptyList());
                    assertFalse(TimeEntryExportSupport.hasLowGpsAccuracy(baseTimeEntry), "Debe ser falso si no hay banderas");
                },
                () -> {
                    baseTimeEntry.setStartLat(null);
                    assertTrue(TimeEntryExportSupport.isGpsMissing(baseTimeEntry), "Debe ser verdadero si la latitud de inicio es nula");
                },
                () -> {
                    baseTimeEntry.setStartLat(new BigDecimal("1.0"));
                    assertFalse(TimeEntryExportSupport.isGpsMissing(baseTimeEntry), "Debe ser falso si la latitud de inicio existe");
                },
                () -> {
                    baseTimeEntry.setModificationReason("Ajuste manual");
                    assertTrue(TimeEntryExportSupport.isModified(baseTimeEntry), "Debe ser verdadero si hay una razón de modificación");
                },
                () -> {
                    baseTimeEntry.setModificationReason(" ");
                    assertFalse(TimeEntryExportSupport.isModified(baseTimeEntry), "Debe ser falso si la razón de modificación está en blanco");
                }
        );
    }

    @Nested
    @DisplayName("Tests for resolveExcelStatus")
    class ResolveExcelStatusTest {
        @Test
        void testReturnsOkStatus() {
            assertEquals("OK", TimeEntryExportSupport.resolveExcelStatus(baseTimeEntry, false), "Debe devolver 'OK' para un fichaje normal");
        }

        @Test
        void testReturnsLowGpsAlertStatus() {
            baseTimeEntry.setFlags(List.of("LOW_GPS_ACCURACY"));
            assertEquals("ALERTA GPS", TimeEntryExportSupport.resolveExcelStatus(baseTimeEntry, false), "Debe devolver 'ALERTA GPS' si la bandera está presente");
        }

        @Test
        void testReturnsNoGpsStatus() {
            baseTimeEntry.setStartLat(null);
            assertEquals("SIN GPS", TimeEntryExportSupport.resolveExcelStatus(baseTimeEntry, false), "Debe devolver 'SIN GPS' si falta la latitud");
        }

        @Test
        void testReturnsModifiedStatusWithAuditChanges() {
            assertEquals("MODIFICADO (Ver Hoja Auditoría)", TimeEntryExportSupport.resolveExcelStatus(baseTimeEntry, true), "Debe devolver 'MODIFICADO' si hay cambios de auditoría");
        }

        @Test
        void testReturnsModifiedStatusWithReason() {
            baseTimeEntry.setModificationReason("Ajuste");
            assertEquals("MODIFICADO (Ver Hoja Auditoría)", TimeEntryExportSupport.resolveExcelStatus(baseTimeEntry, false), "Debe devolver 'MODIFICADO' si hay una razón de modificación");
        }
    }

    @Nested
    @DisplayName("Tests for resolvePdfStatus and Color")
    class ResolvePdfStatusTest {
        @Test
        void testReturnsOkStatus() {
            assertEquals("OK", TimeEntryExportSupport.resolvePdfStatus(baseTimeEntry), "Debe devolver 'OK' para un fichaje normal");
            assertEquals(Color.BLACK, TimeEntryExportSupport.resolvePdfStatusColor(baseTimeEntry), "El color para 'OK' debe ser negro");
        }

        @Test
        void testReturnsModifiedStatus() {
            baseTimeEntry.setModificationReason("Ajuste manual");
            assertEquals("MODIFICADO", TimeEntryExportSupport.resolvePdfStatus(baseTimeEntry), "Debe devolver 'MODIFICADO' si hay razón de modificación");
            assertEquals(new Color(255, 140, 0), TimeEntryExportSupport.resolvePdfStatusColor(baseTimeEntry), "El color para 'MODIFICADO' debe ser naranja");
        }

        @Test
        void testReturnsNoGpsStatus() {
            baseTimeEntry.setStartLat(null);
            assertEquals("SIN GPS", TimeEntryExportSupport.resolvePdfStatus(baseTimeEntry), "Debe devolver 'SIN GPS' si falta la latitud");
            assertEquals(Color.BLACK, TimeEntryExportSupport.resolvePdfStatusColor(baseTimeEntry), "El color para 'SIN GPS' debe ser negro");
        }

        @Test
        void testReturnsLowGpsAlertStatus() {
            baseTimeEntry.setFlags(List.of("LOW_GPS_ACCURACY"));
            assertEquals("ALERTA GPS", TimeEntryExportSupport.resolvePdfStatus(baseTimeEntry), "Debe devolver 'ALERTA GPS' si la bandera está presente");
            assertEquals(new Color(200, 0, 0), TimeEntryExportSupport.resolvePdfStatusColor(baseTimeEntry), "El color para 'ALERTA GPS' debe ser rojo oscuro");
        }
    }
}
