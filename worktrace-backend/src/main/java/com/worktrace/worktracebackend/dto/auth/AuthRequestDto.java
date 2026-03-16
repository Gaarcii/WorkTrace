package com.worktrace.worktracebackend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDto {

    @NotBlank(message = "El email es obligatorio")
    @Size(max = 250, message = "El email debe tener 250 caracteres máximo")
    @Email(message = "Formato no válido, debe ser un email")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 250, message = "La contraseña debe tener entre 6 y 250 caracteres máximo")
    private String password;
}
