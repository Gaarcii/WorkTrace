package com.worktrace.worktracebackend.dto.timeEntry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EditTimeEntryRequestDto {

    @NotNull(message = "La fecha y hora de entrada es obligatoria")
    private OffsetDateTime startAt;

    private OffsetDateTime endAt;

    @NotBlank(message = "La justificación es obligatoria")
    @Size(min = 10, message = "La justificación debe tener al menos 10 caracteres")
    private String justification;
}