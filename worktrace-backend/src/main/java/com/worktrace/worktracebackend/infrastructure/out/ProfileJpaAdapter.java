package com.worktrace.worktracebackend.infrastructure.out;

import com.worktrace.worktracebackend.repository.ProfileRepository;
import com.worktrace.worktracebackend.timeentry.domain.port.out.ProfileQueryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de salida que implementa {@link ProfileQueryPort} sobre JPA.
 * <p>
 * Consulta en modo solo lectura los datos de perfil necesarios para el dominio
 * de fichajes, como las horas semanales contratadas.
 */
@Component
@Transactional(readOnly = true)
public class ProfileJpaAdapter implements ProfileQueryPort {
    private final ProfileRepository profileRepository;

    public ProfileJpaAdapter(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Recupera las horas semanales contratadas del perfil del empleado.
     */
    @Override
    public Optional<BigDecimal> findWeeklyHoursByEmployee(UUID userId) {
        return profileRepository.findWeeklyHoursByUserId(userId);
    }
}
