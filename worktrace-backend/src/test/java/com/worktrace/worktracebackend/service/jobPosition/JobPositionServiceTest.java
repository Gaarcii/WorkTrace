package com.worktrace.worktracebackend.service.jobPosition;

import com.worktrace.worktracebackend.dto.jobPosition.JobPositionRequestDto;
import com.worktrace.worktracebackend.dto.jobPosition.JobPositionResponseDto;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.JobPosition;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.repository.JobPositionRepository;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import com.worktrace.worktracebackend.service.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobPositionServiceTest {

    @Mock
    private JobPositionRepository jobPositionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private JobPositionService jobPositionService;

    private Company company;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        company = new Company();
        company.setId(companyId);

        User user = new User();
        user.setCompany(company);

        UserAndCompanyInfo userAndCompanyInfo = new UserAndCompanyInfo(user, company, null);

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
    }

    @Test
    void testGetJobPositionsSuccess() {
        JobPosition position = new JobPosition();
        position.setId(UUID.randomUUID());
        position.setTitle("Desarrollador Backend");
        List<JobPosition> positions = Collections.singletonList(position);

        when(jobPositionRepository.findByCompany_Id(companyId)).thenReturn(positions);

        List<JobPositionResponseDto> result = jobPositionService.getJobPositions();

        assertFalse(result.isEmpty(), "La lista de puestos no debería estar vacía");
        assertEquals(1, result.size(), "La lista debe contener un puesto");
        assertEquals("Desarrollador Backend", result.getFirst().getName(), "El nombre del puesto debe coincidir");
    }

    @Test
    void testCreateJobPositionSuccess() {
        JobPositionRequestDto requestDto = new JobPositionRequestDto("Diseñador UX");

        when(jobPositionRepository.findByTitleIgnoreCaseAndCompany_Id(anyString(), any(UUID.class))).thenReturn(Optional.empty());
        when(jobPositionRepository.save(any(JobPosition.class))).thenAnswer(invocation -> {
            JobPosition saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        JobPositionResponseDto result = jobPositionService.createJobPosition(requestDto);

        assertNotNull(result, "El resultado no debe ser nulo");
        assertEquals("Diseñador UX", result.getName(), "El nombre del puesto creado debe ser el correcto");
        verify(jobPositionRepository, times(1)).save(any(JobPosition.class));
    }

    @Test
    void testCreateJobPositionThrowsExceptionWhenAlreadyExists() {
        JobPositionRequestDto requestDto = new JobPositionRequestDto("Puesto Existente");
        JobPosition existingPosition = new JobPosition();

        when(jobPositionRepository.findByTitleIgnoreCaseAndCompany_Id("Puesto Existente", companyId)).thenReturn(Optional.of(existingPosition));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> jobPositionService.createJobPosition(requestDto));

        assertEquals("Ya existe un puesto con ese nombre en la empresa", exception.getMessage(), "El mensaje de excepción debe ser el correcto");
        verify(jobPositionRepository, never()).save(any(JobPosition.class));
    }

    @Test
    void testDeleteJobPositionSuccess() {
        UUID jobPositionId = UUID.randomUUID();
        JobPosition positionToDelete = new JobPosition();
        positionToDelete.setId(jobPositionId);
        positionToDelete.setCompany(company);

        when(jobPositionRepository.findById(jobPositionId)).thenReturn(Optional.of(positionToDelete));
        doNothing().when(jobPositionRepository).delete(positionToDelete);

        assertDoesNotThrow(() -> jobPositionService.deleteJobPosition(jobPositionId), "La eliminación no debería lanzar una excepción");

        verify(jobPositionRepository, times(1)).delete(positionToDelete);
    }

    @Test
    void testDeleteJobPositionDoesNothingWhenCompanyMismatch() {
        UUID jobPositionId = UUID.randomUUID();
        JobPosition positionToDelete = new JobPosition();
        positionToDelete.setId(jobPositionId);
        Company anotherCompany = new Company();
        anotherCompany.setId(UUID.randomUUID());
        positionToDelete.setCompany(anotherCompany);

        when(jobPositionRepository.findById(jobPositionId)).thenReturn(Optional.of(positionToDelete));

        jobPositionService.deleteJobPosition(jobPositionId);

        verify(jobPositionRepository, never()).delete(any(JobPosition.class));
    }

    @Test
    void testDeleteJobPositionThrowsExceptionWhenNotFound() {
        UUID jobPositionId = UUID.randomUUID();
        when(jobPositionRepository.findById(jobPositionId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalStateException.class, () -> jobPositionService.deleteJobPosition(jobPositionId));

        assertEquals("Puesto de trabajo no encontrado", exception.getMessage());
        verify(jobPositionRepository, never()).delete(any(JobPosition.class));
    }
}
