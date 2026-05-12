package com.worktrace.worktracebackend.dto.user;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileRequestDto {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 250, message = "El nombre debe tener 250 caracteres máximo")
    private String fullName;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8}[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]$", message = "El DNI debe tener 8 números y 1 letra")
    private String employeeCode;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    private String phone;
}

