package com.worktrace.worktracebackend.dailyclosure.domain.model;

import com.worktrace.worktracebackend.model.TimeEntryStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Instantánea inmutable del estado completo de un fichaje en un momento dado.
 * <p>
 * Es el modelo de dominio que consume el cálculo del hash de integridad: cada
 * campo se serializa de forma determinista al construir la cadena de hash del
 * cierre, por lo que cualquier modificación posterior de un fichaje altera el
 * hash resultante. También se usa para reconstruir el estado previo de un
 * fichaje a partir del log de auditoría.
 *
 * @param id                 Identificador del fichaje.
 * @param employeeId         Identificador del empleado (perfil) propietario.
 * @param workDate           Fecha laboral del fichaje.
 * @param startAt            Marca temporal de entrada.
 * @param endAt              Marca temporal de salida (nula si sigue abierto).
 * @param startLat           Latitud capturada en la entrada.
 * @param startLng           Longitud capturada en la entrada.
 * @param endLat             Latitud capturada en la salida.
 * @param endLng             Longitud capturada en la salida.
 * @param startAccuracyM     Precisión de la geolocalización de entrada (metros).
 * @param endAccuracyM       Precisión de la geolocalización de salida (metros).
 * @param startIp            Dirección IP registrada en la entrada.
 * @param endIp              Dirección IP registrada en la salida.
 * @param startUserAgent     User-Agent registrado en la entrada.
 * @param endUserAgent       User-Agent registrado en la salida.
 * @param startGeoip         Datos de geolocalización por IP en la entrada.
 * @param endGeoip           Datos de geolocalización por IP en la salida.
 * @param flags              Marcas o indicadores anti-fraude asociados.
 * @param timeEntryStatus    Estado del fichaje (p. ej. OPEN, CLOSED).
 * @param deletedAt          Instante del borrado lógico (nulo si no borrado).
 * @param deletedBy          Identificador de quien realizó el borrado lógico.
 * @param deleteReason       Motivo del borrado/anulación.
 * @param createdAt          Instante de creación del fichaje.
 * @param createdBy          Identificador de quien creó el fichaje.
 * @param updatedAt          Instante de la última modificación.
 * @param modificationReason Motivo de la última modificación.
 * @param companyId          Identificador de la empresa (aislamiento multi-tenant).
 */
public record TimeEntrySnapshot(
        UUID id,
        UUID employeeId,
        LocalDate workDate,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        BigDecimal startLat,
        BigDecimal startLng,
        BigDecimal endLat,
        BigDecimal endLng,
        Integer startAccuracyM,
        Integer endAccuracyM,
        String startIp,
        String endIp,
        String startUserAgent,
        String endUserAgent,
        Map<String, Object> startGeoip,
        Map<String, Object> endGeoip,
        List<String> flags,
        TimeEntryStatus timeEntryStatus,
        OffsetDateTime deletedAt,
        UUID deletedBy,
        String deleteReason,
        OffsetDateTime createdAt,
        UUID createdBy,
        OffsetDateTime updatedAt,
        String modificationReason,
        UUID companyId
) {

}
