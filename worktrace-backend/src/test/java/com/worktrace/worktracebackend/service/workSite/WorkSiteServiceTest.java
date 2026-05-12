package com.worktrace.worktracebackend.service.workSite;

import com.worktrace.worktracebackend.dto.workSite.WorkSiteRequestDto;
import com.worktrace.worktracebackend.dto.workSite.WorkSiteResponseDto;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.WorkScheduleRepository;
import com.worktrace.worktracebackend.repository.WorkSiteRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkSiteServiceTest {

    @Mock
    private WorkSiteRepository workSiteRepository;

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private WorkSiteService workSiteService;

    private User admin;
    private Company company;
    private WorkSite workSite;
    private WorkSiteRequestDto workSiteRequestDto;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(UUID.randomUUID());
        company.setCompanyName("Mi Empresa");

        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setCompany(company);

        workSite = new WorkSite();
        workSite.setId(UUID.randomUUID());
        workSite.setName("Sede Central");
        workSite.setAddress("Calle Falsa 123");
        workSite.setCompany(company);
        workSite.setCreatedAt(OffsetDateTime.now());
        workSite.setUpdatedAt(OffsetDateTime.now());

        workSiteRequestDto = new WorkSiteRequestDto();
        workSiteRequestDto.setName("Nueva Sede");
        workSiteRequestDto.setAddress("Avenida Siempreviva 742");
    }

    @Test
    void testGetMyWorkSites() {
        when(userService.getAuthenticatedUser()).thenReturn(admin);
        when(workSiteRepository.findByCompany_Id(company.getId())).thenReturn(Collections.singletonList(workSite));

        List<WorkSiteResponseDto> result = workSiteService.getMyWorkSites();

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(workSite.getName(), result.getFirst().getName()),
                () -> assertEquals(workSite.getAddress(), result.getFirst().getAddress())
        );

        verify(userService).getAuthenticatedUser();
        verify(workSiteRepository).findByCompany_Id(company.getId());
    }

    @Test
    void testCreateWorkSiteSuccess() {
        when(userService.getAuthenticatedUser()).thenReturn(admin);
        when(workSiteRepository.save(any(WorkSite.class))).thenAnswer(invocation -> {
            WorkSite ws = invocation.getArgument(0);
            ws.setId(UUID.randomUUID());
            return ws;
        });

        WorkSiteResponseDto result = workSiteService.createWorkSite(workSiteRequestDto);

        assertAll(
                () -> assertNotNull(result),
                () -> assertNotNull(result.getId()),
                () -> assertEquals(workSiteRequestDto.getName(), result.getName()),
                () -> assertEquals(workSiteRequestDto.getAddress(), result.getAddress())
        );

        verify(userService).getAuthenticatedUser();
        verify(workSiteRepository).save(any(WorkSite.class));
    }

    @Test
    void testUpdateWorkSiteSuccess() {
        when(userService.getAuthenticatedUser()).thenReturn(admin);
        when(workSiteRepository.findByIdAndCompany_Id(workSite.getId(), company.getId())).thenReturn(Optional.of(workSite));
        when(workSiteRepository.save(any(WorkSite.class))).thenReturn(workSite);

        WorkSiteResponseDto result = workSiteService.updateWorkSite(workSite.getId(), workSiteRequestDto);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(workSite.getId(), result.getId()),
                () -> assertEquals(workSiteRequestDto.getName(), result.getName()),
                () -> assertEquals(workSiteRequestDto.getAddress(), result.getAddress())
        );

        verify(userService).getAuthenticatedUser();
        verify(workSiteRepository).findByIdAndCompany_Id(workSite.getId(), company.getId());
        verify(workSiteRepository).save(any(WorkSite.class));
    }

    @Test
    void testUpdateWorkSiteNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(userService.getAuthenticatedUser()).thenReturn(admin);
        when(workSiteRepository.findByIdAndCompany_Id(nonExistentId, company.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> workSiteService.updateWorkSite(nonExistentId, workSiteRequestDto));

        assertEquals("La sede no existe o no pertenece a tu empresa", exception.getMessage());

        verify(userService).getAuthenticatedUser();
        verify(workSiteRepository).findByIdAndCompany_Id(nonExistentId, company.getId());
        verify(workSiteRepository, never()).save(any(WorkSite.class));
    }

    @Test
    void testDeleteWorkSiteSuccess() {
        when(userService.getAuthenticatedUser()).thenReturn(admin);
        when(workSiteRepository.findByIdAndCompany_Id(workSite.getId(), company.getId())).thenReturn(Optional.of(workSite));
        when(workScheduleRepository.findBySite_Id(workSite.getId())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> workSiteService.deleteWorkSite(workSite.getId()));

        verify(userService).getAuthenticatedUser();
        verify(workSiteRepository).findByIdAndCompany_Id(workSite.getId(), company.getId());
        verify(workScheduleRepository).findBySite_Id(workSite.getId());
        verify(workSiteRepository).delete(workSite);
    }

    @Test
    void testDeleteWorkSiteWithAssignedSchedules() {
        User employeeUser = new User();
        Profile employeeProfile = new Profile();
        employeeProfile.setFullName("Juan Pérez");
        employeeUser.setProfile(employeeProfile);

        WorkSchedule schedule = new WorkSchedule();
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(17, 0));
        schedule.setEmployee(employeeProfile);

        when(userService.getAuthenticatedUser()).thenReturn(admin);
        when(workSiteRepository.findByIdAndCompany_Id(workSite.getId(), company.getId())).thenReturn(Optional.of(workSite));
        when(workScheduleRepository.findBySite_Id(workSite.getId())).thenReturn(Collections.singletonList(schedule));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> workSiteService.deleteWorkSite(workSite.getId()));

        assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode()),
                () -> assertTrue(exception.getReason() != null && exception.getReason().contains("No se puede eliminar la sede porque tiene horarios asignados")),
                () -> assertTrue(exception.getReason() != null && exception.getReason().contains("MONDAY 09:00-17:00 (Juan Pérez)"))
        );

        verify(userService).getAuthenticatedUser();
        verify(workSiteRepository).findByIdAndCompany_Id(workSite.getId(), company.getId());
        verify(workScheduleRepository).findBySite_Id(workSite.getId());
        verify(workSiteRepository, never()).delete(any(WorkSite.class));
    }

    @Test
    void testDeleteWorkSiteNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(userService.getAuthenticatedUser()).thenReturn(admin);
        when(workSiteRepository.findByIdAndCompany_Id(nonExistentId, company.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> workSiteService.deleteWorkSite(nonExistentId));

        assertEquals("La sede no existe o no pertenece a tu empresa", exception.getMessage());

        verify(userService).getAuthenticatedUser();
        verify(workSiteRepository).findByIdAndCompany_Id(nonExistentId, company.getId());
        verify(workScheduleRepository, never()).findBySite_Id(any(UUID.class));
        verify(workSiteRepository, never()).delete(any(WorkSite.class));
    }
}
