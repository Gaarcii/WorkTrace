package com.worktrace.worktracebackend.service.company;

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

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final StorageService storageService;
    private final UserService userService;

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