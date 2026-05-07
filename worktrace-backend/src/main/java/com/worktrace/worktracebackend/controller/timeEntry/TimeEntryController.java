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

    @GetMapping("/daily-summary")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<DailySummaryResponseDto> getDailySummary() {
        DailySummaryResponseDto responseDto = timeEntryService.getDailySummary();
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<HistoryResponseDto> getHistoryByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        HistoryResponseDto history = timeEntryService.getHistoryByDate(date);
        return ResponseEntity.ok(history);
    }

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

    @GetMapping("/active-workers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActiveWorkerDto>> getActiveWorkers() {
        List<ActiveWorkerDto> activeWorkerDto = timeEntryService.getActiveWorkers();
        return ResponseEntity.ok(activeWorkerDto);
    }

    @GetMapping("/count-today")
    @PreAuthorize("hasRole('ADMIN')")
    public Long getTimeEntriesCountToday() {
        return timeEntryService.getTimeEntriesCountToday();
    }

    @GetMapping("/hours-today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TotalHoursTodayResponseDto> getTotalHoursToday() {
        TotalHoursTodayResponseDto response = timeEntryService.getTotalHoursToday();
        return ResponseEntity.ok(response);
    }



    @GetMapping("/weekly-chart")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DailyTimeEntryCountDto>> getWeeklyChartData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<DailyTimeEntryCountDto> dailyTimeEntryCounts =
                timeEntryService.getWeeklyTimeEntryCountChartData(startDate, endDate);
        return ResponseEntity.ok(dailyTimeEntryCounts);

    }

    @GetMapping("/admin/by-date")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminTimeEntryByDateResponseDto>> getTimeEntriesByDateForCompany(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AdminTimeEntryByDateResponseDto> response = timeEntryService.getTimeEntriesByDateForCompany(date);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateTimeEntry(
            @PathVariable UUID id,
            @Valid @RequestBody EditTimeEntryRequestDto dto) {
        timeEntryService.updateTimeEntry(id, dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/void")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> voidTimeEntry(
            @PathVariable UUID id,
            @Valid @RequestBody VoidTimeEntryRequestDto dto) {
        timeEntryService.voidTimeEntry(id, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TimeEntryTableResponseDto>> getTimeEntriesByEmployee(
            @PathVariable UUID employeeId,
            Pageable pageable) {
        Page<TimeEntryTableResponseDto> response = timeEntryService
                .getTimeEntriesByEmployee(employeeId, pageable);
        return ResponseEntity.ok(response);
    }

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
