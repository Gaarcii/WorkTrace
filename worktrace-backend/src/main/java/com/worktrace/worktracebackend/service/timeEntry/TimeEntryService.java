package com.worktrace.worktracebackend.service.timeEntry;

import com.worktrace.worktracebackend.dto.auditTimeEntry.AuditRecordDto;
import com.worktrace.worktracebackend.dto.timeEntry.*;
import com.worktrace.worktracebackend.exception.NotFoundException;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import com.worktrace.worktracebackend.service.auditTimeEntry.AuditTimeEntryService;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.files.AdminPdfGeneratorService;
import com.worktrace.worktracebackend.service.files.EmployeePdfGeneratorService;
import com.worktrace.worktracebackend.service.files.ExcelGeneratorService;
import com.worktrace.worktracebackend.service.ip.IpDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
    private final IpDetectionService ipDetectionService;
    private final EmployeePdfGeneratorService employeePdfGeneratorService;
    private final AdminPdfGeneratorService adminPdfGeneratorService;
    private final ExcelGeneratorService excelGeneratorService;
    private final ObjectMapper objectMapper;
    private final AuditTimeEntryService auditTimeEntryService;

    /**
     * Procesa un nuevo fichaje, ya sea de entrada o de salida.
     * Si el empleado ya tiene un fichaje abierto, este método lo cierra. Si no, crea uno nuevo.
     * Además, analiza la IP para detectar posibles anomalías (VPN, Tor) y la precisión del GPS,
     * añadiendo las banderas correspondientes para auditoría.
     *
     * @param requestDto Datos del fichaje enviados por el cliente (latitud, longitud, precisión).
     * @param realIp     La dirección IP real del cliente.
     * @param userAgent  El User-Agent del navegador o dispositivo del cliente.
     * @return Un DTO {@link TimeEntryResponseDto} con el resultado del fichaje procesado.
     */
    @Transactional
    public TimeEntryResponseDto processTimeEntry(TimeEntryRequestDto requestDto, String realIp, String userAgent) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        User user = info.getUser();
        Company company = info.getCompany();
        Profile profile = info.getProfile();

        Optional<TimeEntry> openTimeEntryOpt = timeEntryRepository.
                findByEmployee_UserIdAndEndAtIsNullAndTimeEntryStatus(profile.getUserId(), TimeEntryStatus.OPEN);

        IpDetectionService.IpAnalysisResult ipResult = ipDetectionService.
                analyzeIpWithDetails(realIp, company.getId());

        List<String> flags = new ArrayList<>();
        if (requestDto.getAccuracyMeters() != null && requestDto.getAccuracyMeters() > 200) {
            flags.add("LOW_GPS_ACCURACY");
        }
        if (ipResult.flags() != null) {
            flags.addAll(ipResult.flags());
        }

        TimeEntry savedTimeEntry;

        if (openTimeEntryOpt.isPresent()) {
            TimeEntry openTimeEntry = openTimeEntryOpt.get();

            openTimeEntry.setEndAt(OffsetDateTime.now());
            openTimeEntry.setEndLat(requestDto.getLat());
            openTimeEntry.setEndLng(requestDto.getLng());
            openTimeEntry.setEndAccuracyM(requestDto.getAccuracyMeters());
            openTimeEntry.setEndIp(realIp);
            openTimeEntry.setEndUserAgent(userAgent);
            openTimeEntry.setEndGeoip(ipResult.geoIpMap());

            if (openTimeEntry.getFlags() != null) {
                openTimeEntry.getFlags().addAll(flags);
            } else {
                openTimeEntry.setFlags(flags);
            }

            openTimeEntry.setTimeEntryStatus(TimeEntryStatus.CLOSED);
            savedTimeEntry = timeEntryRepository.save(openTimeEntry);

        } else {
            TimeEntry newTimeEntry = new TimeEntry();
            newTimeEntry.setEmployee(profile);
            newTimeEntry.setCompany(company);
            newTimeEntry.setCreatedBy(user);

            newTimeEntry.setWorkDate(LocalDate.now());
            newTimeEntry.setStartAt(OffsetDateTime.now());
            newTimeEntry.setCreatedAt(OffsetDateTime.now());

            newTimeEntry.setStartLat(requestDto.getLat());
            newTimeEntry.setStartLng(requestDto.getLng());
            newTimeEntry.setStartAccuracyM(requestDto.getAccuracyMeters());
            newTimeEntry.setStartIp(realIp);
            newTimeEntry.setStartUserAgent(userAgent);
            newTimeEntry.setStartGeoip(ipResult.geoIpMap());

            newTimeEntry.setFlags(flags);
            newTimeEntry.setTimeEntryStatus(TimeEntryStatus.OPEN);

            savedTimeEntry = timeEntryRepository.save(newTimeEntry);
        }

        TimeEntryResponseDto response = new TimeEntryResponseDto();
        response.setId(savedTimeEntry.getId());
        response.setStartAt(savedTimeEntry.getStartAt());
        response.setEndAt(savedTimeEntry.getEndAt());
        response.setStatus(savedTimeEntry.getTimeEntryStatus().name());

        return response;
    }

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
     * Obtiene todos los fichajes de una fecha específica para todos los empleados de la empresa.
     *
     * @param date La fecha para la cual se quieren obtener los fichajes.
     * @return Una lista de DTOs {@link AdminTimeEntryByDateResponseDto} con los fichajes del día.
     */
    @Transactional(readOnly = true)
    public List<AdminTimeEntryByDateResponseDto> getTimeEntriesByDateForCompany(LocalDate date) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        List<TimeEntry> timeEntries = timeEntryRepository
                .findByCompany_IdAndWorkDateOrderByStartAtDesc(company.getId(), date);

        return timeEntries.stream().map(timeEntry -> {
            Long workedMinutes = null;
            if (timeEntry.getStartAt() != null && timeEntry.getEndAt() != null) {
                workedMinutes = Duration.between(timeEntry.getStartAt(), timeEntry.getEndAt()).toMinutes();
            }

            Profile employee = timeEntry.getEmployee();
            String jobPositionTitle = employee.getPosition() != null ? employee.getPosition().getTitle() : null;

            return new AdminTimeEntryByDateResponseDto(
                    timeEntry.getId(),
                    employee.getUserId(),
                    employee.getFullName(),
                    jobPositionTitle,
                    employee.getAvatarUrl(),
                    timeEntry.getWorkDate(),
                    timeEntry.getStartAt(),
                    timeEntry.getEndAt(),
                    workedMinutes
            );
        }).toList();
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
