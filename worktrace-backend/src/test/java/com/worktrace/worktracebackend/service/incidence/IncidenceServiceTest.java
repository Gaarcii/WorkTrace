package com.worktrace.worktracebackend.service.incidence;

import com.worktrace.worktracebackend.dto.incidence.AdminIncidenceRequestDto;
import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceRequestDto;
import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceResponseDto;
import com.worktrace.worktracebackend.model.*;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidenceServiceTest {

    @Mock
    private IncidenceRepository incidenceRepository;

    @Mock
    private IncidenceTypeRepository incidenceTypeRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private IncidenceService incidenceService;

    private UserAndCompanyInfo userAndCompanyInfo;
    private User user;
    private Company company;
    private IncidenceType incidenceType;
    private Incidence incidence;
    private UUID incidenceId;
    private UUID incidenceTypeId;

    @BeforeEach
    void setUp() {
        incidenceId = UUID.randomUUID();
        incidenceTypeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        company = new Company();
        company.setId(companyId);
        company.setCompanyName("Empresa de Prueba");

        user = new User();
        user.setId(userId);
        user.setCompany(company);

        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setUser(user);
        profile.setFullName("Juan Trabajador");

        user.setProfile(profile);

        userAndCompanyInfo = new UserAndCompanyInfo(user, company, profile);

        incidenceType = new IncidenceType();
        incidenceType.setId(incidenceTypeId);
        incidenceType.setName("Olvido de fichaje de entrada");
        incidenceType.setCompany(company);

        incidence = new Incidence();
        incidence.setId(incidenceId);
        incidence.setProfile(profile);
        incidence.setCompany(company);
        incidence.setType(incidenceType);
        incidence.setDate(LocalDate.now().minusDays(1));
        incidence.setIncidenceTime(LocalTime.of(9, 0));
        incidence.setComment("Olvidé fichar al entrar.");
        incidence.setStatus(IncidenceStatus.PENDING);
        incidence.setCreatedAt(OffsetDateTime.now());
        incidence.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void testCreateIncidenceSuccess() {
        WorkerIncidenceRequestDto requestDto = new WorkerIncidenceRequestDto(
                incidenceTypeId,
                LocalDate.now().minusDays(1),
                LocalTime.of(9, 5),
                "Se me olvidó fichar al llegar."
        );

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(incidenceTypeRepository.findByIdAndCompany_IdAndDeletedAtIsNull(incidenceTypeId, company.getId())).thenReturn(Optional.of(incidenceType));
        when(incidenceRepository.save(any(Incidence.class))).thenAnswer(invocation -> {
            Incidence savedIncidence = invocation.getArgument(0);
            savedIncidence.setId(UUID.randomUUID()); // Simulate saving
            return savedIncidence;
        });

        WorkerIncidenceResponseDto response = incidenceService.createIncidence(requestDto);

        assertNotNull(response, "La respuesta no debe ser nula");
        assertEquals(incidenceType.getName(), response.getIncidenceType(), "El tipo de incidencia debe coincidir");
        assertEquals(requestDto.getComment(), response.getComment(), "El comentario debe coincidir");
        assertEquals(IncidenceStatus.PENDING, response.getStatus(), "El estado inicial debe ser PENDIENTE");

        verify(incidenceRepository, times(1)).save(any(Incidence.class));
    }

    @Test
    void testCreateIncidenceThrowsExceptionWhenTypeNotFound() {
        WorkerIncidenceRequestDto requestDto = new WorkerIncidenceRequestDto(UUID.randomUUID(), LocalDate.now(), LocalTime.now(), "Test");
        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(incidenceTypeRepository.findByIdAndCompany_IdAndDeletedAtIsNull(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalStateException.class, () -> incidenceService.createIncidence(requestDto));

        assertEquals("Tipo de incidencia no encontrado", exception.getMessage(), "El mensaje de excepción debe ser el correcto");
    }

    @Test
    void testManageIncidenceSuccess() {
        AdminIncidenceRequestDto requestDto = new AdminIncidenceRequestDto(IncidenceStatus.RESOLVED, "Incidencia resuelta y registrada.");

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(incidenceRepository.findById(incidenceId)).thenReturn(Optional.of(incidence));

        assertDoesNotThrow(() -> incidenceService.manageIncidence(incidenceId, requestDto), "La gestión de la incidencia no debería lanzar una excepción");

        verify(incidenceRepository, times(1)).save(incidence);
        assertEquals(IncidenceStatus.RESOLVED, incidence.getStatus(), "El estado de la incidencia debe ser RESUELTO");
        assertEquals(user, incidence.getResolvedBy(), "El usuario que resuelve debe ser el administrador actual");
        assertEquals(requestDto.getAdminResponse(), incidence.getAdminResponse(), "La respuesta del administrador debe guardarse");
    }

    @Test
    void testManageIncidenceThrowsExceptionWhenNotFound() {
        AdminIncidenceRequestDto requestDto = new AdminIncidenceRequestDto(IncidenceStatus.RESOLVED, "Test");
        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(incidenceRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalStateException.class, () -> incidenceService.manageIncidence(UUID.randomUUID(), requestDto));

        assertEquals("Incidencia no encontrada", exception.getMessage());
    }

    @Test
    void testManageIncidenceThrowsExceptionWhenAlreadyManaged() {
        incidence.setStatus(IncidenceStatus.RESOLVED);
        AdminIncidenceRequestDto requestDto = new AdminIncidenceRequestDto(IncidenceStatus.REJECTED, "Intentando cambiar estado.");

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(incidenceRepository.findById(incidenceId)).thenReturn(Optional.of(incidence));

        Exception exception = assertThrows(IllegalStateException.class, () -> incidenceService.manageIncidence(incidenceId, requestDto));

        assertEquals("La incidencia ya ha sido gestionada anteriormente", exception.getMessage());
    }

    @Test
    void testManageIncidenceThrowsExceptionWhenCompanyMismatch() {
        Company anotherCompany = new Company();
        anotherCompany.setId(UUID.randomUUID());
        incidence.setCompany(anotherCompany);

        AdminIncidenceRequestDto requestDto = new AdminIncidenceRequestDto(IncidenceStatus.RESOLVED, "Test");

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(incidenceRepository.findById(incidenceId)).thenReturn(Optional.of(incidence));

        Exception exception = assertThrows(IllegalStateException.class, () -> incidenceService.manageIncidence(incidenceId, requestDto));

        assertEquals("No tienes permisos sobre esta incidencia", exception.getMessage());
    }
}
