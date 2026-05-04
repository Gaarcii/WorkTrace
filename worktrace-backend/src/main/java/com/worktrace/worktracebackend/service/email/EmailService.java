package com.worktrace.worktracebackend.service.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Async
    public void sendNewEmployeeWelcomeEmail(
            String toEmail,
            String employeeName,
            String plainPassword,
            String empresaLogoUrl,
            String empresaNombre,
            String nombreAdmin,
            String urlAccesoApp
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(toEmail);
            helper.setSubject("Bienvenido a Worktrace - Credenciales de acceso");

            Context context = new Context();
            context.setVariable("nombre", employeeName);
            context.setVariable("email", toEmail);
            context.setVariable("password", plainPassword);
            context.setVariable("empresaLogoUrl", empresaLogoUrl);
            context.setVariable("empresaNombre", empresaNombre);
            context.setVariable("nombreAdmin", nombreAdmin);
            context.setVariable("urlAccesoApp", urlAccesoApp);

            String htmlContent = templateEngine.process("email-bienvenida", context);

            helper.setText(htmlContent, true);

            System.out.println("DEBUG: Iniciando envío de correo HTML a " + toEmail);
            mailSender.send(message);
            System.out.println("DEBUG: ¡Correo HTML enviado con éxito a " + toEmail + "!");

        } catch (Exception e) {
            System.err.println("Error al enviar el correo HTML: " + e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String resetLink, String empresaNombre, String empresaLogoUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject("WorkTrace - Restablecer Contraseña");

            Context context = new Context();
            context.setVariable("resetLink", resetLink);
            context.setVariable("empresaNombre", empresaNombre != null ? empresaNombre : "WorkTrace");
            context.setVariable("empresaLogoUrl", empresaLogoUrl);

            String htmlContent = templateEngine.process("email-reset-password", context);

            helper.setText(htmlContent, true);

            System.out.println("DEBUG: Iniciando envío de correo HTML de reset a " + to);
            mailSender.send(message);
            System.out.println("DEBUG: ¡Correo HTML de reset enviado con éxito a " + to + "!");

        } catch (Exception e) {
            System.err.println("Error al enviar el correo HTML de reset de contraseña: " + e.getMessage());
        }
    }
}