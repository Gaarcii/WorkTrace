package com.worktrace.worktracebackend.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeRequestDto {

    @NotBlank(message = "La contraseña es obligatoria")
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String newPassword;

    @NotBlank(message = "Repetir la contraseña es obligatorio")
    private String repeatPassword;
}