package com.worktrace.worktracebackend.service.company;

import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.repository.CompanyRepository;
import com.worktrace.worktracebackend.repository.UserRepository;
import com.worktrace.worktracebackend.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    public String updateMyCompanyLogo(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se ha enviado ninguna imagen");
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado el administrador (no autenticado)");
        }
        String adminEmail = authentication.getName();
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador no encontrado"));

        Company myCompany = admin.getCompany();
        if (myCompany == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este administrador no tiene ninguna empresa asignada");
        }
        String newFilename = storageService.store(file);
        String newLogoUrl = storageService.getUrl(newFilename);

        String oldLogoUrl = myCompany.getLogoUrl();

        myCompany.setLogoUrl(newLogoUrl);
        companyRepository.save(myCompany);

        if (oldLogoUrl != null && !oldLogoUrl.isEmpty()) {
            String oldFilename = oldLogoUrl.substring(oldLogoUrl.lastIndexOf("/") + 1);
            storageService.delete(oldFilename);
        }

        return newLogoUrl;
    }
}