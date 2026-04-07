package com.worktrace.worktracebackend.service.archivos;

import com.worktrace.worktracebackend.dto.auditTimeEntries.AuditRecordDto;
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

    public byte[] generarPDFFichajesEmpresa(Company company, List<TimeEntry> fichajes, LocalDate inicio, LocalDate fin, List<AuditRecordDto> auditoria) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            StringBuilder dataString = new StringBuilder();
            for (TimeEntry r : fichajes) {
                String end = r.getEndAt() != null ? r.getEndAt().toString() : "EN CURSO";
                dataString.append(r.getId()).append("|")
                        .append(r.getEmployee().getUserId()).append("|")
                        .append(r.getStartAt()).append("|")
                        .append(end).append("||");
            }

            String hashSeguridad = pdfHelper.generarHashSha256(dataString.toString());
            String idDocumento = "DOC-" + System.currentTimeMillis() + "-" + hashSeguridad.substring(0, 8).toUpperCase();

            Document document = new Document(PageSize.A4, 30, 30, 40, 60);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            writer.setPageEvent(new PdfHelperService.StandardFooterEvent(hashSeguridad));
            document.open();

            pdfHelper.addCompanyHeader(document, company, "INFORME FORENSE DE REGISTRO HORARIO");

            Font fontRef = FontFactory.getFont(FontFactory.HELVETICA, 8, java.awt.Color.GRAY);
            Paragraph ref = new Paragraph("Ref: " + idDocumento, fontRef);
            ref.setSpacingBefore(5);
            document.add(ref);

            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 9, new java.awt.Color(80, 80, 80));
            Font fontPeriodo = FontFactory.getFont(FontFactory.HELVETICA, 8, new java.awt.Color(100, 100, 100));

            Paragraph subtitulo = new Paragraph("Conforme al Art. 34.9 ET - Garantía de Integridad", fontSubtitulo);
            subtitulo.setSpacingBefore(5);
            document.add(subtitulo);

            DateTimeFormatter dtfShort = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String txtInicio = inicio != null ? inicio.format(dtfShort) : "Inicio";
            String txtFin = fin != null ? fin.format(dtfShort) : "Actualidad";

            Paragraph periodo = new Paragraph("Periodo: " + txtInicio + " al " + txtFin, fontPeriodo);
            periodo.setSpacingAfter(10);
            document.add(periodo);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.8f, 3.5f, 2.2f, 1.8f, 1.8f, 2.8f, 2.2f, 2.2f});

            String[] cabeceras = {"Fecha", "Empleado", "DNI", "Entrada", "Salida", "GPS", "IP", "Integridad"};
            Font fontCabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, java.awt.Color.WHITE);
            addHeaderCells(table, cabeceras, fontCabecera, new java.awt.Color(0, 0, 38));

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 7, java.awt.Color.BLACK);

            for (TimeEntry f : fichajes) {
                String strFecha = f.getWorkDate() != null ? f.getWorkDate().format(dtfShort) : "-";
                String empleado = TimeEntryExportSupport.employeeName(f);
                String dni = TimeEntryExportSupport.employeeCode(f);
                String strEntrada = TimeEntryExportSupport.formatTime(f.getStartAt(), timeFormatter, "-");
                String strSalida = TimeEntryExportSupport.formatTime(f.getEndAt(), timeFormatter, "EN CURSO");

                String gps = (f.getStartLat() != null && f.getStartLng() != null)
                        ? String.format("%.4f, %.4f", f.getStartLat(), f.getStartLng()) : "N/A";
                String ip = f.getStartIp() != null ? f.getStartIp() : "?";

                String estadoStr = TimeEntryExportSupport.resolvePdfStatus(f);
                java.awt.Color colorEstado = TimeEntryExportSupport.resolvePdfStatusColor(f);

                Font fontEstado = FontFactory.getFont(FontFactory.HELVETICA, 7, colorEstado);

                PdfPCell[] row = new PdfPCell[]{
                        new PdfPCell(new Phrase(strFecha, fontNormal)),
                        new PdfPCell(new Phrase(empleado, fontNormal)),
                        new PdfPCell(new Phrase(dni, fontNormal)),
                        new PdfPCell(new Phrase(strEntrada, fontNormal)),
                        new PdfPCell(new Phrase(strSalida, fontNormal)),
                        new PdfPCell(new Phrase(gps, fontNormal)),
                        new PdfPCell(new Phrase(ip, fontNormal)),
                        new PdfPCell(new Phrase(estadoStr, fontEstado))
                };

                boolean isEven = table.getRows().size() % 2 == 0;
                addRowCells(table, row, isEven ? new java.awt.Color(245, 245, 245) : null);
            }
            document.add(table);

            if (auditoria != null && !auditoria.isEmpty()) {
                document.add(new Paragraph(" "));

                Font fontTituloAnexo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new java.awt.Color(200, 0, 0));
                Paragraph tituloAnexo = new Paragraph("ANEXO I: TRAZA DE AUDITORÍA (MODIFICACIONES Y CORRECCIONES)", fontTituloAnexo);
                tituloAnexo.setSpacingAfter(5);
                document.add(tituloAnexo);

                Font fontDescAnexo = FontFactory.getFont(FontFactory.HELVETICA, 8, new java.awt.Color(80, 80, 80));
                Paragraph descAnexo = new Paragraph("Registro detallado de intervenciones manuales según normativa de inalterabilidad.", fontDescAnexo);
                descAnexo.setSpacingAfter(10);
                document.add(descAnexo);

                PdfPTable tableAudit = new PdfPTable(5);
                tableAudit.setWidthPercentage(100);
                tableAudit.setWidths(new float[]{2, 1.5f, 4, 3, 3});

                String[] cabecerasAudit = {"Fecha Edición", "Autor", "Justificación Legal (Motivo)", "Cambio Realizado", "Ref. Registro"};
                Font fontCabeceraAudit = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6, java.awt.Color.WHITE);
                addHeaderCells(tableAudit, cabecerasAudit, fontCabeceraAudit, new java.awt.Color(200, 50, 50));

                DateTimeFormatter auditDtf = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
                Font fontAuditBody = FontFactory.getFont(FontFactory.HELVETICA, 6, new java.awt.Color(50, 50, 50));

                for (AuditRecordDto aud : auditoria) {
                    PdfPCell[] auditRow = new PdfPCell[]{
                            new PdfPCell(new Phrase(aud.getFechaEdicion() != null ? aud.getFechaEdicion().format(auditDtf) : "-", fontAuditBody)),
                            new PdfPCell(new Phrase(aud.getAutor(), fontAuditBody)),
                            new PdfPCell(new Phrase(aud.getJustificacion(), fontAuditBody)),
                            new PdfPCell(new Phrase(aud.getCambioRealizado(), fontAuditBody)),
                            new PdfPCell(new Phrase(aud.getReferenciaRegistro() != null ? aud.getReferenciaRegistro().toString() : "-", fontAuditBody))
                    };
                    addRowCells(tableAudit, auditRow, null);
                }
                document.add(tableAudit);
            }

            document.add(new Paragraph(" "));
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, java.awt.Color.BLACK);
            document.add(new Paragraph("CERTIFICADO DE INTEGRIDAD DIGITAL:", fontBold));

            Font fontMono = FontFactory.getFont(FontFactory.COURIER, 7, java.awt.Color.BLACK);
            document.add(new Paragraph("SHA-256: " + hashSeguridad, fontMono));

            Font fontItalic = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, new java.awt.Color(100, 100, 100));
            document.add(new Paragraph("Este documento incluye una firma criptográfica. Cualquier alteración invalidará el hash.", fontItalic));

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