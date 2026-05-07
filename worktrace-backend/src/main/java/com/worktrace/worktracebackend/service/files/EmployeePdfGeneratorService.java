package com.worktrace.worktracebackend.service.files;

import com.worktrace.worktracebackend.model.Profile;
import com.worktrace.worktracebackend.model.TimeEntry;
import com.worktrace.worktracebackend.model.User;
import lombok.RequiredArgsConstructor;
import org.openpdf.text.*;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeePdfGeneratorService {

    private final PdfHelperService pdfHelper;

    public byte[] generateTimeEntriesPdf(Profile profile, User user, List<TimeEntry> timeEntries, LocalDate startDate, LocalDate endDate) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            StringBuilder dataString = new StringBuilder();
            for (TimeEntry timeEntry : timeEntries) {
                String end = timeEntry.getEndAt() != null ? timeEntry.getEndAt().toString() : "OPEN";
                dataString.append(timeEntry.getId()).append("|")
                        .append(timeEntry.getStartAt()).append("|")
                        .append(end).append("|")
                        .append(user.getId()).append("||");
            }

            String securityHash = pdfHelper.generateSha256Hash(dataString.toString());
            String documentId = "DOC-" + System.currentTimeMillis() + "-" + securityHash.substring(0, 8).toUpperCase();

            Document document = new Document(PageSize.A4, 40, 40, 40, 80);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            writer.setPageEvent(new PdfHelperService.StandardFooterEvent(securityHash));

            document.open();

            pdfHelper.addCompanyHeader(document, user.getCompany(), "Informe de Registro Horario");

            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph ref = new Paragraph("Ref: " + documentId, FontFactory.getFont(FontFactory.HELVETICA, 8, java.awt.Color.GRAY));
            ref.setSpacingBefore(10);
            document.add(ref);

            document.add(new Paragraph("Empleado: " + profile.getFullName(), normalFont));

            String code = user.getProfile() != null && user.getProfile().getEmployeeCode() != null ? user.getProfile().getEmployeeCode() : "N/A";
            document.add(new Paragraph("DNI: " + code, normalFont));

            DateTimeFormatter shortDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String startDateText = startDate != null ? startDate.format(shortDateFormatter) : "Inicio";
            String endDateText = endDate != null ? endDate.format(shortDateFormatter) : "Actualidad";
            document.add(new Paragraph("Periodo: " + startDateText + " - " + endDateText, normalFont));

            document.add(new Paragraph(" "));

            // 5. TABLA DE FICHAJES
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 2, 2, 2, 2});

            String[] headers = {"Fecha", "Entrada", "Salida", "Total", "Estado"};
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.WHITE);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new java.awt.Color(0, 0, 38));
                cell.setPadding(6);
                table.addCell(cell);
            }

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            for (TimeEntry timeEntry : timeEntries) {
                String dateStr = timeEntry.getWorkDate() != null ? timeEntry.getWorkDate().format(shortDateFormatter) : "-";
                String startTimeStr = timeEntry.getStartAt() != null ? timeEntry.getStartAt().format(timeFormatter) : "-";
                String endTimeStr = timeEntry.getEndAt() != null ? timeEntry.getEndAt().format(timeFormatter) : "En curso";

                String totalStr = "-";
                if (timeEntry.getStartAt() != null && timeEntry.getEndAt() != null) {
                    Duration duration = Duration.between(timeEntry.getStartAt(), timeEntry.getEndAt());
                    totalStr = String.format("%dh %dm", duration.toHours(), duration.toMinutesPart());
                }

                String statusStr = "CLOSED".equalsIgnoreCase(timeEntry.getTimeEntryStatus().name()) ? "Cerrado" : "Abierto";

                PdfPCell[] row = new PdfPCell[]{
                        new PdfPCell(new Phrase(dateStr, normalFont)),
                        new PdfPCell(new Phrase(startTimeStr, normalFont)),
                        new PdfPCell(new Phrase(endTimeStr, normalFont)),
                        new PdfPCell(new Phrase(totalStr, normalFont)),
                        new PdfPCell(new Phrase(statusStr, normalFont))
                };

                boolean isEven = table.getRows().size() % 2 == 0;
                for (PdfPCell cell : row) {
                    cell.setPadding(5);
                    if (isEven) cell.setBackgroundColor(new java.awt.Color(245, 245, 245));
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF de fichajes", e);
        }

        return baos.toByteArray();
    }
}