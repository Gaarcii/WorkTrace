package com.worktrace.worktracebackend.service.user;

import com.worktrace.worktracebackend.dto.user.*;
import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleResponseDto;
import com.worktrace.worktracebackend.exception.NotFoundException;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.JobPosition;
import com.worktrace.worktracebackend.model.Profile;
import com.worktrace.worktracebackend.model.Role;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.repository.JobPositionRepository;
import com.worktrace.worktracebackend.repository.ProfileRepository;
import com.worktrace.worktracebackend.repository.UserRepository;
import com.worktrace.worktracebackend.security.JwtService;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import com.worktrace.worktracebackend.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestionar el perfil de los usuarios y la información de los empleados.
 * Centraliza la lógica de negocio para que los usuarios puedan ver y actualizar sus propios datos,
 * y para que los administradores puedan gestionar la información de los empleados de su empresa.
 */
@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final JobPositionRepository jobPositionRepository;

    /**
     * Obtiene el perfil del usuario actualmente autenticado.
     * Este método construye un DTO con la información combinada del perfil y el usuario
     * para ser consumido por el frontend.
     *
     * @return Un {@link UserResponseDto} con los datos del perfil del usuario.
     */
    @Transactional(readOnly = true)
    public UserResponseDto getProfile() {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        return buildUserResponseDto(info.getProfile(), info.getUser());
    }

    /**
     * Actualiza el perfil del usuario autenticado.
     * Permite modificar datos como el teléfono, el avatar y el email. Si se cambia el email,
     * se requiere la contraseña actual para seguridad y se genera un nuevo token JWT
     * para mantener la sesión activa con la nueva identidad.
     *
     * @param requestDto El DTO con los datos a actualizar.
     * @return Un {@link UserResponseDto} con el perfil actualizado y, opcionalmente, un nuevo token JWT.
     */
    @Transactional
    public UserResponseDto updateProfile(UserRequestDto requestDto) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Profile profile = info.getProfile();
        User user = info.getUser();

        if (requestDto.getAvatar() != null && !requestDto.getAvatar().isEmpty()) {

            if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isBlank()) {
                String oldFilename = profile.getAvatarUrl().substring(profile.getAvatarUrl().lastIndexOf('/') + 1);
                storageService.delete(oldFilename, "avatars");
            }

            String storedFilename = storageService.store(requestDto.getAvatar(), "avatars");
            String avatarUrl = storageService.getUrl(storedFilename, "avatars");
            profile.setAvatarUrl(avatarUrl);

        } else if (Boolean.TRUE.equals(requestDto.getDeleteAvatar())) {

            if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isBlank()) {
                String oldFilename = profile.getAvatarUrl().substring(profile.getAvatarUrl().lastIndexOf('/') + 1);
                storageService.delete(oldFilename, "avatars");
            }
            profile.setAvatarUrl(null);
        }

        if (requestDto.getPhone() != null) {
            profile.setPhone(requestDto.getPhone());
        }
        profile.setUpdatedAt(OffsetDateTime.now());

        String updatedJwtToken = null;

        String newEmail = requestDto.getEmail();

        if (newEmail != null && !newEmail.trim().isEmpty() && !user.getEmail().equals(newEmail)) {

            if (requestDto.getActualPassword() == null ||
                    !passwordEncoder.matches(requestDto.getActualPassword(), user.getPasswordHash())) {
                throw new IllegalArgumentException("Contraseña incorrecta. No puedes cambiar el email.");
            }
            user.setEmail(newEmail);
            updatedJwtToken = jwtService.generateToken(user);
        }

        UserResponseDto responseDto = buildUserResponseDto(profile, user);
        responseDto.setUpdatedToken(updatedJwtToken);
        return responseDto;
    }


    /**
     * Cuenta el número total de empleados en la empresa del administrador autenticado.
     *
     * @return El número total de empleados.
     */
    @Transactional(readOnly = true)
    public Long countCompanyEmployees() {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();
        return userRepository.countUsersByCompany_Id(company.getId());
    }

    /**
     * Obtiene estadísticas sobre la distribución de empleados por departamento.
     * Este método es útil para que los administradores visualicen la estructura de su plantilla.
     *
     * @return Una lista de {@link DepartmentStatDto} con el recuento de empleados por departamento.
     */
    @Transactional(readOnly = true)
    public List<DepartmentStatDto> getDepartmentStats() {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        List<DepartmentStatProjection> departmentStatProjections = userRepository
                .getDepartamentStats(company.getId());

        return departmentStatProjections.stream()
                .map(department -> new DepartmentStatDto(
                        department.getDepartamento(),
                        department.getTotalTrabajadores(),
                        department.getTrabajadoresActivos()
                ))
                .toList();
    }

    /**
     * Obtiene una lista paginada de los empleados de la empresa del administrador.
     *
     * @param pageable La información de paginación.
     * @return Una página de {@link EmployeeResponseDto} con los datos de los empleados.
     */
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDto> getEmployeesByCompany(Pageable pageable) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        Page<Profile> profilePage = profileRepository
                .findByUser_Company_IdAndUser_Role(company.getId(), Role.WORKER, pageable);
        return profilePage.map(
                profile -> new EmployeeResponseDto(
                        profile.getUserId(),
                        profile.getFullName(),
                        profile.getEmployeeCode(),
                        profile.getUser().getEmail(),
                        profile.getAvatarUrl(),
                        profile.getPhone(),
                        profile.getPosition().getTitle(),
                        profile.getWeeklyHours().toString(),
                        getEmployeeStatus(profile),
                        profile.getUser().getCreatedAt()
                )
        );
    }

    /**
     * Obtiene los detalles de un empleado específico por su ID.
     *
     * @param employeeId El UUID del empleado a consultar.
     * @return Un {@link EmployeeResponseDto} con los detalles del empleado.
     */
    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployeeById(UUID employeeId) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        Profile profile = profileRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

        if (!profile.getUser().getCompany().getId().equals(company.getId())) {
            throw new IllegalStateException("El empleado no pertenece a tu empresa");
        }

        return new EmployeeResponseDto(
                profile.getUserId(),
                profile.getFullName(),
                profile.getEmployeeCode(),
                profile.getUser().getEmail(),
                profile.getAvatarUrl(),
                profile.getPhone(),
                profile.getPosition().getTitle(),
                profile.getWeeklyHours().toString(),
                getEmployeeStatus(profile),
                profile.getUser().getCreatedAt()
        );
    }

    /**
     * Permite a un administrador editar los datos laborales de un empleado, como su puesto y horas semanales.
     * Se realizan validaciones para asegurar que el empleado y el puesto de trabajo pertenecen a la misma empresa.
     *
     * @param employeeId El UUID del empleado a modificar.
     * @param dto El DTO con los nuevos datos laborales.
     */
    @Transactional
    public void editEmployeeWorkData(UUID employeeId, EditEmployeeWorkDataRequestDto dto) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        Profile profile = profileRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

        if (!profile.getUser().getCompany().getId().equals(company.getId())) {
            throw new IllegalStateException("El empleado no pertenece a tu empresa");
        }

        if (!Role.WORKER.equals(profile.getUser().getRole())) {
            throw new IllegalStateException("Solo se puede editar un empleado");
        }

        JobPosition jobPosition = jobPositionRepository.findById(dto.getPositionId())
                .orElseThrow(() -> new IllegalArgumentException("El puesto de trabajo no existe"));

        if (!jobPosition.getCompany().getId().equals(company.getId())) {
            throw new IllegalStateException("El puesto no pertenece a tu empresa");
        }

        profile.setPosition(jobPosition);
        profile.setWeeklyHours(dto.getWeeklyHours());
        profile.setUpdatedAt(OffsetDateTime.now());
    }

    private String getEmployeeStatus(Profile profile) {
        if (!profile.getIsActive()) {
            return "Inactivo";
        }
        if (profile.getIsFirstLogin()) {
            return "Pendiente";
        }
        return "Activo";
    }
    
    private UserResponseDto buildUserResponseDto(Profile profile, User user) {
        List<WorkScheduleResponseDto> schedules = profile.getWorkSchedules().stream()
                .map(schedule -> {
                    LocalTime start = schedule.getStartTime();
                    LocalTime end = schedule.getEndTime();
                    long minutes = Duration.between(start, end).toMinutes();
                    if (minutes < 0) {
                        minutes += 24 * 60;
                    }
                    return new WorkScheduleResponseDto(
                            schedule.getSite().getName(),
                            schedule.getSite().getAddress(),
                            schedule.getDayOfWeek(),
                            start,
                            end,
                            minutes / 60
                    );
                })
                .toList();

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setFullName(profile.getFullName());
        responseDto.setAvatarUrl(profile.getAvatarUrl());
        responseDto.setJobPosition(profile.getPosition() != null ? profile.getPosition().getTitle() : "Sin asignar");
        responseDto.setEmail(user.getEmail());
        responseDto.setPhone(profile.getPhone());
        responseDto.setSchedule(schedules);

        return responseDto;
    }

}
