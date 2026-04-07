package com.worktrace.worktracebackend.service.archivos;

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

    public byte[] generarPDFFichajes(Profile profile, User user, List<TimeEntry> fichajes, LocalDate inicio, LocalDate fin) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            StringBuilder dataString = new StringBuilder();
            for (TimeEntry r : fichajes) {
                String end = r.getEndAt() != null ? r.getEndAt().toString() : "OPEN";
                dataString.append(r.getId()).append("|")
                        .append(r.getStartAt()).append("|")
                        .append(end).append("|")
                        .append(user.getId()).append("||");
            }

            String hashSeguridad = pdfHelper.generarHashSha256(dataString.toString());
            String idDocumento = "DOC-" + System.currentTimeMillis() + "-" + hashSeguridad.substring(0, 8).toUpperCase();

            Document document = new Document(PageSize.A4, 40, 40, 40, 80);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            writer.setPageEvent(new PdfHelperService.StandardFooterEvent(hashSeguridad));

            document.open();

            pdfHelper.addCompanyHeader(document, user.getCompany(), "Informe de Registro Horario");

            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph ref = new Paragraph("Ref: " + idDocumento, FontFactory.getFont(FontFactory.HELVETICA, 8, java.awt.Color.GRAY));
            ref.setSpacingBefore(10);
            document.add(ref);

            document.add(new Paragraph("Empleado: " + profile.getFullName(), fontNormal));

            String code = user.getProfile() != null && user.getProfile().getEmployeeCode() != null ? user.getProfile().getEmployeeCode() : "N/A";
            document.add(new Paragraph("DNI: " + code, fontNormal));

            DateTimeFormatter dtfShort = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String txtInicio = inicio != null ? inicio.format(dtfShort) : "Inicio";
            String txtFin = fin != null ? fin.format(dtfShort) : "Actualidad";
            document.add(new Paragraph("Periodo: " + txtInicio + " - " + txtFin, fontNormal));

            document.add(new Paragraph(" "));

            // 5. TABLA DE FICHAJES
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 2, 2, 2, 2});

            String[] cabeceras = {"Fecha", "Entrada", "Salida", "Total", "Estado"};
            Font fontCabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.WHITE);
            for (String cab : cabeceras) {
                PdfPCell cell = new PdfPCell(new Phrase(cab, fontCabecera));
                cell.setBackgroundColor(new java.awt.Color(0, 0, 38));
                cell.setPadding(6);
                table.addCell(cell);
            }

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            for (TimeEntry f : fichajes) {
                String strFecha = f.getWorkDate() != null ? f.getWorkDate().format(dtfShort) : "-";
                String strEntrada = f.getStartAt() != null ? f.getStartAt().format(timeFormatter) : "-";
                String strSalida = f.getEndAt() != null ? f.getEndAt().format(timeFormatter) : "En curso";

                String strTotal = "-";
                if (f.getStartAt() != null && f.getEndAt() != null) {
                    Duration dur = Duration.between(f.getStartAt(), f.getEndAt());
                    strTotal = String.format("%dh %dm", dur.toHours(), dur.toMinutesPart());
                }

                String strEstado = "CLOSED".equalsIgnoreCase(f.getEstadoFichaje().name()) ? "Cerrado" : "Abierto";

                PdfPCell[] row = new PdfPCell[]{
                        new PdfPCell(new Phrase(strFecha, fontNormal)),
                        new PdfPCell(new Phrase(strEntrada, fontNormal)),
                        new PdfPCell(new Phrase(strSalida, fontNormal)),
                        new PdfPCell(new Phrase(strTotal, fontNormal)),
                        new PdfPCell(new Phrase(strEstado, fontNormal))
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