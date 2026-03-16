package com.worktrace.worktracebackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRequestDto {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 250, message = "El nombre debe tener 250 caracteres máximo")
    private String companyName;

    @NotBlank(message = "El cif es obligatorio")
    @Size(min = 9, max = 9, message = "El cif debe de tener 8 dígitos")
    private String cif;

    @Valid
    @NotNull(message = "Los datos del administrador son obligatorios")
    private AdminRequestDto admin;
}
