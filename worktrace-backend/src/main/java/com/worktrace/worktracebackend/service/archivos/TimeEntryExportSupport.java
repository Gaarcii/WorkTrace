package com.worktrace.worktracebackend.service.archivos;

import com.worktrace.worktracebackend.model.TimeEntry;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public final class TimeEntryExportSupport {

    private TimeEntryExportSupport() {
    }

    public static String employeeName(TimeEntry entry) {
        return entry.getEmployee() != null ? entry.getEmployee().getFullName() : "?";
    }

    public static String employeeCode(TimeEntry entry) {
        return entry.getEmployee() != null && entry.getEmployee().getEmployeeCode() != null
                ? entry.getEmployee().getEmployeeCode()
                : "-";
    }

    public static String formatTime(TemporalAccessor time, DateTimeFormatter formatter, String defaultValue) {
        return time != null ? formatter.format(time) : defaultValue;
    }

    public static boolean hasLowGpsAccuracy(TimeEntry entry) {
        return entry.getFlags() != null && entry.getFlags().contains("LOW_GPS_ACCURACY");
    }

    public static boolean isGpsMissing(TimeEntry entry) {
        return entry.getStartLat() == null;
    }

    public static boolean isModified(TimeEntry entry) {
        return entry.getModificationReason() != null && !entry.getModificationReason().isBlank();
    }

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


