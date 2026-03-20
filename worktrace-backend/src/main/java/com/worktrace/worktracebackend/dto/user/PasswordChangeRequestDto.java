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
    private String actual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String nueva;

    @NotBlank(message = "Repetir la contraseña es obligatorio")
    private String repetir;
}
