package com.worktrace.worktracebackend.controller.timeEntry;

import com.worktrace.worktracebackend.dto.timeEntry.ResumenDiarioResponseDto;
import com.worktrace.worktracebackend.dto.timeEntry.TimeEntryRequestDto;
import com.worktrace.worktracebackend.dto.timeEntry.TimeEntryResponseDto;
import com.worktrace.worktracebackend.service.timeEntry.TimeEntryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/time-entries")
@RequiredArgsConstructor
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    @PostMapping("/fichar")
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResumenDiarioResponseDto> resumenDiario() {
        ResumenDiarioResponseDto responseDto = timeEntryService.getResumenDiario();
        return ResponseEntity.ok(responseDto);
    }


}