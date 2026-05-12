package com.worktrace.worktracebackend.service.company;

import com.worktrace.worktracebackend.dto.company.CompanyResponseDto;
import com.worktrace.worktracebackend.dto.company.UpdateCompanyDto;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.repository.CompanyRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CompanyService companyService;

    private User authenticatedAdmin;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        testCompany = new Company();
        testCompany.setId(UUID.randomUUID());
        testCompany.setCompanyName("Mi Empresa de Prueba");
        testCompany.setCif("B12345678");
        testCompany.setLogoUrl("http://example.com/logo.png");

        authenticatedAdmin = new User();
        authenticatedAdmin.setId(UUID.randomUUID());
        authenticatedAdmin.setCompany(testCompany);
    }

    @Test
    void testGetMyCompanyDataSuccess() {
        when(userService.getAuthenticatedUser()).thenReturn(authenticatedAdmin);

        CompanyResponseDto result = companyService.getMyCompanyData();

        assertAll("Verifica los datos de la empresa recuperada",
                () -> assertEquals(testCompany.getCompanyName(), result.getCompanyName()),
                () -> assertEquals(testCompany.getCif(), result.getCif()),
                () -> assertEquals(testCompany.getLogoUrl(), result.getLogoUrl())
        );

        verify(userService).getAuthenticatedUser();
    }

    @Test
    void testGetMyCompanyDataError_AdminHasNoCompany() {
        authenticatedAdmin.setCompany(null);
        when(userService.getAuthenticatedUser()).thenReturn(authenticatedAdmin);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> companyService.getMyCompanyData());

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Este administrador no tiene ninguna empresa asignada", exception.getReason());

        verify(userService).getAuthenticatedUser();
    }

    @Test
    void testUpdateMyCompanyLogoSuccess() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "new_logo.png",
                "image/png",
                "test image data".getBytes()
        );
        String newFilename = "generated_filename.png";
        String newLogoUrl = "http://example.com/logos/generated_filename.png";

        when(userService.getAuthenticatedUser()).thenReturn(authenticatedAdmin);
        when(storageService.store(any(), eq("logos"))).thenReturn(newFilename);
        when(storageService.getUrl(newFilename, "logos")).thenReturn(newLogoUrl);

        String resultUrl = companyService.updateMyCompanyLogo(mockFile);

        assertEquals(newLogoUrl, resultUrl);
        assertEquals(newLogoUrl, testCompany.getLogoUrl());

        verify(userService).getAuthenticatedUser();
        verify(storageService).store(mockFile, "logos");
        verify(storageService).getUrl(newFilename, "logos");
        verify(companyRepository).save(testCompany);
        verify(storageService).delete("logo.png", "logos");
    }
    
    @Test
    void testUpdateMyCompanyLogoError_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "image/png", new byte[0]);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> companyService.updateMyCompanyLogo(emptyFile));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("No se ha enviado ninguna imagen", exception.getReason());

        verifyNoInteractions(userService, companyRepository, storageService);
    }

    @Test
    void testUpdateMyCompanyLogoError_AdminHasNoCompany() {
        authenticatedAdmin.setCompany(null);
        MockMultipartFile mockFile = new MockMultipartFile("file", "logo.png", "image/png", "data".getBytes());
        when(userService.getAuthenticatedUser()).thenReturn(authenticatedAdmin);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> companyService.updateMyCompanyLogo(mockFile));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Este administrador no tiene ninguna empresa asignada", exception.getReason());

        verify(userService).getAuthenticatedUser();
        verifyNoInteractions(companyRepository, storageService);
    }

    @Test
    void testUpdateMyCompanyDataSuccess() {
        UpdateCompanyDto updateDto = new UpdateCompanyDto();
        updateDto.setCompanyName("Nuevo Nombre Empresa");
        updateDto.setCif("A87654321");

        when(userService.getAuthenticatedUser()).thenReturn(authenticatedAdmin);

        companyService.updateMyCompanyData(updateDto);

        assertEquals("Nuevo Nombre Empresa", testCompany.getCompanyName());
        assertEquals("A87654321", testCompany.getCif());

        verify(userService).getAuthenticatedUser();
        verify(companyRepository).save(testCompany);
    }

    @Test
    void testUpdateMyCompanyDataError_AdminHasNoCompany() {
        authenticatedAdmin.setCompany(null);
        UpdateCompanyDto updateDto = new UpdateCompanyDto();
        updateDto.setCompanyName("Nuevo Nombre");
        updateDto.setCif("A12345678");

        when(userService.getAuthenticatedUser()).thenReturn(authenticatedAdmin);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> companyService.updateMyCompanyData(updateDto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Este administrador no tiene ninguna empresa asignada", exception.getReason());

        verify(userService).getAuthenticatedUser();
        verifyNoInteractions(companyRepository);
    }
}
