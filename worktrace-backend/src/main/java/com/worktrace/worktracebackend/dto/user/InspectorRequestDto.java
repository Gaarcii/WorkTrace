package com.worktrace.worktracebackend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InspectorRequestDto {

    @NotBlank(message = "El email es obligatorio")
    @Size(max = 250, message = "El email debe tener 250 caracteres máximo")
    @Email(message = "Formato no válido, debe ser un email")
    private String email;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 250, message = "El nombre debe tener 250 caracteres máximo")
    private String fullName;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    private String phone;
}