package com.worktrace.worktracebackend.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequestDto {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 250, message = "El nombre debe tener 250 caracteres máximo")
    private String fullName;

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 9, max = 9, message = "El DNI debe tener 9 caracteres")
    private String employeeCode;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    private String phone;

    @Positive(message = "Las horas semanales deben ser un número positivo")
    @Max(value = 168, message = "No hay más de 168 horas en una semana")
    private BigDecimal weeklyHours;

    private UUID position;
}
