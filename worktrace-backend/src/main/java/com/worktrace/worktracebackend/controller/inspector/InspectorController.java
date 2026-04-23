package com.worktrace.worktracebackend.controller.inspector;

import com.worktrace.worktracebackend.dto.inspector.InspectorHomeResponseDto;
import com.worktrace.worktracebackend.service.inspector.InspectorHomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inspector")
@RequiredArgsConstructor
public class InspectorController {

    private final InspectorHomeService inspectorHomeService;

    @GetMapping("/home")
    @PreAuthorize("hasRole('INSPECTOR')")
    public ResponseEntity<InspectorHomeResponseDto> getHome() {
        return ResponseEntity.ok(inspectorHomeService.getHome());
    }
}

