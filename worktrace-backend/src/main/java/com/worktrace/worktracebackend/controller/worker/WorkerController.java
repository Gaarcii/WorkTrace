package com.worktrace.worktracebackend.controller.worker;

import com.worktrace.worktracebackend.dto.worker.WorkerRequestDto;
import com.worktrace.worktracebackend.dto.worker.WorkerResponseDto;
import com.worktrace.worktracebackend.service.worker.WorkerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/worker")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<WorkerResponseDto> getWorker() {
        WorkerResponseDto responseDto = workerService.getProfile();
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping(consumes = org.springframework.http.MediaType
            .MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkerResponseDto> actualizarWorker(
            @Valid @ModelAttribute WorkerRequestDto requestDto) {

        WorkerResponseDto responseDto = workerService.putProfile(requestDto);
        return ResponseEntity.ok(responseDto);
    }

}
