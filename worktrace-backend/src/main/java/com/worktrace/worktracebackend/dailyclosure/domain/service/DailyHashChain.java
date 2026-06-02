package com.worktrace.worktracebackend.dailyclosure.domain.service;

import com.worktrace.worktracebackend.dailyclosure.domain.model.HashResult;
import com.worktrace.worktracebackend.dailyclosure.domain.model.TimeEntrySnapshot;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.stream.Stream;

/**
 * Servicio responsable de calcular la cadena de integridad (hash chain) para
 * las marcaciones (time entries) de un día concreto.
 * <p>
 * Propósito y contexto de negocio:
 * - Este servicio forma parte del subsistema de cierre diario (daily closure)
 * y se encarga de producir un resumen criptográfico inmutable que permite
 * detectar modificaciones retroactivas en las entradas de tiempo de una
 * compañía.
 * - Implementa el protocolo de hash V2: construye un flujo de bytes UTF-8
 * compuesto por el hash anterior seguido de los campos serializados de
 * cada snapshot en orden cronológico, y calcula SHA-256 sobre ese flujo.
 * - Está diseñado para procesar los snapshots de forma incremental (O(1)
 * memoria), por lo que puede usarse con colecciones grandes sin cargar
 * todo en memoria.
 * <p>
 * Responsabilidades principales:
 * - Garantizar compatibilidad con el formato V2 existente (serialización
 * exacta de campos y ausencia de separadores explícitos).
 * - Proveer el valor de hash versionado (por ejemplo "V2:abcdef...") y el
 * recuento de snapshots procesados, información que utilizan los casos de
 * uso de cierre diario y verificación de integridad.
 */
public class DailyHashChain {

    private static final String HASH_VERSION_V2 = "V2";
    private static final String HASH_SEPARATOR = ":";

    /**
     * Calcula el hash de integridad para una secuencia de snapshots de marcaciones
     * pertenecientes a un mismo día, encadenando el hash previo con los campos
     * serializados de cada snapshot y aplicando SHA-256 sobre el flujo resultante.
     * <p>
     * Regla de negocio (por qué / qué):
     * - Se debe producir un valor de integridad determinista que permita detectar
     * cualquier modificación o inserción retroactiva en las entradas de tiempo.
     * - El algoritmo respeta la versión V2 del protocolo (serialización exacta
     * de campos y ausencia de separadores) para mantener compatibilidad con
     * registros históricos y con los procesos de verificación que consumen
     * estos hashes.
     * - El cálculo se realiza de forma incremental (memoria O(1)), por lo que
     * es seguro procesar grandes volúmenes de snapshots sin cargarlos todos
     * en memoria.
     * <p>
     * Consideraciones operativas:
     * - Si la secuencia está vacía se añade el literal "NO_ACTIVITY" al digest
     * (comportamiento requerido por el protocolo del dominio para días sin
     * actividad).
     * - El llamador es responsable de cerrar el Stream proporcionado.
     *
     * @param snapshots Stream de {@link TimeEntrySnapshot} ordenados en modo
     *                  cronológico (primero la entrada más antigua). Cada
     *                  snapshot representa una instantánea inmutable de una
     *                  marcación que se serializa campo a campo.
     * @param prevHash  Hash previo (cadena) que actúa como semilla para la
     *                  cadena. Puede ser null si no existe un hash anterior;
     *                  en ese caso se serializa como "null" para preservar la
     *                  compatibilidad con la representación previa.
     * @return {@link com.worktrace.worktracebackend.dailyclosure.domain.model.HashResult}
     * que contiene la versión-prefijada del hash calculado (por ejemplo
     * "V2:...hex...") y el número de snapshots procesados.
     * @throws IllegalStateException si el proveedor criptográfico no ofrece
     *                               el algoritmo SHA-256 (condición fatal de la plataforma).
     */
    public HashResult compute(Stream<TimeEntrySnapshot> snapshots, String prevHash) {
        MessageDigest digest = newDigest();
        update(digest, prevHash);

        int[] count = {0};
        boolean[] hadData = {false};

        snapshots.forEach(s -> {
            hadData[0] = true;
            count[0]++;
            feedSnapshot(digest, s);
        });

        if (!hadData[0]) {
            update(digest, "NO_ACTIVITY");
        }

        String hex = HexFormat.of().formatHex(digest.digest());
        return new HashResult(HASH_VERSION_V2 + HASH_SEPARATOR + hex, count[0]);
    }

    private static void feedSnapshot(MessageDigest d, TimeEntrySnapshot s) {
        update(d, s.id());
        update(d, s.employeeId());
        update(d, s.workDate());
        updateTruncated(d, s.startAt());
        updateTruncated(d, s.endAt());
        update(d, s.startLat());
        update(d, s.startLng());
        updateNullable(d, s.endLat());
        updateNullable(d, s.endLng());
        update(d, s.startAccuracyM());
        updateNullable(d, s.endAccuracyM());
        update(d, s.startIp());
        updateNullable(d, s.endIp());
        update(d, s.startUserAgent());
        updateNullable(d, s.endUserAgent());
        update(d, s.startGeoip());
        updateNullable(d, s.endGeoip());
        updateNullable(d, s.flags());
        update(d, s.timeEntryStatus());
        updateTruncated(d, s.deletedAt());
        updateNullable(d, s.deletedBy());
        updateNullable(d, s.deleteReason());
        updateTruncated(d, s.createdAt());
        updateNullable(d, s.createdBy());
        updateTruncated(d, s.updatedAt());
        updateNullable(d, s.modificationReason());
        updateNullable(d, s.companyId());
    }

    private static void update(MessageDigest d, Object value) {
        d.update(String.valueOf(value).getBytes(StandardCharsets.UTF_8));
    }

    private static void updateTruncated(MessageDigest d, OffsetDateTime dt) {
        String v = dt != null ? dt.truncatedTo(ChronoUnit.SECONDS).toString() : "NULL";
        d.update(v.getBytes(StandardCharsets.UTF_8));
    }

    private static void updateNullable(MessageDigest d, Object value) {
        String v = value != null ? String.valueOf(value) : "NULL";
        d.update(v.getBytes(StandardCharsets.UTF_8));
    }

    private static void update(MessageDigest d, String value) {
        d.update(String.valueOf(value).getBytes(StandardCharsets.UTF_8));
    }

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
