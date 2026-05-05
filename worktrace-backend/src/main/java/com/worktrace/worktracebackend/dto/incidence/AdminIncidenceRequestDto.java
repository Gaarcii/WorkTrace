package com.worktrace.worktracebackend.dto.incidence;

import com.worktrace.worktracebackend.model.IncidenceStatus;
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
    private IncidenceStatus estado;
    private String respuestaAdmin;
}
