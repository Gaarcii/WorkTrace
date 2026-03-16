package com.worktrace.worktracebackend.service;

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
    public void sendNewEmployeePassword(String toEmail, String employeeName, String plainPassword) {
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

            String htmlContent = templateEngine.process("email-bienvenida", context);

            helper.setText(htmlContent, true);

            System.out.println("DEBUG: Iniciando envío de correo HTML a " + toEmail);
            mailSender.send(message);
            System.out.println("DEBUG: ¡Correo HTML enviado con éxito a " + toEmail + "!");

        } catch (Exception e) {
            System.err.println("Error al enviar el correo HTML: " + e.getMessage());
        }
    }
}