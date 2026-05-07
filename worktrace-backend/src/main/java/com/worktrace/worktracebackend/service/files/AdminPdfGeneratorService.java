package com.worktrace.worktracebackend.service.files;

import com.worktrace.worktracebackend.dto.auditTimeEntry.AuditRecordDto;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.TimeEntry;
import lombok.RequiredArgsConstructor;
import org.openpdf.text.*;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPdfGeneratorService {

    private final PdfHelperService pdfHelper;

    public byte[] generateCompanyTimeEntriesPdf(Company company, List<TimeEntry> timeEntries, LocalDate startDate, LocalDate endDate, List<AuditRecordDto> auditTrail) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            StringBuilder dataString = new StringBuilder();
            for (TimeEntry timeEntry : timeEntries) {
                String end = timeEntry.getEndAt() != null ? timeEntry.getEndAt().toString() : "EN CURSO";
                dataString.append(timeEntry.getId()).append("|")
                        .append(timeEntry.getEmployee().getUserId()).append("|")
                        .append(timeEntry.getStartAt()).append("|")
                        .append(end).append("||");
            }

            String securityHash = pdfHelper.generateSha256Hash(dataString.toString());
            String documentId = "DOC-" + System.currentTimeMillis() + "-" + securityHash.substring(0, 8).toUpperCase();

            Document document = new Document(PageSize.A4, 30, 30, 40, 60);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            writer.setPageEvent(new PdfHelperService.StandardFooterEvent(securityHash));
            document.open();

            pdfHelper.addCompanyHeader(document, company, "INFORME FORENSE DE REGISTRO HORARIO");

            Font referenceFont = FontFactory.getFont(FontFactory.HELVETICA, 8, java.awt.Color.GRAY);
            Paragraph referenceParagraph = new Paragraph("Ref: " + documentId, referenceFont);
            referenceParagraph.setSpacingBefore(5);
            document.add(referenceParagraph);

            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new java.awt.Color(80, 80, 80));
            Font periodFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new java.awt.Color(100, 100, 100));

            Paragraph subtitle = new Paragraph("Conforme al Art. 34.9 ET - Garantía de Integridad", subtitleFont);
            subtitle.setSpacingBefore(5);
            document.add(subtitle);

            DateTimeFormatter shortDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String startDateText = startDate != null ? startDate.format(shortDateFormatter) : "Inicio";
            String endDateText = endDate != null ? endDate.format(shortDateFormatter) : "Actualidad";

            Paragraph periodParagraph = new Paragraph("Periodo: " + startDateText + " al " + endDateText, periodFont);
            periodParagraph.setSpacingAfter(10);
            document.add(periodParagraph);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.8f, 3.5f, 2.2f, 1.8f, 1.8f, 2.8f, 2.2f, 2.2f});

            String[] headers = {"Fecha", "Empleado", "DNI", "Entrada", "Salida", "GPS", "IP", "Integridad"};
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, java.awt.Color.WHITE);
            addHeaderCells(table, headers, headerFont, new java.awt.Color(0, 0, 38));

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 7, java.awt.Color.BLACK);

            for (TimeEntry timeEntry : timeEntries) {
                String dateStr = timeEntry.getWorkDate() != null ? timeEntry.getWorkDate().format(shortDateFormatter) : "-";
                String employeeName = TimeEntryExportSupport.employeeName(timeEntry);
                String employeeId = TimeEntryExportSupport.employeeCode(timeEntry);
                String startTimeStr = TimeEntryExportSupport.formatTime(timeEntry.getStartAt(), timeFormatter, "-");
                String endTimeStr = TimeEntryExportSupport.formatTime(timeEntry.getEndAt(), timeFormatter, "EN CURSO");

                String gps = (timeEntry.getStartLat() != null && timeEntry.getStartLng() != null)
                        ? String.format("%.4f, %.4f", timeEntry.getStartLat(), timeEntry.getStartLng()) : "N/A";
                String ip = timeEntry.getStartIp() != null ? timeEntry.getStartIp() : "?";

                String statusStr = TimeEntryExportSupport.resolvePdfStatus(timeEntry);
                java.awt.Color statusColor = TimeEntryExportSupport.resolvePdfStatusColor(timeEntry);

                Font statusFont = FontFactory.getFont(FontFactory.HELVETICA, 7, statusColor);

                PdfPCell[] row = new PdfPCell[]{
                        new PdfPCell(new Phrase(dateStr, normalFont)),
                        new PdfPCell(new Phrase(employeeName, normalFont)),
                        new PdfPCell(new Phrase(employeeId, normalFont)),
                        new PdfPCell(new Phrase(startTimeStr, normalFont)),
                        new PdfPCell(new Phrase(endTimeStr, normalFont)),
                        new PdfPCell(new Phrase(gps, normalFont)),
                        new PdfPCell(new Phrase(ip, normalFont)),
                        new PdfPCell(new Phrase(statusStr, statusFont))
                };

                boolean isEven = table.getRows().size() % 2 == 0;
                addRowCells(table, row, isEven ? new java.awt.Color(245, 245, 245) : null);
            }
            document.add(table);

            if (auditTrail != null && !auditTrail.isEmpty()) {
                document.add(new Paragraph(" "));

                Font annexTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new java.awt.Color(200, 0, 0));
                Paragraph annexTitle = new Paragraph("ANEXO I: TRAZA DE AUDITORÍA (MODIFICACIONES Y CORRECCIONES)", annexTitleFont);
                annexTitle.setSpacingAfter(5);
                document.add(annexTitle);

                Font annexDescriptionFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new java.awt.Color(80, 80, 80));
                Paragraph annexDescription = new Paragraph("Registro detallado de intervenciones manuales según normativa de inalterabilidad.", annexDescriptionFont);
                annexDescription.setSpacingAfter(10);
                document.add(annexDescription);

                PdfPTable auditTable = new PdfPTable(5);
                auditTable.setWidthPercentage(100);
                auditTable.setWidths(new float[]{2, 1.5f, 4, 3, 3});

                String[] auditHeaders = {"Fecha Edición", "Autor", "Justificación Legal (Motivo)", "Cambio Realizado", "Ref. Registro"};
                Font auditHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6, java.awt.Color.WHITE);
                addHeaderCells(auditTable, auditHeaders, auditHeaderFont, new java.awt.Color(200, 50, 50));

                DateTimeFormatter auditDateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
                Font auditBodyFont = FontFactory.getFont(FontFactory.HELVETICA, 6, new java.awt.Color(50, 50, 50));

                for (AuditRecordDto auditRecord : auditTrail) {
                    PdfPCell[] auditRow = new PdfPCell[]{
                            new PdfPCell(new Phrase(auditRecord.getEditionDate() != null ? auditRecord.getEditionDate().format(auditDateTimeFormatter) : "-", auditBodyFont)),
                            new PdfPCell(new Phrase(auditRecord.getAuthor(), auditBodyFont)),
                            new PdfPCell(new Phrase(auditRecord.getJustification(), auditBodyFont)),
                            new PdfPCell(new Phrase(auditRecord.getChangePerformed(), auditBodyFont)),
                            new PdfPCell(new Phrase(auditRecord.getReferenciaRegistro() != null ? auditRecord.getReferenciaRegistro().toString() : "-", auditBodyFont))
                    };
                    addRowCells(auditTable, auditRow, null);
                }
                document.add(auditTable);
            }

            document.add(new Paragraph(" "));
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, java.awt.Color.BLACK);
            document.add(new Paragraph("CERTIFICADO DE INTEGRIDAD DIGITAL:", boldFont));

            Font monoFont = FontFactory.getFont(FontFactory.COURIER, 7, java.awt.Color.BLACK);
            document.add(new Paragraph("SHA-256: " + securityHash, monoFont));

            Font italicFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, new java.awt.Color(100, 100, 100));
            document.add(new Paragraph("Este documento incluye una firma criptográfica. Cualquier alteración invalidará el hash.", italicFont));

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF Forense de la empresa", e);
        }

        return baos.toByteArray();
    }

    private void addHeaderCells(PdfPTable table, String[] headers, Font font, java.awt.Color backgroundColor) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(backgroundColor);
            cell.setPadding(4);
            table.addCell(cell);
        }
    }

    private void addRowCells(PdfPTable table, PdfPCell[] cells, java.awt.Color backgroundColor) {
        for (PdfPCell cell : cells) {
            cell.setPadding(4);
            if (backgroundColor != null) {
                cell.setBackgroundColor(backgroundColor);
            }
            table.addCell(cell);
        }
    }
}