package com.worktrace.worktracebackend.service.user;

import com.worktrace.worktracebackend.dto.user.EditEmployeeWorkDataRequestDto;
import com.worktrace.worktracebackend.dto.user.EmployeeResponseDto;
import com.worktrace.worktracebackend.dto.user.UserRequestDto;
import com.worktrace.worktracebackend.dto.user.UserResponseDto;
import com.worktrace.worktracebackend.exception.NotFoundException;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.JobPositionRepository;
import com.worktrace.worktracebackend.repository.ProfileRepository;
import com.worktrace.worktracebackend.security.JwtService;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private StorageService storageService;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private JobPositionRepository jobPositionRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    private User user;
    private Profile profile;
    private Company company;
    private JobPosition jobPosition;
    private UserAndCompanyInfo userAndCompanyInfo;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(UUID.randomUUID());
        company.setCompanyName("Mi Empresa");

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setRole(Role.WORKER);
        user.setCompany(company);
        user.setCreatedAt(OffsetDateTime.now());
        user.setIsEnabled(true);

        jobPosition = new JobPosition();
        jobPosition.setId(UUID.randomUUID());
        jobPosition.setTitle("Desarrollador Backend");
        jobPosition.setCompany(company);

        profile = new Profile();
        profile.setUserId(user.getId());
        profile.setFullName("Nombre Completo");
        profile.setEmployeeCode("EMP001");
        profile.setIsActive(true);
        profile.setIsFirstLogin(false);
        profile.setUpdatedAt(OffsetDateTime.now());
        profile.setUser(user);
        profile.setPosition(jobPosition);
        profile.setWeeklyHours(new BigDecimal("40.0"));
        profile.setWorkSchedules(new ArrayList<>());

        user.setProfile(profile);

        userAndCompanyInfo = new UserAndCompanyInfo(user, company, profile);
    }

    @Test
    void testGetProfileSuccess() {
        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);

        UserResponseDto response = userProfileService.getProfile();

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(profile.getFullName(), response.getFullName()),
                () -> assertEquals(user.getEmail(), response.getEmail()),
                () -> {
                    if (profile.getPosition() != null) {
                        assertEquals(jobPosition.getTitle(), response.getJobPosition());
                    }
                }
        );
    }

    @Test
    void testUpdateProfileSuccess_OnlyPhone() {
        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);

        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setPhone("123456789");

        UserResponseDto response = userProfileService.updateProfile(requestDto);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals("123456789", profile.getPhone()),
                () -> assertNull(response.getUpdatedToken())
        );
        verify(storageService, never()).store(any(), any());
    }

    @Test
    void testUpdateProfileSuccess_NewAvatar() {
        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        MockMultipartFile avatarFile = new MockMultipartFile("avatar", "avatar.jpg", "image/jpeg", "test-image".getBytes());
        String newFilename = "new-avatar.jpg";
        String newAvatarUrl = "http://localhost/avatars/" + newFilename;

        when(storageService.store(avatarFile, "avatars")).thenReturn(newFilename);
        when(storageService.getUrl(newFilename, "avatars")).thenReturn(newAvatarUrl);

        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setAvatar(avatarFile);

        userProfileService.updateProfile(requestDto);

        assertEquals(newAvatarUrl, profile.getAvatarUrl());
        verify(storageService, times(1)).store(avatarFile, "avatars");
        verify(storageService, never()).delete(any(), any());
    }

    @Test
    void testUpdateProfileSuccess_ReplaceAvatar() {
        String oldFilename = "old-avatar.jpg";
        profile.setAvatarUrl("http://localhost/avatars/" + oldFilename);

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        MockMultipartFile avatarFile = new MockMultipartFile("avatar", "avatar.jpg", "image/jpeg", "test-image".getBytes());
        String newFilename = "new-avatar.jpg";
        String newAvatarUrl = "http://localhost/avatars/" + newFilename;

        when(storageService.store(avatarFile, "avatars")).thenReturn(newFilename);
        when(storageService.getUrl(newFilename, "avatars")).thenReturn(newAvatarUrl);

        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setAvatar(avatarFile);

        userProfileService.updateProfile(requestDto);

        assertEquals(newAvatarUrl, profile.getAvatarUrl());
        verify(storageService, times(1)).delete(eq(oldFilename), eq("avatars"));
        verify(storageService, times(1)).store(avatarFile, "avatars");
    }

    @Test
    void testUpdateProfileSuccess_DeleteAvatar() {
        String oldFilename = "old-avatar.jpg";
        profile.setAvatarUrl("http://localhost/avatars/" + oldFilename);

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);

        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setDeleteAvatar(true);

        userProfileService.updateProfile(requestDto);

        assertNull(profile.getAvatarUrl());
        verify(storageService, times(1)).delete(eq(oldFilename), eq("avatars"));
        verify(storageService, never()).store(any(), any());
    }


    @Test
    void testUpdateProfileSuccess_ChangeEmail() {
        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(passwordEncoder.matches("currentPassword", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("newJwtToken");

        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setEmail("new@example.com");
        requestDto.setActualPassword("currentPassword");

        UserResponseDto response = userProfileService.updateProfile(requestDto);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals("new@example.com", user.getEmail()),
                () -> assertEquals("newJwtToken", response.getUpdatedToken())
        );
    }

    @Test
    void testUpdateProfileFail_ChangeEmailIncorrectPassword() {
        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setEmail("new@example.com");
        requestDto.setActualPassword("wrongPassword");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userProfileService.updateProfile(requestDto));

        assertEquals("Contraseña incorrecta. No puedes cambiar el email.", exception.getMessage());
    }

    @Test
    void testGetEmployeeByIdSuccess() {
        UUID employeeId = profile.getUserId();
        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(profileRepository.findById(employeeId)).thenReturn(Optional.of(profile));

        EmployeeResponseDto response = userProfileService.getEmployeeById(employeeId);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(profile.getFullName(), response.getName()),
                () -> assertEquals(user.getEmail(), response.getEmail()),
                () -> {
                    if (profile.getPosition() != null) {
                        assertEquals(jobPosition.getTitle(), response.getJobPosition());
                    }
                },
                () -> assertEquals("40.0", response.getWeeklyHours())
        );

        verify(profileRepository, times(1)).findById(employeeId);
    }

    @Test
    void testGetEmployeeByIdNotFound() {
        UUID employeeId = UUID.randomUUID();
        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(profileRepository.findById(employeeId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userProfileService.getEmployeeById(employeeId));

        assertEquals("Empleado no encontrado", exception.getMessage());
    }

    @Test
    void testGetEmployeeById_EmployeeNotFromSameCompany() {
        UUID employeeId = UUID.randomUUID();
        Company anotherCompany = new Company();
        anotherCompany.setId(UUID.randomUUID());

        User employeeUser = new User();
        employeeUser.setCompany(anotherCompany);
        Profile employeeProfile = new Profile();
        employeeProfile.setUser(employeeUser);

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(profileRepository.findById(employeeId)).thenReturn(Optional.of(employeeProfile));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> userProfileService.getEmployeeById(employeeId));

        assertEquals("El empleado no pertenece a tu empresa", exception.getMessage());
    }

    @Test
    void testEditEmployeeWorkDataSuccess() {
        UUID employeeId = UUID.randomUUID();
        UUID positionId = UUID.randomUUID();

        User employeeUser = new User();
        employeeUser.setRole(Role.WORKER);
        employeeUser.setCompany(company);

        Profile employeeProfile = new Profile();
        employeeProfile.setUser(employeeUser);

        JobPosition newJobPosition = new JobPosition();
        newJobPosition.setId(positionId);
        newJobPosition.setCompany(company);

        EditEmployeeWorkDataRequestDto requestDto = new EditEmployeeWorkDataRequestDto();
        requestDto.setPositionId(positionId);
        requestDto.setWeeklyHours(new BigDecimal("35.5"));

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(profileRepository.findById(employeeId)).thenReturn(Optional.of(employeeProfile));
        when(jobPositionRepository.findById(positionId)).thenReturn(Optional.of(newJobPosition));

        userProfileService.editEmployeeWorkData(employeeId, requestDto);

        assertAll(
                () -> assertEquals(newJobPosition, employeeProfile.getPosition()),
                () -> assertEquals(new BigDecimal("35.5"), employeeProfile.getWeeklyHours())
        );
    }

    @Test
    void testEditEmployeeWorkDataFail_EmployeeNotFound() {
        UUID employeeId = UUID.randomUUID();
        EditEmployeeWorkDataRequestDto requestDto = new EditEmployeeWorkDataRequestDto();
        requestDto.setPositionId(UUID.randomUUID());

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(profileRepository.findById(employeeId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userProfileService.editEmployeeWorkData(employeeId, requestDto));

        assertEquals("Empleado no encontrado", exception.getMessage());
    }

    @Test
    void testEditEmployeeWorkDataFail_NotAWorker() {
        UUID employeeId = UUID.randomUUID();
        User adminUser = new User();
        adminUser.setRole(Role.ADMIN);
        adminUser.setCompany(company);
        Profile adminProfile = new Profile();
        adminProfile.setUser(adminUser);

        EditEmployeeWorkDataRequestDto requestDto = new EditEmployeeWorkDataRequestDto();
        requestDto.setPositionId(UUID.randomUUID());

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(profileRepository.findById(employeeId)).thenReturn(Optional.of(adminProfile));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> userProfileService.editEmployeeWorkData(employeeId, requestDto));

        assertEquals("Solo se puede editar un empleado", exception.getMessage());
    }

    @Test
    void testEditEmployeeWorkDataFail_JobPositionNotFound() {
        UUID employeeId = UUID.randomUUID();
        UUID positionId = UUID.randomUUID();

        User employeeUser = new User();
        employeeUser.setRole(Role.WORKER);
        employeeUser.setCompany(company);
        Profile employeeProfile = new Profile();
        employeeProfile.setUser(employeeUser);

        EditEmployeeWorkDataRequestDto requestDto = new EditEmployeeWorkDataRequestDto();
        requestDto.setPositionId(positionId);

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(profileRepository.findById(employeeId)).thenReturn(Optional.of(employeeProfile));
        when(jobPositionRepository.findById(positionId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userProfileService.editEmployeeWorkData(employeeId, requestDto));

        assertEquals("El puesto de trabajo no existe", exception.getMessage());
    }
}
