package com.worktrace.worktracebackend.service.company;

import com.worktrace.worktracebackend.dto.company.CompanyResponseDto;
import com.worktrace.worktracebackend.dto.company.UpdateCompanyDto;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.repository.CompanyRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Servicio para gestionar las operaciones relacionadas con la empresa.
 * Centraliza la lógica de negocio para que los administradores puedan consultar
 * y actualizar la información de su propia empresa.
 */
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final StorageService storageService;
    private final UserService userService;

    /**
     * Obtiene los datos de la empresa asociada al administrador autenticado.
     * Este método permite a un administrador ver la información de su propia empresa.
     * @return Un DTO con los datos de la empresa.
     */
    @Transactional(readOnly = true)
    public CompanyResponseDto getMyCompanyData() {
        User admin = userService.getAuthenticatedUser();
        Company myCompany = admin.getCompany();

        if (myCompany == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Este administrador no tiene ninguna empresa asignada"
            );
        }

        return new CompanyResponseDto(
                myCompany.getCompanyName(),
                myCompany.getCif(),
                myCompany.getLogoUrl()
        );
    }

    /**
     * Actualiza el logo de la empresa del administrador autenticado.
     * Se encarga de almacenar el nuevo archivo de logo, actualizar la URL en la base de datos
     * y eliminar el logo anterior si existía.
     * @param file El nuevo archivo de logo.
     * @return La URL pública del nuevo logo.
     */
    @Transactional
    public String updateMyCompanyLogo(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se ha enviado ninguna imagen");
        }

        User admin = userService.getAuthenticatedUser();

        Company myCompany = admin.getCompany();
        if (myCompany == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este administrador no tiene ninguna empresa asignada");
        }
        String newFilename = storageService.store(file, "logos");
        String newLogoUrl = storageService.getUrl(newFilename, "logos");

        String oldLogoUrl = myCompany.getLogoUrl();

        myCompany.setLogoUrl(newLogoUrl);
        companyRepository.save(myCompany);

        if (oldLogoUrl != null && !oldLogoUrl.isEmpty()) {
            String oldFilename = oldLogoUrl.substring(oldLogoUrl.lastIndexOf("/") + 1);
            storageService.delete(oldFilename, "logos");
        }

        return newLogoUrl;
    }

    /**
     * Actualiza los datos generales (nombre y CIF) de la empresa del administrador autenticado.
     * @param dto El DTO con los nuevos datos para la empresa.
     */
    @Transactional
    public void updateMyCompanyData(UpdateCompanyDto dto) {
        User admin = userService.getAuthenticatedUser();
        Company myCompany = admin.getCompany();

        if (myCompany == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Este administrador no tiene ninguna empresa asignada");
        }

        myCompany.setCompanyName(dto.getCompanyName());
        myCompany.setCif(dto.getCif());

        companyRepository.save(myCompany);
    }
}
