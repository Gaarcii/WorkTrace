package com.worktrace.worktracebackend.service.incidenceType;

import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeItemDto;
import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeRequestDto;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.IncidenceType;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.repository.IncidenceRepository;
import com.worktrace.worktracebackend.repository.IncidenceTypeRepository;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import com.worktrace.worktracebackend.service.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidenceTypeServiceTest {

    @Mock
    private IncidenceTypeRepository incidenceTypeRepository;

    @Mock
    private IncidenceRepository incidenceRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private IncidenceTypeService incidenceTypeService;

    private UserAndCompanyInfo userAndCompanyInfo;
    private Company company;
    private UUID companyId;
    private UUID incidenceTypeId;
    private IncidenceType incidenceType;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        incidenceTypeId = UUID.randomUUID();

        company = new Company();
        company.setId(companyId);

        User user = new User();
        user.setCompany(company);

        userAndCompanyInfo = new UserAndCompanyInfo(user, company, null);

        incidenceType = new IncidenceType();
        incidenceType.setId(incidenceTypeId);
        incidenceType.setName("Tipo de Incidencia Original");
        incidenceType.setCompany(company);
        incidenceType.setDeletedAt(null);
    }

    @Test
    void testCreateIncidenceTypeSuccess() {
        IncidenceTypeRequestDto requestDto = new IncidenceTypeRequestDto();
        requestDto.setName("Nuevo Tipo de Incidencia");

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(incidenceTypeRepository.findByNameIgnoreCaseAndCompany_Id(anyString(), any(UUID.class))).thenReturn(Optional.empty());
        when(incidenceTypeRepository.save(any(IncidenceType.class))).thenAnswer(invocation -> {
            IncidenceType saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        IncidenceTypeItemDto result = incidenceTypeService.createIncidenceType(requestDto);

        assertNotNull(result);
        assertEquals("Nuevo Tipo de Incidencia", result.getName());
        verify(incidenceTypeRepository, times(1)).save(any(IncidenceType.class));
    }

    @Test
    void testCreateIncidenceTypeReactivatesDeleted() {
        IncidenceTypeRequestDto requestDto = new IncidenceTypeRequestDto();
        requestDto.setName("Tipo Reactivado");

        IncidenceType deletedType = new IncidenceType();
        deletedType.setId(UUID.randomUUID());
        deletedType.setName("Tipo Reactivado");
        deletedType.setCompany(company);
        deletedType.setDeletedAt(OffsetDateTime.now());

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(incidenceTypeRepository.findByNameIgnoreCaseAndCompany_Id("Tipo Reactivado", companyId)).thenReturn(Optional.of(deletedType));
        when(incidenceTypeRepository.save(any(IncidenceType.class))).thenReturn(deletedType);

        IncidenceTypeItemDto result = incidenceTypeService.createIncidenceType(requestDto);

        assertNull(deletedType.getDeletedAt(), "La fecha de borrado debe ser nula tras reactivar");
        assertEquals("Tipo Reactivado", result.getName());
        verify(incidenceTypeRepository, times(1)).save(deletedType);
    }

    @Test
    void testCreateIncidenceTypeThrowsConflict() {
        IncidenceTypeRequestDto requestDto = new IncidenceTypeRequestDto();
        requestDto.setName("Tipo de Incidencia Original");

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(incidenceTypeRepository.findByNameIgnoreCaseAndCompany_Id("Tipo de Incidencia Original", companyId)).thenReturn(Optional.of(incidenceType));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> incidenceTypeService.createIncidenceType(requestDto));

        assertEquals(409, exception.getStatusCode().value());
        assert exception.getReason() != null;
        assertTrue(exception.getReason().contains("Ya existe un tipo de incidencia con ese nombre"));
    }

    @Test
    void testUpdateIncidenceTypeSuccess() {
        IncidenceTypeRequestDto requestDto = new IncidenceTypeRequestDto();
        requestDto.setName("Nombre Actualizado");

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(incidenceTypeRepository.findByIdAndCompany_IdAndDeletedAtIsNull(incidenceTypeId, companyId)).thenReturn(Optional.of(incidenceType));
        when(incidenceTypeRepository.findByNameIgnoreCaseAndCompany_Id("Nombre Actualizado", companyId)).thenReturn(Optional.empty());
        when(incidenceTypeRepository.save(any(IncidenceType.class))).thenReturn(incidenceType);

        IncidenceTypeItemDto result = incidenceTypeService.updateIncidenceType(incidenceTypeId, requestDto);

        assertEquals("Nombre Actualizado", result.getName());
        verify(incidenceTypeRepository, times(1)).save(incidenceType);
    }

    @Test
    void testDeleteIncidenceTypeWithExistingIncidences() {
        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(incidenceTypeRepository.findByIdAndCompany_IdAndDeletedAtIsNull(incidenceTypeId, companyId)).thenReturn(Optional.of(incidenceType));
        when(incidenceRepository.existsByType_IdAndCompany_Id(incidenceTypeId, companyId)).thenReturn(true);

        incidenceTypeService.deleteIncidenceType(incidenceTypeId);

        verify(incidenceTypeRepository, times(1)).save(incidenceType);
        verify(incidenceTypeRepository, never()).delete(any(IncidenceType.class));
        assertNotNull(incidenceType.getDeletedAt(), "La fecha de borrado debe establecerse para un borrado lógico");
    }

    @Test
    void testDeleteIncidenceTypeWithoutExistingIncidences() {
        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(incidenceTypeRepository.findByIdAndCompany_IdAndDeletedAtIsNull(incidenceTypeId, companyId)).thenReturn(Optional.of(incidenceType));
        when(incidenceRepository.existsByType_IdAndCompany_Id(incidenceTypeId, companyId)).thenReturn(false);

        incidenceTypeService.deleteIncidenceType(incidenceTypeId);

        verify(incidenceTypeRepository, never()).save(any(IncidenceType.class));
        verify(incidenceTypeRepository, times(1)).delete(incidenceType);
    }

    @Test
    void testDeleteIncidenceTypeThrowsNotFound() {
        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(incidenceTypeRepository.findByIdAndCompany_IdAndDeletedAtIsNull(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> incidenceTypeService.deleteIncidenceType(UUID.randomUUID()));

        assertEquals(404, exception.getStatusCode().value());
    }
}
