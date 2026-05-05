package com.worktrace.worktracebackend.service.archivos;


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

@Service
@RequiredArgsConstructor
public class ExcelGeneratorService {

    private final PdfHelperService pdfHelper;

    public byte[] generarExcelFichajesEmpresa(List<TimeEntry> fichajes, List<AuditRecordDto> auditoria, LocalDate inicio, LocalDate fin) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            DateTimeFormatter dtfShort = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            DateTimeFormatter auditDtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            StringBuilder dataToHash = new StringBuilder();

            Sheet sheetFichajes = workbook.createSheet("Registro_Horario");
            String[] cabecerasFichajes = {"ID Registro", "Fecha", "Empleado", "DNI/Código", "Hora Entrada", "Hora Salida", "Latitud", "Longitud", "Precisión (m)", "IP Origen", "ESTADO INTEGRIDAD"};
            crearCabecera(sheetFichajes, cabecerasFichajes, headerStyle);

            int rowIdx = 1;
            for (TimeEntry f : fichajes) {
                Row row = sheetFichajes.createRow(rowIdx++);

                String empleado = TimeEntryExportSupport.employeeName(f);
                String dni = TimeEntryExportSupport.employeeCode(f);
                String entrada = TimeEntryExportSupport.formatTime(f.getStartAt(), timeFormatter, "-");
                String salida = TimeEntryExportSupport.formatTime(f.getEndAt(), timeFormatter, "EN CURSO");

                boolean tieneCambios = auditoria.stream().anyMatch(log -> log.getReferenciaRegistro().equals(f.getId()));
                String estado = TimeEntryExportSupport.resolveExcelStatus(f, tieneCambios);

                row.createCell(0).setCellValue(f.getId() != null ? f.getId().toString() : "");
                row.createCell(1).setCellValue(f.getWorkDate() != null ? f.getWorkDate().format(dtfShort) : "");
                row.createCell(2).setCellValue(empleado);
                row.createCell(3).setCellValue(dni);
                row.createCell(4).setCellValue(entrada);
                row.createCell(5).setCellValue(salida);
                row.createCell(6).setCellValue(f.getStartLat() != null ? String.valueOf(f.getStartLat()) : "");
                row.createCell(7).setCellValue(f.getStartLng() != null ? String.valueOf(f.getStartLng()) : "");
                row.createCell(8).setCellValue(f.getStartAccuracyM() != null ? String.valueOf(f.getStartAccuracyM()) : "");
                row.createCell(9).setCellValue(f.getStartIp() != null ? f.getStartIp() : "");
                row.createCell(10).setCellValue(estado);

                dataToHash.append(f.getId()).append(entrada).append(salida).append(estado);
            }
            ajustarColumnas(sheetFichajes, cabecerasFichajes.length);


            if (!auditoria.isEmpty()) {
                Sheet sheetAudit = workbook.createSheet("Traza_Auditoria");
                String[] cabecerasAudit = {"Fecha Edición", "Autor (Rol)", "Justificación Legal", "Detalle Cambio", "ID Registro Afectado"};
                crearCabecera(sheetAudit, cabecerasAudit, headerStyle);

                int auditRowIdx = 1;
                for (AuditRecordDto aud : auditoria) {
                    Row row = sheetAudit.createRow(auditRowIdx++);
                    row.createCell(0).setCellValue(aud.getEditionDate() != null ? aud.getEditionDate().format(auditDtf) : "");
                    row.createCell(1).setCellValue(aud.getAuthor() != null ? aud.getAuthor() : "Admin");
                    row.createCell(2).setCellValue(aud.getJustification() != null ? aud.getJustification() : "Sin motivo");
                    row.createCell(3).setCellValue(aud.getChangePerformed() != null ? aud.getChangePerformed() : "Ajuste manual");
                    row.createCell(4).setCellValue(aud.getReferenciaRegistro() != null ? aud.getReferenciaRegistro().toString() : "");

                    dataToHash.append(aud.getReferenciaRegistro()).append(aud.getChangePerformed());
                }
                ajustarColumnas(sheetAudit, cabecerasAudit.length);
            }

            Sheet sheetCert = workbook.createSheet("Certificado_Integridad");
            crearCabecera(sheetCert, new String[]{"CONCEPTO", "VALOR"}, headerStyle);

            String hashDocumento = pdfHelper.generarHashSha256(dataToHash.toString());

            String rangoTxt = (inicio != null ? inicio.format(dtfShort) : "Inicio") + " - " + (fin != null ? fin.format(dtfShort) : "Actualidad");

            String[][] certificadoDatos = {
                    {"INFORME FORENSE EXPORTABLE (Art 34.9 ET)", ""},
                    {"Fecha Generación", OffsetDateTime.now().toString()},
                    {"Rango Auditado", rangoTxt},
                    {"FIRMA DIGITAL (SHA-256)", hashDocumento},
                    {"NOTA LEGAL", "Este archivo es una copia de trabajo. La integridad se garantiza mediante el Hash."}
            };

            for (int i = 0; i < certificadoDatos.length; i++) {
                Row row = sheetCert.createRow(i + 1);
                row.createCell(0).setCellValue(certificadoDatos[i][0]);
                row.createCell(1).setCellValue(certificadoDatos[i][1]);
            }
            ajustarColumnas(sheetCert, 2);

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel de auditoría", e);
        }
    }

    private void crearCabecera(Sheet sheet, String[] cabeceras, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < cabeceras.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(cabeceras[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void ajustarColumnas(Sheet sheet, int numColumnas) {
        for (int i = 0; i < numColumnas; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}