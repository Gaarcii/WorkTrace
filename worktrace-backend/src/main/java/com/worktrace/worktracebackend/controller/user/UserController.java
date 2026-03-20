package com.worktrace.worktracebackend.controller.user;

import com.worktrace.worktracebackend.dto.user.UserRequestDto;
import com.worktrace.worktracebackend.dto.user.UserResponseDto;
import com.worktrace.worktracebackend.service.user.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}
