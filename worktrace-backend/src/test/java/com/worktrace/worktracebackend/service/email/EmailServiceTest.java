package com.worktrace.worktracebackend.service.email;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "senderEmail", "no-reply@worktrace.com");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void testSendNewEmployeeWelcomeEmail() throws Exception {
        String toEmail = "nuevo.empleado@test.com";
        String employeeName = "Juan Pérez";
        String plainPassword = "password123";
        String companyLogoUrl = "http://example.com/logo.png";
        String companyName = "Mi Empresa";
        String adminName = "Admin Carlos";
        String appAccessUrl = "http://app.worktrace.com";
        String expectedHtml = "<html><body>Contenido de bienvenida</body></html>";

        when(templateEngine.process(eq("email-bienvenida"), any(Context.class))).thenReturn(expectedHtml);

        emailService.sendNewEmployeeWelcomeEmail(toEmail, employeeName, plainPassword, companyLogoUrl, companyName, adminName, appAccessUrl);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email-bienvenida"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();

        assertAll("Verifica el contenido del contexto para el email de bienvenida",
                () -> assertEquals(employeeName, capturedContext.getVariable("nombre")),
                () -> assertEquals(toEmail, capturedContext.getVariable("email")),
                () -> assertEquals(plainPassword, capturedContext.getVariable("password")),
                () -> assertEquals(companyLogoUrl, capturedContext.getVariable("empresaLogoUrl")),
                () -> assertEquals(companyName, capturedContext.getVariable("empresaNombre")),
                () -> assertEquals(adminName, capturedContext.getVariable("nombreAdmin")),
                () -> assertEquals(appAccessUrl, capturedContext.getVariable("urlAccesoApp"))
        );

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendNewInspectorWelcomeEmail() throws Exception {
        String toEmail = "nuevo.inspector@test.com";
        String inspectorName = "Inspector Gadget";
        String plainPassword = "password456";
        String companyLogoUrl = "http://example.com/logo-inspector.png";
        String companyName = "Agencia de Inspección";
        String adminName = "Admin General";
        String appAccessUrl = "http://app.worktrace.com/audit";
        String expectedHtml = "<html><body>Contenido de bienvenida para inspector</body></html>";

        when(templateEngine.process(eq("email-bienvenida-inspector"), any(Context.class))).thenReturn(expectedHtml);

        emailService.sendNewInspectorWelcomeEmail(toEmail, inspectorName, plainPassword, companyLogoUrl, companyName, adminName, appAccessUrl);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email-bienvenida-inspector"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();

        assertAll("Verifica el contenido del contexto para el email de bienvenida del inspector",
                () -> assertEquals(inspectorName, capturedContext.getVariable("nombre")),
                () -> assertEquals(toEmail, capturedContext.getVariable("email")),
                () -> assertEquals(plainPassword, capturedContext.getVariable("password")),
                () -> assertEquals(companyLogoUrl, capturedContext.getVariable("empresaLogoUrl")),
                () -> assertEquals(companyName, capturedContext.getVariable("empresaNombre")),
                () -> assertEquals(adminName, capturedContext.getVariable("nombreAdmin")),
                () -> assertEquals(appAccessUrl, capturedContext.getVariable("urlAccesoApp"))
        );

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendPasswordResetEmail() throws Exception {
        String toEmail = "usuario@test.com";
        String resetLink = "http://app.worktrace.com/reset?token=xyz";
        String companyName = "Empresa Registrada";
        String companyLogoUrl = "http://example.com/logo-empresa.png";
        String expectedHtml = "<html><body>Contenido de reseteo de contraseña</body></html>";

        when(templateEngine.process(eq("email-reset-password"), any(Context.class))).thenReturn(expectedHtml);

        emailService.sendPasswordResetEmail(toEmail, resetLink, companyName, companyLogoUrl);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email-reset-password"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();

        assertAll("Verifica el contenido del contexto para el email de reseteo de contraseña",
                () -> assertEquals(resetLink, capturedContext.getVariable("resetLink")),
                () -> assertEquals(companyName, capturedContext.getVariable("empresaNombre")),
                () -> assertEquals(companyLogoUrl, capturedContext.getVariable("empresaLogoUrl"))
        );

        verify(mailSender).send(mimeMessage);
    }
}
