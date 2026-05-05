package com.worktrace.worktracebackend.dto.timeEntry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VoidTimeEntryRequestDto {
    @NotBlank(message = "La justificación es obligatoria")
    @Size(min = 10, message = "La justificación debe tener al menos 1o caracteres")
    private String justificacion;
}
