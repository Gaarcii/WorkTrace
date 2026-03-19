package com.worktrace.worktracebackend.service.incidenceType;

import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeResponseDto;
import com.worktrace.worktracebackend.repository.IncidenceTypeRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UsuarioYCompaniaInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IncidenceTypeService {
    private final IncidenceTypeRepository incidenceTypeRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public IncidenceTypeResponseDto getTiposIncidencias() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        IncidenceTypeResponseDto typeResponseDto = new IncidenceTypeResponseDto();
        typeResponseDto.setTipos(
                incidenceTypeRepository.findByCompany_Id(
                        info.getCompany().getId()));

        return typeResponseDto;
    }
}
