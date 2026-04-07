package com.worktrace.worktracebackend.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompanyDto {

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(max = 250, message = "El nombre debe tener 250 caracteres máximo")
    private String companyName;

    @NotBlank(message = "El CIF/NIF es obligatorio")
    @Size(min = 9, max = 9, message = "El CIF/NIF debe tener 9 caracteres")
    private String cif;

}