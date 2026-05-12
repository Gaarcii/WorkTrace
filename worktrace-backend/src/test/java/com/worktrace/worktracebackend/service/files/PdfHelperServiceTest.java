package com.worktrace.worktracebackend.service.files;

import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.service.hash.HashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfHelperServiceTest {

    @Mock
    private HashService hashService;

    @InjectMocks
    private PdfHelperService pdfHelperService;

    private Company testCompany;
    private final String reportTitle = "Título del Informe de Prueba";

    @BeforeEach
    void setUp() {
        testCompany = new Company();
        testCompany.setCompanyName("Mi Empresa de Prueba, S.L.");
        testCompany.setCif("B12345678");
        testCompany.setLogoUrl("https://www.example.com/logo.png");
    }

    @Test
    void testGenerateSha256HashSuccess() {
        String rawData = "datos para hashear";
        String expectedHash = "hash-generado-de-prueba";
        when(hashService.sha256Hex(rawData)).thenReturn(expectedHash);

        String actualHash = pdfHelperService.generateSha256Hash(rawData);

        assertEquals(expectedHash, actualHash, "El hash devuelto debe coincidir con el esperado del servicio mockeado");
        verify(hashService, times(1)).sha256Hex(rawData);
    }

    @Test
    void testAddCompanyHeaderSuccess() {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
        } catch (DocumentException e) {
            fail("La configuración del PdfWriter no debería fallar en el test");
        }
        document.open();

        assertDoesNotThrow(() -> pdfHelperService.addCompanyHeader(document, testCompany, reportTitle),
                "La adición de la cabecera no debería lanzar una excepción con datos válidos");

        assertTrue(document.isOpen(), "El documento debería permanecer abierto después de añadir la cabecera");
    }

    @Test
    void testAddCompanyHeaderHandlesNullCompany() {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
        } catch (DocumentException e) {
            fail("La configuración del PdfWriter no debería fallar en el test");
        }
        document.open();

        assertDoesNotThrow(() -> pdfHelperService.addCompanyHeader(document, null, reportTitle),
                "La adición de la cabecera no debería lanzar una excepción si la compañía es nula");
    }

    @Test
    void testStandardFooterEventOnEndPage() {
        String securityHash = "hash-de-seguridad-para-el-pie-de-pagina";
        PdfHelperService.StandardFooterEvent footerEvent = new PdfHelperService.StandardFooterEvent(securityHash);

        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = null;
        try {
            writer = PdfWriter.getInstance(document, baos);
        } catch (DocumentException e) {
            fail("La configuración del PdfWriter no debería fallar en el test");
        }
        document.open();

        PdfWriter finalWriter = writer;
        assertDoesNotThrow(() -> footerEvent.onEndPage(finalWriter, document),
                "El evento onEndPage no debería lanzar una excepción con datos válidos");
    }
}
