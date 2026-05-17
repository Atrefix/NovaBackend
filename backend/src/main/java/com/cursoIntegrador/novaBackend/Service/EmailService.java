package com.cursoIntegrador.novaBackend.Service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.cursoIntegrador.novaBackend.Util.WordGenerator;

@Service
@PropertySource("classpath:secret-credentials.properties")
public class EmailService {

    @Value("${email.sender}")
    private String EMAIL_EMISOR;

    @Value("${email.password}")
    private String PASSWORD_EMISOR;

    private MultiPartEmail configurarEmail() throws EmailException {
        MultiPartEmail email = new MultiPartEmail();
        email.setHostName("smtp.gmail.com");
        email.setSmtpPort(587);
        email.setAuthentication(EMAIL_EMISOR, PASSWORD_EMISOR);
        email.setStartTLSEnabled(true);
        email.setFrom(EMAIL_EMISOR);
        return email;
    }

    public void enviarTokenDeRecuperacion(String destinatario, String token) throws EmailException {
        File pdf;

        try {
            pdf = WordGenerator.generarPdfRecuperacion(destinatario, token);
        } catch (IOException e) {
            throw new RuntimeException("Error generando el PDF de recuperación", e);
        }

        MultiPartEmail email = configurarEmail();
        email.setSubject("Recuperación de contraseña - NovaTech");
        email.setMsg("Tu token de recuperación es: " + token
                + "\nVálido por 10 minutos. \nPégalo en el formulario de recuperación de contraseña."
                + "\nSi no solicitaste este correo, ignóralo y cambia tu contraseña por seguridad.");
        email.addTo(destinatario);

        EmailAttachment attachment = new EmailAttachment();
        attachment.setPath(pdf.getAbsolutePath());
        attachment.setDisposition(EmailAttachment.ATTACHMENT);
        attachment.setDescription("Detalles de recuperación de contraseña");
        attachment.setName("Recuperacion_NovaTech.docx");

        email.attach(attachment);
        email.send();

        pdf.deleteOnExit();
    }

    public void enviarCorreoBienvenida(String destinatario) throws EmailException {
        File pdf;

        try {
            pdf = WordGenerator.generarPdfBienvenida(destinatario);
        } catch (IOException e) {
            throw new RuntimeException("Error generando el PDF de bienvenida", e);
        }

        MultiPartEmail email = configurarEmail();
        email.setSubject("¡Bienvenido a NovaTech!");
        email.setMsg("Hola " + destinatario + "\n\n"
                + "¡Gracias por unirte a nuestra comunidad de amantes del café!\n"
                + "Adjunto encontrarás tu carta de bienvenida.\n\n"
                + "El equipo de NovaTech te desea una excelente experiencia");
        email.addTo(destinatario);

        EmailAttachment attachment = new EmailAttachment();
        attachment.setPath(pdf.getAbsolutePath());
        attachment.setDisposition(EmailAttachment.ATTACHMENT);
        attachment.setDescription("Carta de bienvenida - NovaTech");
        attachment.setName("Bienvenida_NovaTech.docx");

        email.attach(attachment);
        email.send();

        pdf.deleteOnExit();
    }
}
