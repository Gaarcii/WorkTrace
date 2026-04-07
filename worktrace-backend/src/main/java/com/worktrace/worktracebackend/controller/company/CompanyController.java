package com.worktrace.worktracebackend.controller.company;

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

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PatchMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateMyCompanyLogo(@RequestPart("file") MultipartFile file) {
        String logoUrl = companyService.updateMyCompanyLogo(file);
        return ResponseEntity.ok(Map.of(
                "message", "Logo de la empresa actualizado correctamente",
                "logoUrl", logoUrl
        ));
    }

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