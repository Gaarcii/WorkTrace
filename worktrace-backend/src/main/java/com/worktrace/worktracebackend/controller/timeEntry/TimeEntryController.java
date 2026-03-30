package com.worktrace.worktracebackend.controller.timeEntry;

import com.worktrace.worktracebackend.dto.timeEntry.*;
import com.worktrace.worktracebackend.service.timeEntry.TimeEntryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/weeklyChart")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DailyFichajeCountDto>> getWeeklyData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        List<DailyFichajeCountDto> dailyFichajeCountDtos =
                timeEntryService.getWeeklyChartData(fechaInicio, fechaFin);
        return ResponseEntity.ok(dailyFichajeCountDtos);

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

}