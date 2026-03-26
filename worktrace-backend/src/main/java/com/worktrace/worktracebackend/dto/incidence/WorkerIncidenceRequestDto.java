package com.worktrace.worktracebackend.dto.incidence;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkerIncidenceRequestDto {
    @NotNull(message = "El tipo de incidencia es obligatorio")
    private UUID typeId;

    @NotNull(message = "La fecha de la incidencia es obligatoria")
    @PastOrPresent(message = "La fecha debe de ser del día de hoy o de días anteriores")
    private LocalDate fechaAfectada;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    private String comentario;
}
