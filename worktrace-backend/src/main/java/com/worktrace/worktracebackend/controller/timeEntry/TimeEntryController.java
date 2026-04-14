package com.worktrace.worktracebackend.controller.timeEntry;

import com.worktrace.worktracebackend.dto.timeEntry.*;
import com.worktrace.worktracebackend.service.timeEntry.TimeEntryService;
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

@RestController
@RequestMapping("/api/time-entries")
@RequiredArgsConstructor
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    @PostMapping("/fichar")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<TimeEntryResponseDto> fichar(
            @Valid @RequestBody TimeEntryRequestDto requestDto,
            HttpServletRequest httpRequest) {

        String ipReal = httpRequest.getRemoteAddr();
        ipReal = ipReal.replace("/", "");

        String userAgent = httpRequest.getHeader("User-Agent");
        TimeEntryResponseDto response = timeEntryService.procesarFichaje(requestDto, ipReal, userAgent);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/resumenDiario")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ResumenDiarioResponseDto> resumenDiario() {
        ResumenDiarioResponseDto responseDto = timeEntryService.getResumenDiario();
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/historial")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<HistorialResponseDto> getHistorial(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        HistorialResponseDto historial = timeEntryService.getHistorial(fecha);
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<EstadisticasResponseDto> getEstadisticas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        if (fechaFin == null) {
            fechaFin = LocalDate.now();
        }
        if (fechaInicio == null) {
            fechaInicio = timeEntryService.primerFichaje();
        }
        EstadisticasResponseDto estadisticas = timeEntryService.getEstadisticas(fechaInicio, fechaFin);
        return ResponseEntity.ok(estadisticas);
    }

    @GetMapping("/estadisticas/exportar")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<byte[]> descargarInformePdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        if (fechaFin == null) {
            fechaFin = LocalDate.now();
        }

        if (fechaInicio == null) {
            fechaInicio = timeEntryService.primerFichaje();
        }

        byte[] pdfBytes = timeEntryService.exportarHistorialPdf(fechaInicio, fechaFin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        String nombreArchivo = "fichajes_" + fechaInicio + "_al_" + fechaFin + ".pdf";
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/activeWorkers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActiveWorkerDto>> getActiveWorkers() {
        List<ActiveWorkerDto> activeWorkerDto = timeEntryService.getActiveWorkers();
        return ResponseEntity.ok(activeWorkerDto);
    }

    @GetMapping("/numFichajes")
    @PreAuthorize("hasRole('ADMIN')")
    public Long getFichajesCountToday() {
        return timeEntryService.getFichajesCountToday();
    }

    @GetMapping("/horasHoy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HorasTrabajadasHoyResponseDto> getHorasTotalesHoy() {
        HorasTrabajadasHoyResponseDto response = timeEntryService.getHorasTotalesHoy();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/weeklyChart")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DailyFichajeCountDto>> getWeeklyData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        List<DailyFichajeCountDto> dailyFichajeCountDtos =
                timeEntryService.getWeeklyChartData(fechaInicio, fechaFin);
        return ResponseEntity.ok(dailyFichajeCountDtos);

    }

    @GetMapping("/admin/by-day")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminFichajeDiaResponseDto>> getFichajesPorDiaEmpresa(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<AdminFichajeDiaResponseDto> response = timeEntryService.getFichajesPorDiaEmpresa(fecha);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> editarFichaje(
            @PathVariable UUID id,
            @Valid @RequestBody EditTimeEntryRequestDto dto) {
        timeEntryService.editarFichaje(id, dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> anularFichaje(
            @PathVariable UUID id,
            @Valid @RequestBody AnularTimeEntryRequestDto dto) {
        timeEntryService.anularFichaje(id, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<FichajeTablaResponseDto>> getFichajesPorEmpleado(
            @PathVariable UUID employeeId,
            Pageable pageable) {
        Page<FichajeTablaResponseDto> response = timeEntryService
                .getFichajesPaginadosPorEmpleado(employeeId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/export/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportarInformeEmpresaPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        byte[] pdfBytes = timeEntryService.exportarInformeEmpresaPdf(fechaInicio, fechaFin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        String nombreArchivo = "informe_forense_" + fechaInicio + "_al_" + fechaFin + ".pdf";
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/admin/export/excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportarInformeEmpresaExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        byte[] excelBytes = timeEntryService.exportarInformeEmpresaExcel(fechaInicio, fechaFin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        String nombreArchivo = "AUDITORIA_DATOS_" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

}