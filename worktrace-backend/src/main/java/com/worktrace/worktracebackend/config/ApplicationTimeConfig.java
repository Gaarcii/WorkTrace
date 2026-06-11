package com.worktrace.worktracebackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

/**
 * Configuración de tiempo de la aplicación.
 * <p>
 * Expone la zona horaria de referencia del sistema como un {@link ZoneId}
 * inyectable, de modo que los casos de uso comparen horas "de pared" (p. ej. la
 * hora de un horario frente a la hora real de un fichaje) de forma determinista,
 * sin depender de la zona por defecto de la JVM. Configurable mediante la
 * propiedad {@code worktrace.timezone} (por defecto {@code Europe/Madrid}).
 */
@Configuration
public class ApplicationTimeConfig {

    @Bean
    public ZoneId applicationZoneId(@Value("${worktrace.timezone:Europe/Madrid}") String timezone) {
        return ZoneId.of(timezone);
    }
}
