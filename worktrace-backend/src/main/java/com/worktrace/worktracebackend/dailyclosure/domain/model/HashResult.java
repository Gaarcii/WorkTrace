package com.worktrace.worktracebackend.dailyclosure.domain.model;

/**
 * Resultado del cálculo del hash encadenado de un cierre diario.
 * <p>
 * Devuelto por {@code DailyHashChain#compute}, agrupa el hash versionado
 * resultante y el número de fichajes que se procesaron para obtenerlo.
 *
 * @param hash        Hash versionado calculado (p. ej. {@code "V2:<hex>"}).
 * @param recordCount Número de snapshots de fichaje incluidos en el cálculo.
 */
public record HashResult(
        String hash,
        int recordCount
) {
}
