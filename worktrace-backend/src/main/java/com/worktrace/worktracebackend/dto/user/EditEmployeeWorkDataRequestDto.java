package com.worktrace.worktracebackend.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class EditEmployeeWorkDataRequestDto {

    @NotNull(message = "El puesto de trabajo es obligatorio")
    private UUID positionId;

    @Positive(message = "Las horas semanales deben ser un numero positivo")
    @Max(value = 168, message = "No hay mas de 168 horas en una semana")
    @NotNull(message = "Las horas semanales son obligatorias")
    private BigDecimal weeklyHours;
}
