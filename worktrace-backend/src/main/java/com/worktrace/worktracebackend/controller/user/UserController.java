package com.worktrace.worktracebackend.controller.user;

import com.worktrace.worktracebackend.dto.user.DepartmentStatDto;
import com.worktrace.worktracebackend.dto.user.EmployeeResponseDto;
import com.worktrace.worktracebackend.dto.user.UserRequestDto;
import com.worktrace.worktracebackend.dto.user.UserResponseDto;
import com.worktrace.worktracebackend.service.user.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> getUserProfile() {
        UserResponseDto responseDto = userProfileService.getProfile();
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> actualizarPerfil(
            @Valid @ModelAttribute UserRequestDto requestDto) {

        UserResponseDto responseDto = userProfileService.putProfile(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/total-workers")
    @PreAuthorize("hasRole('ADMIN')")
    public Long getTotalWorkers() {
        return userProfileService.getCompanyWorkers();
    }

    @GetMapping("/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DepartmentStatDto>> getDepartmenteStats() {
        List<DepartmentStatDto> dto = userProfileService.getDepartmetnStatus();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EmployeeResponseDto>> getEmpleados(Pageable pageable) {
        Page<EmployeeResponseDto> responseDtos = userProfileService.empleadosPorEmpresa(pageable);
        return ResponseEntity.ok(responseDtos);
    }
}
