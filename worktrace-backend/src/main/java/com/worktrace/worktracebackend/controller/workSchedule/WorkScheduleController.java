package com.worktrace.worktracebackend.controller.workSchedule;

import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleRequestDto;
import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleResponseDto;
import com.worktrace.worktracebackend.service.workSchedule.WorkScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/horario")
@RequiredArgsConstructor
public class WorkScheduleController {

    private final WorkScheduleService workScheduleService;

    @PostMapping("/asignar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> putWorkSchedule(
            @Valid @RequestBody WorkScheduleRequestDto dto) {
        workScheduleService.assignWorkSchedule(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkScheduleResponseDto>> getEmployeeSchedules(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(workScheduleService.getEmployeeSchedules(employeeId));
    }
}
