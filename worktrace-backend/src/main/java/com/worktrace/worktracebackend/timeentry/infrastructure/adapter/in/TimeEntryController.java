package com.worktrace.worktracebackend.timeentry.infrastructure.adapter.in;

import com.worktrace.worktracebackend.dto.timeEntry.*;
import com.worktrace.worktracebackend.service.timeEntry.TimeEntryService;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetTimeEntryCountTodayUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetTotalHoursTodayUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Controlador para gestionar los fichajes (entradas y salidas) de los empleados.
 * Proporciona funcionalidades tanto para los trabajadores (fichar, ver su historial)
 * como para los administradores (ver fichajes de la empresa, gestionar y exportar informes).
 */
@RestController
@RequestMapping("/api/time-entries")
@RequiredArgsConstructor
public class TimeEntryController {

    private final TimeEntryService timeEntryService;
    private final GetTimeEntryCountTodayUseCase getTimeEntryCountTodayUseCase;
    private final GetTotalHoursTodayUseCase getTotalHoursTodayUseCase;

    /**
     * Registra un nuevo fichaje (entrada o salida) para el trabajador autenticado.
     * Captura la dirección IP y el User-Agent para fines de auditoría y seguridad.
     *
     * @param requestDto  Datos del fichaje, como el tipo (entrada/salida).
     * @param httpRequest Información de la petición para obtener datos de auditoría.
     * @return El fichaje que ha sido creado.
     */
    @PostMapping("/clock-in")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<TimeEntryResponseDto> createTimeEntry(
            @Valid @RequestBody TimeEntryRequestDto requestDto,
            HttpServletRequest httpRequest) {

        String realIp = httpRequest.getRemoteAddr();
        realIp = realIp.replace("/", "");

        String userAgent = httpRequest.getHeader("User-Agent");
        TimeEntryResponseDto response = timeEntryService.processTimeEntry(requestDto, realIp, userAgent);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un resumen diario de los fichajes del trabajador autenticado.
     * Útil para que el trabajador vea su estado actual y las horas trabajadas en el día.
     * @return Un resumen con los fichajes del día y el total de horas.
     */
    @GetMapping("/daily-summary")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<DailySummaryResponseDto> getDailySummary() {
        DailySummaryResponseDto responseDto = timeEntryService.getDailySummary();
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Obtiene el historial de fichajes de un día específico para el trabajador autenticado.
     * @param date La fecha para la cual se quiere obtener el historial.
     * @return El historial de fichajes para la fecha especificada.
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<HistoryResponseDto> getHistoryByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        HistoryResponseDto history = timeEntryService.getHistoryByDate(date);
        return ResponseEntity.ok(history);
    }

    /**
     * Obtiene estadísticas de fichajes (como horas totales) para el trabajador autenticado en un rango de fechas.
     * Si no se especifica un rango, se calculan desde el primer fichaje hasta la fecha actual.
     * @param startDate Fecha de inicio para el cálculo de estadísticas.
     * @param endDate Fecha de fin para el cálculo de estadísticas.
     * @return Las estadísticas calculadas.
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<StatisticsResponseDto> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = timeEntryService.getFirstTimeEntryDateForEmployee();
        }
        StatisticsResponseDto statistics = timeEntryService.getStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Exporta el historial de fichajes del trabajador autenticado a un archivo PDF.
     * @param startDate Fecha de inicio del informe.
     * @param endDate Fecha de fin del informe.
     * @return Un archivo PDF con el historial de fichajes.
     */
    @GetMapping("/statistics/export")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<byte[]> exportStatisticsPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (endDate == null) {
            endDate = LocalDate.now();
        }

        if (startDate == null) {
            startDate = timeEntryService.getFirstTimeEntryDateForEmployee();
        }

        byte[] pdfBytes = timeEntryService.exportEmployeeHistoryPdf(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        String fileName = "fichajes_" + startDate + "_al_" + endDate + ".pdf";
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    /**
     * Obtiene una lista de los trabajadores que se encuentran actualmente con una sesión de trabajo activa.
     * @return Lista de trabajadores activos.
     */
    @GetMapping("/active-workers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActiveWorkerDto>> getActiveWorkers() {
        List<ActiveWorkerDto> activeWorkerDto = timeEntryService.getActiveWorkers();
        return ResponseEntity.ok(activeWorkerDto);
    }

    /**
     * Devuelve el número total de fichajes realizados en el día de hoy en la empresa.
     * @return El recuento de fichajes de hoy.
     */
    @GetMapping("/count-today")
    @PreAuthorize("hasRole('ADMIN')")
    public Long getTimeEntriesCountToday() {
        return getTimeEntryCountTodayUseCase.execute();
    }

    /**
     * Calcula y devuelve el total de horas trabajadas por todos los empleados de la empresa en el día de hoy.
     * @return Un DTO con el total de horas y minutos.
     */
    @GetMapping("/hours-today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TotalHoursTodayResponseDto> getTotalHoursToday() {
        Long minutes = getTotalHoursTodayUseCase.execute();
        return ResponseEntity.ok(new TotalHoursTodayResponseDto(minutes));
    }

    /**
     * Obtiene los datos necesarios para construir un gráfico semanal del número de fichajes por día.
     * @param startDate Fecha de inicio de la semana.
     * @param endDate Fecha de fin de la semana.
     * @return Una lista con el recuento de fichajes por día.
     */
    @GetMapping("/weekly-chart")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DailyTimeEntryCountDto>> getWeeklyChartData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<DailyTimeEntryCountDto> dailyTimeEntryCounts =
                timeEntryService.getWeeklyTimeEntryCountChartData(startDate, endDate);
        return ResponseEntity.ok(dailyTimeEntryCounts);

    }

    /**
     * Obtiene todos los fichajes de una fecha específica para todos los empleados de la empresa.
     * @param date La fecha de la que se quieren obtener los fichajes.
     * @return Una lista de fichajes para la fecha indicada.
     */
    @GetMapping("/admin/by-date")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminTimeEntryByDateResponseDto>> getTimeEntriesByDateForCompany(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AdminTimeEntryByDateResponseDto> response = timeEntryService.getTimeEntriesByDateForCompany(date);
        return ResponseEntity.ok(response);
    }

    /**
     * Permite a un administrador modificar la hora de un fichaje existente.
     * @param id El ID del fichaje a modificar.
     * @param dto Los nuevos datos para el fichaje.
     * @return Una respuesta vacía si la operación fue exitosa.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateTimeEntry(
            @PathVariable UUID id,
            @Valid @RequestBody EditTimeEntryRequestDto dto) {
        timeEntryService.updateTimeEntry(id, dto);
        return ResponseEntity.ok().build();
    }

    /**
     * Permite a un administrador anular un fichaje, especificando un motivo.
     * El fichaje no se elimina, sino que se marca como anulado.
     * @param id El ID del fichaje a anular.
     * @param dto El motivo de la anulación.
     * @return Una respuesta vacía si la operación fue exitosa.
     */
    @PatchMapping("/{id}/void")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> voidTimeEntry(
            @PathVariable UUID id,
            @Valid @RequestBody VoidTimeEntryRequestDto dto) {
        timeEntryService.voidTimeEntry(id, dto);
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene una lista paginada de todos los fichajes de un empleado específico.
     * @param employeeId El ID del empleado.
     * @param pageable Información de paginación.
     * @return Una página con los fichajes del empleado.
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TimeEntryTableResponseDto>> getTimeEntriesByEmployee(
            @PathVariable UUID employeeId,
            Pageable pageable) {
        Page<TimeEntryTableResponseDto> response = timeEntryService
                .getTimeEntriesByEmployee(employeeId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Exporta un informe forense completo de los fichajes de la empresa en formato PDF.
     * Este informe está diseñado para auditorías e inspecciones.
     * @param startDate Fecha de inicio del informe.
     * @param endDate Fecha de fin del informe.
     * @return Un archivo PDF con el informe.
     */
    @GetMapping("/admin/export/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','INSPECTOR')")
    public ResponseEntity<byte[]> exportCompanyReportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        byte[] pdfBytes = timeEntryService.exportCompanyReportAsPdf(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        String fileName = "informe_forense_" + startDate + "_al_" + endDate + ".pdf";
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    /**
     * Exporta un informe de auditoría de los fichajes de la empresa en formato Excel.
     * @param startDate Fecha de inicio del informe.
     * @param endDate Fecha de fin del informe.
     * @return Un archivo Excel con el informe.
     */
    @GetMapping("/admin/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN','INSPECTOR')")
    public ResponseEntity<byte[]> exportCompanyReportExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        byte[] excelBytes = timeEntryService.exportCompanyReportAsExcel(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        String fileName = "AUDITORIA_DATOS_" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

}
