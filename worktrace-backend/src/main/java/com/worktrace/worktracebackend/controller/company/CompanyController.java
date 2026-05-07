package com.worktrace.worktracebackend.controller.company;

import com.worktrace.worktracebackend.dto.company.CompanyResponseDto;
import com.worktrace.worktracebackend.dto.company.UpdateCompanyDto;
import com.worktrace.worktracebackend.service.company.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Controlador para gestionar la información de la empresa.
 * Permite a los administradores obtener y actualizar los datos de su propia empresa.
 */
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /**
     * Obtiene los datos de la empresa a la que pertenece el administrador autenticado.
     *
     * @return Una respuesta con los datos de la empresa.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyResponseDto> getMyCompanyData() {
        return ResponseEntity.ok(companyService.getMyCompanyData());
    }

    /**
     * Actualiza el logo de la empresa a la que pertenece el administrador autenticado.
     * @param file El nuevo archivo de logo.
     * @return Una respuesta con un mensaje de éxito y la URL del nuevo logo.
     */
    @PatchMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateMyCompanyLogo(@RequestPart("file") MultipartFile file) {
        String logoUrl = companyService.updateMyCompanyLogo(file);
        return ResponseEntity.ok(Map.of(
                "message", "Logo de la empresa actualizado correctamente",
                "logoUrl", logoUrl
        ));
    }

    /**
     * Actualiza los datos de la empresa a la que pertenece el administrador autenticado.
     * @param dto Los nuevos datos de la empresa.
     * @return Una respuesta con un mensaje de éxito.
     */
    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateCompanyData
            (@Valid @RequestBody UpdateCompanyDto dto) {

        companyService.updateMyCompanyData(dto);

        return ResponseEntity.ok(Map.of(
                "message", "Datos de la empresa actualizados correctamente"
        ));
    }
}
