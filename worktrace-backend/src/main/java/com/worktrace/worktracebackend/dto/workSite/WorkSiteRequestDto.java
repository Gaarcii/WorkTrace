package com.worktrace.worktracebackend.dto.workSite;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkSiteRequestDto {
    @NotBlank(message = "El nombre de la sede es obligatorio")
    private String name;

    @NotBlank(message = "La dirección de la sede es obligatoria")
    private String address;
}