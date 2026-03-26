package com.worktrace.worktracebackend.dto.incidence;

import com.worktrace.worktracebackend.model.EstadoIncidencia;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminIncidenceRequestDto {

    @NotNull(message = "Es obligatorio el estado")
    private EstadoIncidencia estado;
    private String respuestaAdmin;
}
