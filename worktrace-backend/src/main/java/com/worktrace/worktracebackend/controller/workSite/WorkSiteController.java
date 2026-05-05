package com.worktrace.worktracebackend.controller.workSite;


import com.worktrace.worktracebackend.dto.workSite.WorkSiteRequestDto;
import com.worktrace.worktracebackend.dto.workSite.WorkSiteResponseDto;
import com.worktrace.worktracebackend.service.workSite.WorkSiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/work-sites")
@RequiredArgsConstructor
public class WorkSiteController {

    private final WorkSiteService workSiteService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkSiteResponseDto>> getWorkSites() {
        return ResponseEntity.ok(workSiteService.getMyWorkSites());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkSiteResponseDto> createWorkSite
            (@Valid @RequestBody WorkSiteRequestDto dto) {
        WorkSiteResponseDto created = workSiteService.createWorkSite(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkSiteResponseDto> updateWorkSite(
            @PathVariable UUID id,
            @Valid @RequestBody WorkSiteRequestDto dto) {

        WorkSiteResponseDto updated = workSiteService.updateWorkSite(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWorkSite(@PathVariable UUID id) {
        workSiteService.deleteWorkSite(id);
        return ResponseEntity.noContent().build();
    }

}