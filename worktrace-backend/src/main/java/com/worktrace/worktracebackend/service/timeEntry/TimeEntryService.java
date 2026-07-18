package com.worktrace.worktracebackend.service.timeEntry;

import com.worktrace.worktracebackend.dto.auditTimeEntry.AuditRecordDto;
import com.worktrace.worktracebackend.dto.timeEntry.EditTimeEntryRequestDto;
import com.worktrace.worktracebackend.dto.timeEntry.VoidTimeEntryRequestDto;
import com.worktrace.worktracebackend.exception.NotFoundException;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import com.worktrace.worktracebackend.service.auditTimeEntry.AuditTimeEntryService;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.files.AdminPdfGeneratorService;
import com.worktrace.worktracebackend.service.files.EmployeePdfGeneratorService;
import com.worktrace.worktracebackend.service.files.ExcelGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio principal para gestionar toda la lógica de negocio relacionada con los fichajes (Time Entries).
 * Este servicio centraliza las operaciones de creación, consulta, modificación y anulación de fichajes,
 * así como el cálculo de estadísticas y la generación de informes tanto para empleados como para administradores.
 */
@Service
@RequiredArgsConstructor
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final UserService userService;
    private final EmployeePdfGeneratorService employeePdfGeneratorService;
    private final AdminPdfGeneratorService adminPdfGeneratorService;
    private final ExcelGeneratorService excelGeneratorService;
    private final ObjectMapper objectMapper;
    private final AuditTimeEntryService auditTimeEntryService;

    /**
     * Genera y exporta el historial de fichajes del empleado autenticado en formato PDF.
     * Este método recopila los datos y los delega al servicio de generación de PDF para crear
     * un informe que el empleado puede descargar.
     *
     * @param startDate La fecha de inicio del informe.
     * @param endDate   La fecha de fin del informe.
     * @return Un array de bytes (byte[]) que representa el archivo PDF.
     */
    @Transactional(readOnly = true)
    public byte[] exportEmployeeHistoryPdf(LocalDate startDate, LocalDate endDate) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();

        List<TimeEntry> timeEntries = timeEntryRepository
                .findTimeEntriesByEmployee_UserIdAndWorkDateBetweenOrderByWorkDateDesc(
                        info.getProfile().getUserId(),
                        startDate,
                        endDate
                );

        return employeePdfGeneratorService.generateTimeEntriesPdf(
                info.getProfile(),
                info.getUser(),
                timeEntries,
                startDate,
                endDate
        );
    }

    /**
     * Permite a un administrador modificar un fichaje existente.
     * Esta operación requiere una justificación y registra un evento de auditoría detallado
     * que incluye el estado del fichaje antes y después del cambio, garantizando la trazabilidad.
     *
     * @param id  El UUID del fichaje a modificar.
     * @param dto El DTO con los nuevos datos del fichaje y la justificación.
     */
    @Transactional
    public void updateTimeEntry(UUID id, EditTimeEntryRequestDto dto) {
        if (dto.getJustification() == null || dto.getJustification().trim().length() < 10) {
            throw new IllegalArgumentException("La justificación es obligatoria y debe tener al menos 10 caracteres.");
        }

        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();
        TimeEntry timeEntry = findAndValidateTimeEntry(id, company);

        try {
            String oldDataJson = objectMapper.writeValueAsString(timeEntry);

            timeEntry.setStartAt(dto.getStartAt());
            timeEntry.setWorkDate(dto.getStartAt().toLocalDate());

            if (dto.getEndAt() != null) {
                timeEntry.setEndAt(dto.getEndAt());
                timeEntry.setTimeEntryStatus(TimeEntryStatus.CLOSED);
            } else {
                timeEntry.setEndAt(null);
                timeEntry.setTimeEntryStatus(TimeEntryStatus.OPEN);
            }

            timeEntry.setModificationReason(dto.getJustification());
            timeEntry.setUpdatedAt(OffsetDateTime.now());

            String newDataJson = objectMapper.writeValueAsString(timeEntry);

            auditTimeEntryService.logTimeEntryChange("ADMIN_ADJUST", dto.getJustification(), oldDataJson, newDataJson, timeEntry.getId());
            auditTimeEntryService.saveTimeEntry(timeEntry);

        } catch (JacksonException e) {
            throw new RuntimeException("Error al generar los datos de auditoría", e);
        }
    }

    /**
     * Permite a un administrador anular un fichaje (borrado lógico).
     * El fichaje no se elimina de la base de datos, sino que se marca como anulado,
     * registrando quién lo hizo, cuándo y por qué. Esta acción también genera un evento de auditoría.
     *
     * @param id  El UUID del fichaje a anular.
     * @param dto El DTO con la justificación de la anulación.
     */
    @Transactional
    public void voidTimeEntry(UUID id, VoidTimeEntryRequestDto dto) {
        if (dto.getJustification() == null || dto.getJustification().trim().length() < 10) {
            throw new IllegalArgumentException("El motivo de anulación es obligatorio y debe tener al menos 10 caracteres.");
        }

        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();
        TimeEntry timeEntry = findAndValidateTimeEntry(id, company);

        try {
            String oldDataJson = objectMapper.writeValueAsString(timeEntry);

            timeEntry.setDeletedAt(OffsetDateTime.now());
            timeEntry.setDeletedBy(info.getUser());
            timeEntry.setDeleteReason(dto.getJustification());
            timeEntry.setUpdatedAt(OffsetDateTime.now());

            String newDataJson = objectMapper.writeValueAsString(timeEntry);

            auditTimeEntryService.logTimeEntryChange("SOFT_DELETE", dto.getJustification(), oldDataJson, newDataJson, timeEntry.getId());

            auditTimeEntryService.saveTimeEntry(timeEntry);

        } catch (JacksonException e) {
            throw new RuntimeException("Error al generar los datos de auditoría", e);
        }
    }

    /**
     * Genera y exporta un informe forense en PDF con los fichajes de la empresa en un rango de fechas.
     *
     * @param startDate La fecha de inicio del informe.
     * @param endDate   La fecha de fin del informe.
     * @return Un array de bytes (byte[]) que representa el archivo PDF.
     */
    @Transactional(readOnly = true)
    public byte[] exportCompanyReportAsPdf(LocalDate startDate, LocalDate endDate) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        List<TimeEntry> timeEntries = timeEntryRepository
                .findByCompany_IdAndWorkDateBetweenOrderByWorkDateDesc(company.getId(), startDate, endDate);

        List<AuditRecordDto> auditRecords = List.of();

        return adminPdfGeneratorService.generateCompanyTimeEntriesPdf(company, timeEntries, startDate, endDate, auditRecords);
    }

    /**
     * Genera y exporta un informe de auditoría en Excel con los fichajes de la empresa en un rango de fechas.
     *
     * @param startDate La fecha de inicio del informe.
     * @param endDate   La fecha de fin del informe.
     * @return Un array de bytes (byte[]) que representa el archivo Excel.
     */
    @Transactional(readOnly = true)
    public byte[] exportCompanyReportAsExcel(LocalDate startDate, LocalDate endDate) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        List<TimeEntry> timeEntries = timeEntryRepository
                .findByCompany_IdAndWorkDateBetweenOrderByWorkDateDesc(company.getId(), startDate, endDate);

        List<AuditRecordDto> auditRecords = List.of();

        return excelGeneratorService.generateCompanyTimeEntriesExcel(timeEntries, auditRecords, startDate, endDate);
    }

    private TimeEntry findAndValidateTimeEntry(UUID id, Company company) {
        Optional<TimeEntry> timeEntryOpt = timeEntryRepository.findById(id);
        if (timeEntryOpt.isEmpty()) {
            throw new NotFoundException("No se encontró el fichaje");
        }
        if (!timeEntryOpt.get().getCompany().getId().equals(company.getId())) {
            throw new IllegalStateException("El fichaje no pertenece a tu empresa");
        }
        return timeEntryOpt.get();
    }

}
