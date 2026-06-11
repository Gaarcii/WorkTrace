package com.worktrace.worktracebackend.timeentry.infrastructure.adapter.in;

import com.worktrace.worktracebackend.dto.timeEntry.*;
import com.worktrace.worktracebackend.service.timeEntry.TimeEntryService;
import com.worktrace.worktracebackend.timeentry.domain.model.DailySummary;
import com.worktrace.worktracebackend.timeentry.domain.port.in.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Fichajes", description = "Registro y consulta de fichajes de trabajadores, e informes administrativos")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;
    private final GetTimeEntryCountTodayUseCase getTimeEntryCountTodayUseCase;
    private final GetTotalHoursTodayUseCase getTotalHoursTodayUseCase;
    private final GetFirstTimeEntryDateForEmployeeUseCase getFirstTimeEntryDateForEmployeeUseCase;
    private final GetWeeklyTimeEntryCountChartDataUseCase getWeeklyTimeEntryCountChartDataUseCase;
    private final GetActiveWorkersUseCase getActiveWorkersUseCase;
    private final GetDailySummaryUseCase getDailySummaryUseCase;
    /**
     * Registra un nuevo fichaje (entrada o salida) para el trabajador autenticado.
     * Captura la dirección IP y el User-Agent para fines de auditoría y seguridad.
     *
     * @param requestDto  Datos del fichaje, como el tipo (entrada/salida).
     * @param httpRequest Información de la petición para obtener datos de auditoría.
     * @return El fichaje que ha sido creado.
     */
    @Operation(summary = "Registrar fichaje", description = "Registra una entrada o salida para el trabajador autenticado, capturando IP y User-Agent para auditoría")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fichaje registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos del fichaje inválidos"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol WORKER")
    })
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
    @Operation(summary = "Resumen diario propio", description = "Devuelve los fichajes del día y el total de horas trabajadas del trabajador autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen obtenido correctamente"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol WORKER")
    })
    @GetMapping("/daily-summary")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<DailySummaryResponseDto> getDailySummary() {
        DailySummary dailySummary = getDailySummaryUseCase.execute();
        DailySummaryResponseDto dto = new DailySummaryResponseDto();
        dto.setAccumulatedMinutes(dailySummary.accumulatedMinutes());
        dto.setTargetMinutes(dailySummary.targetMinutes());
        dto.setEntryTime(dailySummary.entryTime());
        dto.setLastTimeEntries(dailySummary.lastTimeEntries().stream()
                .map(e -> new LastTimeEntriesResponseDto(e.timeEntryId(), e.eventType(), e.date()))
                .toList());
        return ResponseEntity.ok(dto);
    }

    /**
     * Obtiene el historial de fichajes de un día específico para el trabajador autenticado.
     * @param date La fecha para la cual se quiere obtener el historial.
     * @return El historial de fichajes para la fecha especificada.
     */
    @Operation(summary = "Historial por fecha", description = "Devuelve el historial de fichajes del trabajador autenticado para un día concreto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial obtenido correctamente"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol WORKER")
    })
    @GetMapping("/history")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<HistoryResponseDto> getHistoryByDate(
            @Parameter(description = "Fecha del historial a consultar", example = "2026-06-02")
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
    @Operation(summary = "Estadísticas propias", description = "Calcula estadísticas de fichajes del trabajador autenticado en un rango de fechas. Si se omiten, abarca desde el primer fichaje hasta hoy")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estadísticas calculadas correctamente"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol WORKER")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<StatisticsResponseDto> getStatistics(
            @Parameter(description = "Fecha de inicio (opcional, por defecto el primer fichaje)", example = "2026-06-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Fecha de fin (opcional, por defecto hoy)", example = "2026-06-02")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate =getFirstTimeEntryDateForEmployeeUseCase.execute();
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
    @Operation(summary = "Exportar historial propio a PDF", description = "Genera un PDF con el historial de fichajes del trabajador autenticado para el rango indicado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF generado correctamente"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol WORKER")
    })
    @GetMapping("/statistics/export")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<byte[]> exportStatisticsPdf(
            @Parameter(description = "Fecha de inicio del informe (opcional, por defecto el primer fichaje)", example = "2026-06-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Fecha de fin del informe (opcional, por defecto hoy)", example = "2026-06-02")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (endDate == null) {
            endDate = LocalDate.now();
        }

        if (startDate == null) {
            startDate =getFirstTimeEntryDateForEmployeeUseCase.execute();
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
    @Operation(summary = "Trabajadores activos", description = "Lista los trabajadores de la empresa con una sesión de trabajo actualmente abierta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN")
    })
    @GetMapping("/active-workers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActiveWorkerDto>> getActiveWorkers() {
        List<ActiveWorkerDto> activeWorkerDto = getActiveWorkersUseCase.execute()
                .stream()
                .map(
                        e -> new ActiveWorkerDto(
                                e.employeeId(),
                                e.fullName(),
                                e.jobPosition(),
                                e.avatarUrl(),
                                e.entryTime(),
                                e.punctualityMinutes()
                        )
                ).toList();
        return ResponseEntity.ok(activeWorkerDto);
    }

    /**
     * Devuelve el número total de fichajes realizados en el día de hoy en la empresa.
     * @return El recuento de fichajes de hoy.
     */
    @Operation(summary = "Nº de fichajes de hoy", description = "Devuelve el número total de fichajes registrados hoy en la empresa autenticada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recuento obtenido correctamente"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN")
    })
    @GetMapping("/count-today")
    @PreAuthorize("hasRole('ADMIN')")
    public Long getTimeEntriesCountToday() {
        return getTimeEntryCountTodayUseCase.execute();
    }

    /**
     * Calcula y devuelve el total de horas trabajadas por todos los empleados de la empresa en el día de hoy.
     * @return Un DTO con el total de horas y minutos.
     */
    @Operation(summary = "Horas trabajadas hoy", description = "Calcula el total de horas trabajadas por todos los empleados de la empresa en el día de hoy")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total calculado correctamente"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN")
    })
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
    @Operation(summary = "Datos de gráfico semanal", description = "Devuelve el recuento de fichajes por día en el rango indicado, para construir el gráfico semanal del panel de administración")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Datos del gráfico obtenidos correctamente"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN")
    })
    @GetMapping("/weekly-chart")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DailyTimeEntryCountDto>> getWeeklyChartData(
            @Parameter(description = "Fecha de inicio de la semana", example = "2026-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Fecha de fin de la semana", example = "2026-06-07")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<DailyTimeEntryCountDto> result = getWeeklyTimeEntryCountChartDataUseCase
                .execute(startDate, endDate)
                .stream()
                .map(e -> new DailyTimeEntryCountDto(e.date().toString(), e.count()))
                .toList();
        return ResponseEntity.ok(result);

    }

    /**
     * Obtiene todos los fichajes de una fecha específica para todos los empleados de la empresa.
     * @param date La fecha de la que se quieren obtener los fichajes.
     * @return Una lista de fichajes para la fecha indicada.
     */
    @Operation(summary = "Fichajes de la empresa por fecha", description = "Devuelve todos los fichajes de todos los empleados de la empresa para un día concreto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fichajes obtenidos correctamente"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN")
    })
    @GetMapping("/admin/by-date")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminTimeEntryByDateResponseDto>> getTimeEntriesByDateForCompany(
            @Parameter(description = "Fecha de los fichajes a consultar", example = "2026-06-02")
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
    @Operation(summary = "Ajustar fichaje", description = "Permite a un administrador modificar la hora de un fichaje existente. El cambio queda registrado en el log de auditoría")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fichaje ajustado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos del ajuste inválidos"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "No existe el fichaje indicado")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateTimeEntry(
            @Parameter(description = "Identificador del fichaje a ajustar")
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
    @Operation(summary = "Anular fichaje", description = "Marca un fichaje como anulado indicando un motivo. El fichaje no se elimina físicamente (soft delete) y queda registrado en auditoría")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fichaje anulado correctamente"),
            @ApiResponse(responseCode = "400", description = "Motivo de anulación inválido"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "No existe el fichaje indicado")
    })
    @PatchMapping("/{id}/void")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> voidTimeEntry(
            @Parameter(description = "Identificador del fichaje a anular")
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
    @Operation(summary = "Fichajes de un empleado (paginado)", description = "Devuelve una página con todos los fichajes de un empleado concreto de la empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de fichajes obtenida correctamente"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN")
    })
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TimeEntryTableResponseDto>> getTimeEntriesByEmployee(
            @Parameter(description = "Identificador del empleado")
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
    @Operation(summary = "Informe forense PDF", description = "Exporta un informe forense completo de los fichajes de la empresa en PDF, pensado para auditorías e inspecciones")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF generado correctamente"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN o INSPECTOR")
    })
    @GetMapping("/admin/export/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','INSPECTOR')")
    public ResponseEntity<byte[]> exportCompanyReportPdf(
            @Parameter(description = "Fecha de inicio del informe", example = "2026-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Fecha de fin del informe", example = "2026-06-02")
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
    @Operation(summary = "Informe de auditoría Excel", description = "Exporta un informe de auditoría de los fichajes de la empresa en formato Excel (.xlsx)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Excel generado correctamente"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN o INSPECTOR")
    })
    @GetMapping("/admin/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN','INSPECTOR')")
    public ResponseEntity<byte[]> exportCompanyReportExcel(
            @Parameter(description = "Fecha de inicio del informe", example = "2026-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Fecha de fin del informe", example = "2026-06-02")
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
