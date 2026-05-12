package com.worktrace.worktracebackend.service.workSchedule;

import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleDayRequestDto;
import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleRequestDto;
import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleResponseDto;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.ProfileRepository;
import com.worktrace.worktracebackend.repository.WorkScheduleRepository;
import com.worktrace.worktracebackend.repository.WorkSiteRepository;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import com.worktrace.worktracebackend.service.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkScheduleServiceTest {

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserService userService;

    @Mock
    private WorkSiteRepository workSiteRepository;

    @InjectMocks
    private WorkScheduleService workScheduleService;

    private UserAndCompanyInfo userAndCompanyInfo;
    private User worker;
    private Profile employeeProfile;
    private WorkSite workSite;
    private UUID employeeId;
    private UUID workSiteId;

    @BeforeEach
    void setUp() {
        UUID companyId = UUID.randomUUID();
        Company company = new Company();
        company.setId(companyId);
        company.setCompanyName("Empresa de Prueba");

        employeeId = UUID.randomUUID();
        worker = new User();
        worker.setId(employeeId);
        worker.setCompany(company);

        employeeProfile = new Profile();
        employeeProfile.setUserId(employeeId);

        workSiteId = UUID.randomUUID();
        workSite = new WorkSite();
        workSite.setId(workSiteId);
        workSite.setCompany(company);
        workSite.setName("Oficina Central");
        workSite.setAddress("Calle Falsa 123");

        userAndCompanyInfo = new UserAndCompanyInfo(worker, company, employeeProfile);
    }

    @Test
    void testAssignWorkScheduleSuccess() {
        WorkScheduleDayRequestDto scheduleDto = new WorkScheduleDayRequestDto(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), workSiteId);
        WorkScheduleRequestDto requestDto = new WorkScheduleRequestDto(employeeId, List.of(scheduleDto));

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(userService.getUserById(employeeId)).thenReturn(worker);
        when(profileRepository.findById(employeeId)).thenReturn(Optional.of(employeeProfile));
        when(workSiteRepository.findById(workSiteId)).thenReturn(Optional.of(workSite));
        when(workScheduleRepository.findByEmployee_UserId(employeeId)).thenReturn(new ArrayList<>());

        workScheduleService.assignWorkSchedule(requestDto);

        verify(workScheduleRepository, times(1)).saveAllAndFlush(any());
    }

    @Test
    void testAssignWorkScheduleThrowsExceptionWhenWorkerNotInCompany() {
        Company anotherCompany = new Company();
        anotherCompany.setId(UUID.randomUUID());
        worker.setCompany(anotherCompany);

        WorkScheduleRequestDto requestDto = new WorkScheduleRequestDto(employeeId, List.of());

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(userService.getUserById(employeeId)).thenReturn(worker);
        when(profileRepository.findById(employeeId)).thenReturn(Optional.of(employeeProfile));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> workScheduleService.assignWorkSchedule(requestDto));

        assertEquals("El trabajador no pertenece a tu empresa", exception.getMessage());
    }

    @Test
    void testAssignWorkScheduleThrowsExceptionForDuplicateDays() {
        WorkScheduleDayRequestDto scheduleDto1 = new WorkScheduleDayRequestDto(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), workSiteId);
        WorkScheduleDayRequestDto scheduleDto2 = new WorkScheduleDayRequestDto(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(18, 0), workSiteId);
        WorkScheduleRequestDto requestDto = new WorkScheduleRequestDto(employeeId, List.of(scheduleDto1, scheduleDto2));

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(userService.getUserById(employeeId)).thenReturn(worker);
        when(profileRepository.findById(employeeId)).thenReturn(Optional.of(employeeProfile));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> workScheduleService.assignWorkSchedule(requestDto));

        assertEquals("Hay días de la semana duplicados en la solicitud", exception.getMessage());
    }

    @Test
    void testGetEmployeeSchedulesSuccess() {
        WorkSchedule schedule = new WorkSchedule();
        schedule.setDayOfWeek(DayOfWeek.TUESDAY);
        schedule.setStartTime(LocalTime.of(10, 0));
        schedule.setEndTime(LocalTime.of(18, 0));
        schedule.setSite(workSite);

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(userService.getUserById(employeeId)).thenReturn(worker);
        when(workScheduleRepository.findByEmployee_UserId(employeeId)).thenReturn(List.of(schedule));

        List<WorkScheduleResponseDto> response = workScheduleService.getEmployeeSchedules(employeeId);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(1, response.size()),
                () -> assertEquals(DayOfWeek.TUESDAY, response.getFirst().getDayOfWeek()),
                () -> assertEquals("Oficina Central", response.getFirst().getPlace())
        );
    }

    @Test
    void testGetEmployeeSchedulesThrowsExceptionWhenWorkerNotInCompany() {
        Company anotherCompany = new Company();
        anotherCompany.setId(UUID.randomUUID());
        worker.setCompany(anotherCompany);

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(userService.getUserById(employeeId)).thenReturn(worker);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> workScheduleService.getEmployeeSchedules(employeeId));

        assertEquals("El trabajador no pertenece a tu empresa", exception.getMessage());
    }
}
