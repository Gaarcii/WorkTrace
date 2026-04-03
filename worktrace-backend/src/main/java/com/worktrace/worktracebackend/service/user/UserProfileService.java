package com.worktrace.worktracebackend.service.user;

import com.worktrace.worktracebackend.dto.user.*;
import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleResponseDto;
import com.worktrace.worktracebackend.exception.NotFoundException;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.Profile;
import com.worktrace.worktracebackend.model.Role;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.repository.ProfileRepository;
import com.worktrace.worktracebackend.repository.UserRepository;
import com.worktrace.worktracebackend.security.JwtService;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UsuarioYCompaniaInfo;
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

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public UserResponseDto getProfile() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        return construirUserResponseDto(info.getProfile(), info.getUser());
    }

    @Transactional
    public UserResponseDto putProfile(UserRequestDto requestDto) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
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

        } else if (Boolean.TRUE.equals(requestDto.getEliminarAvatar())) {

            if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isBlank()) {
                String oldFilename = profile.getAvatarUrl().substring(profile.getAvatarUrl().lastIndexOf('/') + 1);
                storageService.delete(oldFilename, "avatars");
            }
            profile.setAvatarUrl(null);
        }

        if (requestDto.getTelefono() != null) {
            profile.setPhone(requestDto.getTelefono());
        }
        profile.setUpdatedAt(OffsetDateTime.now());

        String nuevoToken = null;

        String nuevoEmail = requestDto.getEmail();

        if (nuevoEmail != null && !nuevoEmail.trim().isEmpty() && !user.getEmail().equals(nuevoEmail)) {

            if (requestDto.getContrasenaActual() == null ||
                    !passwordEncoder.matches(requestDto.getContrasenaActual(), user.getPasswordHash())) {
                throw new IllegalArgumentException("Contraseña incorrecta. No puedes cambiar el email.");
            }
            user.setEmail(nuevoEmail);
            nuevoToken = jwtService.generateToken(user);
        }

        UserResponseDto responseDto = construirUserResponseDto(profile, user);
        responseDto.setTokenActualizado(nuevoToken);
        return responseDto;
    }


    @Transactional(readOnly = true)
    public Long getCompanyWorkers() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();
        return userRepository.countUsersByCompany_Id(company.getId());
    }

    @Transactional(readOnly = true)
    public List<DepartmentStatDto> getDepartmetnStatus() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
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

    @Transactional(readOnly = true)
    public Page<EmployeeResponseDto> empleadosPorEmpresa(Pageable pageable) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
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
                        estadoEmpleado(profile),
                        profile.getUser().getCreatedAt()
                )
        );
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmpleadoById(UUID employeeId) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
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
                estadoEmpleado(profile),
                profile.getUser().getCreatedAt()
        );
    }

    private String estadoEmpleado(Profile profile) {
        if (!profile.getIsActive()) {
            return "Inactivo";
        }
        if (profile.getIsFirstLogin()) {
            return "Pendiente";
        }
        return "Activo";
    }
    
    private UserResponseDto construirUserResponseDto(Profile profile, User user) {
        List<WorkScheduleResponseDto> horario = profile.getWorkSchedules().stream()
                .map(h -> {
                    LocalTime start = h.getStartTime();
                    LocalTime end = h.getEndTime();
                    long minutos = Duration.between(start, end).toMinutes();
                    if (minutos < 0) {
                        minutos += 24 * 60;
                    }
                    return new WorkScheduleResponseDto(
                            h.getSite().getName(),
                            h.getSite().getAddress(),
                            h.getDayOfWeek(),
                            start,
                            end,
                            minutos / 60
                    );
                })
                .toList();

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setNombreCompleto(profile.getFullName());
        responseDto.setAvatarUrl(profile.getAvatarUrl());
        responseDto.setPuestoTrabajo(profile.getPosition() != null ? profile.getPosition().getTitle() : "Sin asignar");
        responseDto.setEmail(user.getEmail());
        responseDto.setTelefono(profile.getPhone());
        responseDto.setHorario(horario);

        return responseDto;
    }

}
