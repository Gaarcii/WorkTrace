package com.worktrace.worktracebackend.dailyclosure.domain.model;

/**
 * Posibles resultados de la verificación de integridad de un cierre diario.
 * <p>
 * Clasifican el estado de los fichajes de un día respecto al hash almacenado en
 * su cierre, distinguiendo entre coincidencia, modificaciones legítimas y
 * manipulaciones no trazadas.
 */
public enum IntegrityResult {

    /** El hash recalculado coincide con el almacenado: los datos están intactos. */
    VALID,

    /** El hash difiere, pero los cambios quedan explicados por modificaciones
     *  legítimas registradas en el log de auditoría (p. ej. ajustes de admin). */
    MODIFIED,

    /** El hash difiere y la discrepancia no puede explicarse con la auditoría
     *  (sin auditoría, o con modificación directa en BD): integridad rota. */
    CORRUPTED,
}
