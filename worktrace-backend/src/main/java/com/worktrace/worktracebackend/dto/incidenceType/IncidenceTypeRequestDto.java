package com.worktrace.worktracebackend.dto.incidenceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IncidenceTypeRequestDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 250, message = "El nombre debe tener 250 caracteres máximo")
    private String name;
}
