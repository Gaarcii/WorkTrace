package com.worktrace.worktracebackend.service.files;
import com.worktrace.worktracebackend.dto.auditTimeEntry.AuditRecordDto;
import com.worktrace.worktracebackend.model.TimeEntry;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio para generar informes de auditoría en formato Excel.
 * Su propósito es crear un libro de trabajo Excel con varias hojas que detallan
 * los registros horarios, la traza de auditoría de modificaciones y un certificado
 * de integridad digital, proporcionando un documento completo y fácilmente procesable
 * para inspecciones o análisis de datos.
 */
@Service
@RequiredArgsConstructor
public class ExcelGeneratorService {

    private final PdfHelperService pdfHelper;

    /**
     * Genera un informe de auditoría en formato Excel (XLSX) que contiene los registros horarios y su traza de auditoría.
     * El libro de trabajo resultante incluye tres hojas:
     * 1.  **Registro_Horario**: Detalla todos los fichajes del período, indicando su estado de integridad.
     * 2.  **Traza_Auditoria**: Registra todas las modificaciones manuales realizadas sobre los fichajes.
     * 3.  **Certificado_Integridad**: Contiene metadatos del informe y una firma digital (hash SHA-256)
     *     del contenido de las otras dos hojas para garantizar que los datos no han sido alterados.
     *
     * @param timeEntries La lista de registros de fichajes a incluir.
     * @param auditTrail La lista de registros de auditoría (modificaciones) a incluir.
     * @param startDate La fecha de inicio del período del informe.
     * @param endDate La fecha de fin del período del informe.
     * @return Un array de bytes (byte[]) que representa el archivo Excel generado.
     */
    public byte[] generateCompanyTimeEntriesExcel(List<TimeEntry> timeEntries, List<AuditRecordDto> auditTrail, LocalDate startDate, LocalDate endDate) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            DateTimeFormatter shortDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            DateTimeFormatter auditDateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            StringBuilder dataToHash = new StringBuilder();

            Sheet timeEntriesSheet = workbook.createSheet("Registro_Horario");
            String[] timeEntriesHeaders = {"ID Registro", "Fecha", "Empleado", "DNI/Código", "Hora Entrada", "Hora Salida", "Latitud", "Longitud", "Precisión (m)", "IP Origen", "ESTADO INTEGRIDAD"};
            createHeader(timeEntriesSheet, timeEntriesHeaders, headerStyle);

            int rowIdx = 1;
            for (TimeEntry timeEntry : timeEntries) {
                Row row = timeEntriesSheet.createRow(rowIdx++);

                String employeeName = TimeEntryExportSupport.employeeName(timeEntry);
                String employeeCode = TimeEntryExportSupport.employeeCode(timeEntry);
                String startTime = TimeEntryExportSupport.formatTime(timeEntry.getStartAt(), timeFormatter, "-");
                String endTime = TimeEntryExportSupport.formatTime(timeEntry.getEndAt(), timeFormatter, "EN CURSO");

                boolean hasChanges = auditTrail.stream().anyMatch(log -> log.getReferenciaRegistro().equals(timeEntry.getId()));
                String status = TimeEntryExportSupport.resolveExcelStatus(timeEntry, hasChanges);

                row.createCell(0).setCellValue(timeEntry.getId() != null ? timeEntry.getId().toString() : "");
                row.createCell(1).setCellValue(timeEntry.getWorkDate() != null ? timeEntry.getWorkDate().format(shortDateFormatter) : "");
                row.createCell(2).setCellValue(employeeName);
                row.createCell(3).setCellValue(employeeCode);
                row.createCell(4).setCellValue(startTime);
                row.createCell(5).setCellValue(endTime);
                row.createCell(6).setCellValue(timeEntry.getStartLat() != null ? String.valueOf(timeEntry.getStartLat()) : "");
                row.createCell(7).setCellValue(timeEntry.getStartLng() != null ? String.valueOf(timeEntry.getStartLng()) : "");
                row.createCell(8).setCellValue(timeEntry.getStartAccuracyM() != null ? String.valueOf(timeEntry.getStartAccuracyM()) : "");
                row.createCell(9).setCellValue(timeEntry.getStartIp() != null ? timeEntry.getStartIp() : "");
                row.createCell(10).setCellValue(status);

                dataToHash.append(timeEntry.getId()).append(startTime).append(endTime).append(status);
            }
            autoSizeColumns(timeEntriesSheet, timeEntriesHeaders.length);

            if (!auditTrail.isEmpty()) {
                Sheet auditTrailSheet = workbook.createSheet("Traza_Auditoria");
                String[] auditTrailHeaders = {"Fecha Edición", "Autor (Rol)", "Justificación Legal", "Detalle Cambio", "ID Registro Afectado"};
                createHeader(auditTrailSheet, auditTrailHeaders, headerStyle);

                int auditRowIdx = 1;
                for (AuditRecordDto auditRecord : auditTrail) {
                    Row row = auditTrailSheet.createRow(auditRowIdx++);
                    row.createCell(0).setCellValue(auditRecord.getEditionDate() != null ? auditRecord.getEditionDate().format(auditDateTimeFormatter) : "");
                    row.createCell(1).setCellValue(auditRecord.getAuthor() != null ? auditRecord.getAuthor() : "Admin");
                    row.createCell(2).setCellValue(auditRecord.getJustification() != null ? auditRecord.getJustification() : "Sin motivo");
                    row.createCell(3).setCellValue(auditRecord.getChangePerformed() != null ? auditRecord.getChangePerformed() : "Ajuste manual");
                    row.createCell(4).setCellValue(auditRecord.getReferenciaRegistro() != null ? auditRecord.getReferenciaRegistro().toString() : "");

                    dataToHash.append(auditRecord.getReferenciaRegistro()).append(auditRecord.getChangePerformed());
                }
                autoSizeColumns(auditTrailSheet, auditTrailHeaders.length);
            }

            Sheet integrityCertificateSheet = workbook.createSheet("Certificado_Integridad");
            createHeader(integrityCertificateSheet, new String[]{"CONCEPTO", "VALOR"}, headerStyle);

            String documentHash = pdfHelper.generateSha256Hash(dataToHash.toString());

            String dateRangeText = (startDate != null ? startDate.format(shortDateFormatter) : "Inicio") + " - " + (endDate != null ? endDate.format(shortDateFormatter) : "Actualidad");

            String[][] certificateData = {
                    {"INFORME FORENSE EXPORTABLE (Art 34.9 ET)", ""},
                    {"Fecha Generación", OffsetDateTime.now().toString()},
                    {"Rango Auditado", dateRangeText},
                    {"FIRMA DIGITAL (SHA-256)", documentHash},
                    {"NOTA LEGAL", "Este archivo es una copia de trabajo. La integridad se garantiza mediante el Hash."}
            };

            for (int i = 0; i < certificateData.length; i++) {
                Row row = integrityCertificateSheet.createRow(i + 1);
                row.createCell(0).setCellValue(certificateData[i][0]);
                row.createCell(1).setCellValue(certificateData[i][1]);
            }
            autoSizeColumns(integrityCertificateSheet, 2);

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel de auditoría", e);
        }
    }

    private void createHeader(Sheet sheet, String[] headers, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
