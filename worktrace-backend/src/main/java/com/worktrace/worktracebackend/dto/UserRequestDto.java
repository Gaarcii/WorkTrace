package com.worktrace.worktracebackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {

    @NotBlank(message = "El email es obligatorio")
    @Size(max = 250, message = "El email debe tener 250 caracteres máximo")
    @Email(message = "Formato no válido, debe ser un email")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 250, message = "La contraseña debe tener entre 6 y 250 caracateres máximo")
    private String password;

    @Valid
    @NotNull(message = "Los datos del administrador son obligatorios")
    private ProfileRequestDto profile;
}
