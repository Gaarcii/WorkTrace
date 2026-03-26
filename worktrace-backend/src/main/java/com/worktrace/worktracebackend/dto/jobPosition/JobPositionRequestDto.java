package com.worktrace.worktracebackend.dto.jobPosition;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JobPositionRequestDto {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
}
