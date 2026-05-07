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

/**
 * Servicio para gestionar el envío de correos electrónicos de la aplicación.
 * Se encarga de construir y enviar diferentes tipos de notificaciones, como
 * correos de bienvenida o de recuperación de contraseña, utilizando plantillas HTML
 * para un formato enriquecido. Las operaciones se ejecutan de forma asíncrona
 * para no impactar en el rendimiento de las peticiones principales.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String senderEmail;

    /**
     * Envía un correo de bienvenida a un nuevo empleado.
     * El propósito es proporcionar al empleado sus credenciales de acceso iniciales
     * (email y contraseña generada) y darle la bienvenida a la plataforma en nombre de su empresa.
     *
     * @param toEmail          Dirección de correo del nuevo empleado.
     * @param employeeName     Nombre completo del empleado.
     * @param plainPassword    Contraseña en texto plano para que el empleado inicie sesión por primera vez.
     * @param companyLogoUrl   URL del logo de la empresa para personalizar el correo.
     * @param companyName      Nombre de la empresa.
     * @param adminName        Nombre del administrador que lo ha registrado.
     * @param appAccessUrl     URL de acceso a la aplicación.
     */
    @Async
    public void sendNewEmployeeWelcomeEmail(
            String toEmail,
            String employeeName,
            String plainPassword,
            String companyLogoUrl,
            String companyName,
            String adminName,
            String appAccessUrl
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
            context.setVariable("empresaLogoUrl", companyLogoUrl);
            context.setVariable("empresaNombre", companyName);
            context.setVariable("nombreAdmin", adminName);
            context.setVariable("urlAccesoApp", appAccessUrl);

            String htmlContent = templateEngine.process("email-bienvenida", context);

            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Error al enviar el correo HTML: " + e.getMessage());
        }
    }

    /**
     * Envía un correo de bienvenida a un nuevo inspector.
     * Proporciona al inspector sus credenciales de acceso para que pueda realizar
     * auditorías en la plataforma.
     *
     * @param toEmail          Dirección de correo del nuevo inspector.
     * @param inspectorName    Nombre completo del inspector.
     * @param plainPassword    Contraseña en texto plano para el primer inicio de sesión.
     * @param empresaLogoUrl   URL del logo de la empresa que lo registra.
     * @param empresaNombre    Nombre de la empresa.
     * @param nombreAdmin      Nombre del administrador que lo ha registrado.
     * @param urlAccesoApp     URL de acceso a la aplicación.
     */
    @Async
    public void sendNewInspectorWelcomeEmail(
            String toEmail,
            String inspectorName,
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
            helper.setSubject("Bienvenido a Worktrace - Acceso de Auditor");

            Context context = new Context();
            context.setVariable("nombre", inspectorName);
            context.setVariable("email", toEmail);
            context.setVariable("password", plainPassword);
            context.setVariable("empresaLogoUrl", empresaLogoUrl);
            context.setVariable("empresaNombre", empresaNombre);
            context.setVariable("nombreAdmin", nombreAdmin);
            context.setVariable("urlAccesoApp", urlAccesoApp);

            String htmlContent = templateEngine.process("email-bienvenida-inspector", context);

            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Error al enviar el correo de bienvenida para inspector: " + e.getMessage());
        }
    }

    /**
     * Envía un correo para restablecer la contraseña.
     * El correo contiene un enlace único y de tiempo limitado que permite al usuario
     * establecer una nueva contraseña de forma segura.
     *
     * @param to             Dirección de correo del usuario que solicita el restablecimiento.
     * @param resetLink      URL única para el proceso de restablecimiento.
     * @param empresaNombre  Nombre de la empresa para personalizar el correo.
     * @param empresaLogoUrl URL del logo de la empresa.
     */
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

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Error al enviar el correo HTML de reset de contraseña: " + e.getMessage());
        }
    }
}
