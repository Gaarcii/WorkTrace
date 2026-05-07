package com.worktrace.worktracebackend.service.files;

import com.worktrace.worktracebackend.model.TimeEntry;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Clase de utilidad final (no instanciable) que proporciona métodos de apoyo para la exportación de fichajes.
 * Su propósito es centralizar la lógica de formato y resolución de estados de los fichajes,
 * garantizando que la representación de los datos sea consistente a través de diferentes formatos de archivo
 * como PDF y Excel.
 */
public final class TimeEntryExportSupport {

    private TimeEntryExportSupport() {
    }

    /**
     * Obtiene de forma segura el nombre completo del empleado a partir de un fichaje.
     * Se utiliza para evitar errores de puntero nulo si el empleado no está asociado al fichaje.
     * @param entry El fichaje del que se extraerá el nombre.
     * @return El nombre del empleado o un interrogante ("?") si no se encuentra.
     */
    public static String employeeName(TimeEntry entry) {
        return entry.getEmployee() != null ? entry.getEmployee().getFullName() : "?";
    }

    /**
     * Obtiene de forma segura el código o DNI del empleado a partir de un fichaje.
     * @param entry El fichaje del que se extraerá el código.
     * @return El código del empleado o un guion ("-") si no está disponible.
     */
    public static String employeeCode(TimeEntry entry) {
        return entry.getEmployee() != null && entry.getEmployee().getEmployeeCode() != null
                ? entry.getEmployee().getEmployeeCode()
                : "-";
    }

    /**
     * Formatea un objeto temporal (fecha/hora) a una cadena de texto.
     * Proporciona un valor por defecto si el objeto temporal es nulo, simplificando el código en los generadores de informes.
     * @param time El objeto temporal a formatear.
     * @param formatter El formateador a utilizar.
     * @param defaultValue El valor a devolver si el objeto temporal es nulo.
     * @return La cadena de texto formateada o el valor por defecto.
     */
    public static String formatTime(TemporalAccessor time, DateTimeFormatter formatter, String defaultValue) {
        return time != null ? formatter.format(time) : defaultValue;
    }

    /**
     * Comprueba si un fichaje tiene una bandera que indica baja precisión del GPS.
     * Sirve para identificar fichajes cuya ubicación podría no ser fiable.
     * @param entry El fichaje a comprobar.
     * @return {@code true} si la precisión del GPS es baja, {@code false} en caso contrario.
     */
    public static boolean hasLowGpsAccuracy(TimeEntry entry) {
        return entry.getFlags() != null && entry.getFlags().contains("LOW_GPS_ACCURACY");
    }

    /**
     * Comprueba si a un fichaje le faltan las coordenadas GPS de inicio.
     * @param entry El fichaje a comprobar.
     * @return {@code true} si faltan las coordenadas, {@code false} en caso contrario.
     */
    public static boolean isGpsMissing(TimeEntry entry) {
        return entry.getStartLat() == null;
    }

    /**
     * Comprueba si un fichaje ha sido modificado manualmente.
     * La comprobación se basa en la existencia de una razón de modificación.
     * @param entry El fichaje a comprobar.
     * @return {@code true} si el fichaje ha sido modificado, {@code false} en caso contrario.
     */
    public static boolean isModified(TimeEntry entry) {
        return entry.getModificationReason() != null && !entry.getModificationReason().isBlank();
    }

    /**
     * Determina una cadena de estado para un fichaje en un informe de Excel.
     * Este estado resume la integridad del fichaje (OK, SIN GPS, MODIFICADO, etc.) para una rápida identificación en la hoja de cálculo.
     * @param entry El fichaje a evaluar.
     * @param hasAuditChanges Indica si existen registros de auditoría externos para este fichaje.
     * @return La cadena de texto que representa el estado del fichaje.
     */
    public static String resolveExcelStatus(TimeEntry entry, boolean hasAuditChanges) {
        String status = "OK";
        if (hasLowGpsAccuracy(entry)) {
            status = "ALERTA GPS";
        }
        if (isGpsMissing(entry)) {
            status = "SIN GPS";
        }
        if (hasAuditChanges || isModified(entry)) {
            status = "MODIFICADO (Ver Hoja Auditoría)";
        }
        return status;
    }

    /**
     * Determina una cadena de estado para un fichaje en un informe PDF.
     * Similar a `resolveExcelStatus`, pero adaptado para el formato PDF.
     * @param entry El fichaje a evaluar.
     * @return La cadena de texto que representa el estado.
     */
    public static String resolvePdfStatus(TimeEntry entry) {
        if (isModified(entry)) {
            return "MODIFICADO";
        }
        if (isGpsMissing(entry)) {
            return "SIN GPS";
        }
        if (hasLowGpsAccuracy(entry)) {
            return "ALERTA GPS";
        }
        return "OK";
    }

    /**
     * Determina el color del texto del estado en un informe PDF.
     * Se utiliza para resaltar visualmente los estados que requieren atención (p. ej., modificaciones en naranja, alertas en rojo).
     * @param entry El fichaje a evaluar.
     * @return Un objeto `java.awt.Color` para colorear el texto.
     */
    public static java.awt.Color resolvePdfStatusColor(TimeEntry entry) {
        if (isModified(entry)) {
            return new java.awt.Color(255, 140, 0);
        }
        if (hasLowGpsAccuracy(entry)) {
            return new java.awt.Color(200, 0, 0);
        }
        return java.awt.Color.BLACK;
    }
}
