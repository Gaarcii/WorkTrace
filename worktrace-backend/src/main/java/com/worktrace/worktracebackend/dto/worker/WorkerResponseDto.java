package com.worktrace.worktracebackend.dto.worker;

import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkerResponseDto {

    private String nombreCompleto;
    private String avatarUrl;
    private String puestoTrabajo;
    private String email;
    private String telefono;
    private List<WorkScheduleResponseDto> horario;
    private String tokenActualizado;
}
