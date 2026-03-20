package com.worktrace.worktracebackend.service.worker;

import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleResponseDto;
import com.worktrace.worktracebackend.dto.worker.WorkerRequestDto;
import com.worktrace.worktracebackend.dto.worker.WorkerResponseDto;
import com.worktrace.worktracebackend.model.Profile;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.security.JwtService;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UsuarioYCompaniaInfo;
import com.worktrace.worktracebackend.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public WorkerResponseDto getProfile() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        return construirWorkerResponseDto(info.getProfile(), info.getUser());
    }

    @Transactional
    public WorkerResponseDto putProfile(WorkerRequestDto requestDto) {
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

        WorkerResponseDto responseDto = construirWorkerResponseDto(profile, user);
        responseDto.setTokenActualizado(nuevoToken);
        return responseDto;
    }

    private WorkerResponseDto construirWorkerResponseDto(Profile profile, User user) {
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

        WorkerResponseDto responseDto = new WorkerResponseDto();
        responseDto.setNombreCompleto(profile.getFullName());
        responseDto.setAvatarUrl(profile.getAvatarUrl());
        responseDto.setPuestoTrabajo(profile.getPosition() != null ? profile.getPosition().getTitle() : "Sin asignar");
        responseDto.setEmail(user.getEmail());
        responseDto.setTelefono(profile.getPhone());
        responseDto.setHorario(horario);

        return responseDto;
    }
}
