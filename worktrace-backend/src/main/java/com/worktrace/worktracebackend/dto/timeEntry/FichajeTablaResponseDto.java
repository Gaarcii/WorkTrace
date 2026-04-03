package com.worktrace.worktracebackend.dto.timeEntry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FichajeTablaResponseDto {
    private UUID id;
    private LocalDate fecha;
    private OffsetDateTime entrada;
    private OffsetDateTime salida;
    private BigDecimal latEntrada;
    private BigDecimal lngEntrada;
    private BigDecimal latSalida;
    private BigDecimal lngSalida;
    private Long horasTrabajadas;
}