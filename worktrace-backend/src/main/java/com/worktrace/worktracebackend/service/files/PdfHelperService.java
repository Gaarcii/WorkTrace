package com.worktrace.worktracebackend.service.files;

import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.service.hash.HashService;
import lombok.RequiredArgsConstructor;
import org.openpdf.text.*;
import org.openpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Servicio de utilidad para la generación de documentos PDF.
 * Su propósito es centralizar y reutilizar la lógica común para la creación de PDFs,
 * como la generación de cabeceras y pies de página estandarizados, y el cálculo de hashes
 * de seguridad, garantizando así la consistencia y la integridad en todos los informes generados.
 */
@Service
@RequiredArgsConstructor
public class PdfHelperService {

    private final HashService hashService;

    /**
     * Genera un hash criptográfico SHA-256 a partir de una cadena de datos.
     * Este método se utiliza para crear una "huella digital" única del contenido de un informe,
     * permitiendo verificar posteriormente que los datos no han sido alterados.
     *
     * @param rawData La cadena de datos brutos que se va a hashear.
     * @return La representación hexadecimal del hash SHA-256.
     */
    public String generateSha256Hash(String rawData) {
        return hashService.sha256Hex(rawData);
    }

    /**
     * Añade una cabecera estandarizada a un documento PDF.
     * La cabecera incluye elementos de branding como el logo de la empresa, el nombre y el CIF,
     * así como el título del informe y la fecha de emisión, proporcionando un formato profesional y consistente.
     *
     * @param document    El documento PDF al que se añadirá la cabecera.
     * @param company     La entidad Company para obtener los datos de la empresa.
     * @param reportTitle El título específico del informe que se está generando.
     * @throws DocumentException Si ocurre un error al añadir elementos al documento.
     */
    public void addCompanyHeader(Document document, Company company, String reportTitle) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 2});

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        if (company != null && company.getLogoUrl() != null && !company.getLogoUrl().isBlank()) {
            try {
                Image logo = Image.getInstance(java.net.URI.create(company.getLogoUrl()).toURL());
                logo.scaleToFit(85, 85);
                logoCell.addElement(logo);
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo cargar el logo desde: " + company.getLogoUrl());
                logoCell.addElement(new Phrase(" "));
            }
        } else {
            logoCell.addElement(new Phrase(" "));
        }
        headerTable.addCell(logoCell);

        PdfPCell companyCell = new PdfPCell();
        companyCell.setBorder(Rectangle.NO_BORDER);
        companyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Font fontEmpresa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new java.awt.Color(0, 0, 38));
        Font fontGris = FontFactory.getFont(FontFactory.HELVETICA, 10, java.awt.Color.GRAY);

        Paragraph pName = new Paragraph(company != null ? company.getCompanyName() : "Mi Empresa", fontEmpresa);
        pName.setAlignment(Element.ALIGN_RIGHT);
        companyCell.addElement(pName);

        if (company != null && company.getCif() != null) {
            Paragraph pCif = new Paragraph("CIF: " + company.getCif(), fontGris);
            pCif.setAlignment(Element.ALIGN_RIGHT);
            companyCell.addElement(pCif);
        }

        DateTimeFormatter dtfText = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy");
        Paragraph pFecha = new Paragraph("Emitido: " + LocalDate.now().format(dtfText), fontGris);
        pFecha.setAlignment(Element.ALIGN_RIGHT);
        companyCell.addElement(pFecha);

        headerTable.addCell(companyCell);
        document.add(headerTable);

        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new java.awt.Color(0, 0, 38));
        Paragraph titulo = new Paragraph(reportTitle, fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingBefore(15);
        titulo.setSpacingAfter(10);
        document.add(titulo);

        document.add(new org.openpdf.text.pdf.draw.LineSeparator(0.5f, 100, java.awt.Color.LIGHT_GRAY, Element.ALIGN_CENTER, -5));
    }

    /**
     * Clase interna que gestiona la creación de un pie de página estándar en cada página del PDF.
     * Se utiliza para mostrar información crucial en la parte inferior de cada página, como el número de página,
     * notas legales y, lo más importante, la huella digital (hash) del documento para garantizar su integridad.
     */
    public static class StandardFooterEvent extends PdfPageEventHelper {
        private final String hashSeguridad;

        public StandardFooterEvent(String hashSeguridad) {
            this.hashSeguridad = hashSeguridad;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            float x = document.left();
            float y = document.bottom() - 10;

            // Línea separadora
            cb.setLineWidth(0.3f);
            cb.setColorStroke(java.awt.Color.LIGHT_GRAY);
            cb.moveTo(x, y);
            cb.lineTo(document.right(), y);
            cb.stroke();

            try {
                y -= 15;
                ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                        new Phrase("Documento generado automáticamente. Validez sujeta a verificación.", FontFactory.getFont(FontFactory.HELVETICA, 8, java.awt.Color.GRAY)),
                        x, y, 0);

                if (hashSeguridad != null && !hashSeguridad.isEmpty()) {
                    y -= 10;
                    ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                            new Phrase("Huella Digital (SHA-256) de integridad de datos:", FontFactory.getFont(FontFactory.HELVETICA, 8, java.awt.Color.GRAY)),
                            x, y, 0);

                    y -= 10;
                    ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                            new Phrase(hashSeguridad, FontFactory.getFont(FontFactory.COURIER, 7, java.awt.Color.BLACK)),
                            x, y, 0);
                }

                String pageText = "Página " + writer.getPageNumber();
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                        new Phrase(pageText, FontFactory.getFont(FontFactory.HELVETICA, 9, java.awt.Color.GRAY)),
                        document.right(), y + 20, 0);

            } catch (Exception ignored) {
            }
        }
    }
}
