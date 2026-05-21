package com.cursoIntegrador.novaBackend.Util;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.time.LocalDateTime;
import java.net.URI;
import java.time.format.DateTimeFormatter;

public class WordGenerator {

    public static File generarPdfBienvenida(String email) throws IOException {
        File pdfFile = File.createTempFile("Bienvenida_", ".docx");

        try (FileOutputStream out = new FileOutputStream(pdfFile);
                XWPFDocument document = new XWPFDocument()) {

            XWPFParagraph imageParagraph = document.createParagraph();
            imageParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun imageRun = imageParagraph.createRun();

            try (InputStream imageStream = URI.create(
                    //"https://images.vexels.com/media/users/3/305727/isolated/preview/4695ea7b3f321f8cfc8c7ad0a5577133-circulo-de-cafe.png")
                    "http://localhost:8080/images/productos/NovaLogo.jpeg")
                    .toURL().openStream()) {
                imageRun.addPicture(
                        imageStream,
                        Document.PICTURE_TYPE_PNG,
                        "logo.png",
                        Units.toEMU(120),
                        Units.toEMU(120));
            } catch (Exception e) {
                System.err.println("No se pudo cargar la imagen: " + e.getMessage());
            }

            imageRun.addBreak();
            imageRun.addBreak();
            imageRun.addBreak();

            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("¡Bienvenido a NovaTech!");
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            document.createParagraph().createRun().addBreak();

            XWPFParagraph body = document.createParagraph();
            XWPFRun bodyRun = body.createRun();

            bodyRun.setText("Hola, " + email + " ☕");
            bodyRun.addBreak();
            bodyRun.setText("¡Gracias por unirte a nuestra comunidad amante de la tecnología!");
            bodyRun.addBreak();
            bodyRun.addBreak();

            bodyRun.setText(
                    "En NovaTech, nos encanta ofrecerte una experiencia única confianza y seguridad.");
            bodyRun.addBreak();
            bodyRun.setText("Desde ahora eres parte de la familia NovaTech, disfruta de promociones y novedades especiales.");
            bodyRun.addBreak();
            bodyRun.addBreak();

            bodyRun.setText(
                    "Recuerda que puedes iniciar sesión con tu cuenta registrada para acceder a todas las funciones.");
            bodyRun.addBreak();
            bodyRun.addBreak();

            bodyRun.setBold(true);
            bodyRun.setText("¡Nos alegra tenerte con nosotros!");
            bodyRun.setBold(false);
            bodyRun.addBreak();
            bodyRun.addBreak();

            bodyRun.setText("Fecha de registro: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            bodyRun.addBreak();
            bodyRun.addBreak();

            bodyRun.setBold(true);
            bodyRun.setText("Con cariño, el equipo de NovaTech 👁️");
            bodyRun.setBold(false);

            document.write(out);
        }

        return pdfFile;
    }

    public static File generarPdfRecuperacion(String email, String token) throws IOException {
        File pdfFile = File.createTempFile("Recuperacion_", ".docx");

        try (FileOutputStream out = new FileOutputStream(pdfFile);
                XWPFDocument document = new XWPFDocument()) {

            XWPFParagraph imageParagraph = document.createParagraph();
            imageParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun imageRun = imageParagraph.createRun();

            try (InputStream imageStream = URI.create(
                    //"https://images.vexels.com/media/users/3/305727/isolated/preview/4695ea7b3f321f8cfc8c7ad0a5577133-circulo-de-cafe.png")
                    "http://localhost:8080/images/productos/NovaLogo.jpeg")
                    .toURL().openStream()) {
                imageRun.addPicture(
                        imageStream,
                        Document.PICTURE_TYPE_PNG,
                        "logo.png",
                        Units.toEMU(120),
                        Units.toEMU(120));
            } catch (Exception e) {
                System.err.println("No se pudo cargar la imagen " + e.getMessage());
            }
            imageRun.addBreak();
            imageRun.addBreak();
            imageRun.addBreak();

            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("Recuperación de Contraseña - NovaTech");
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            document.createParagraph().createRun().addBreak();

            XWPFParagraph body = document.createParagraph();
            XWPFRun bodyRun = body.createRun();
            bodyRun.setText("Hola, " + email);
            bodyRun.addBreak();
            bodyRun.setText("Has solicitado la recuperación de tu contraseña.");
            bodyRun.addBreak();
            bodyRun.setText("Tu token de recuperación es:");
            bodyRun.addBreak();
            bodyRun.setBold(true);
            bodyRun.setText(token);
            bodyRun.addBreak();
            bodyRun.setBold(false);
            bodyRun.addBreak();
            bodyRun.setText("Este token es válido por 10 minutos.");
            bodyRun.addBreak();
            bodyRun.setText("Fecha de generación: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            bodyRun.addBreak();
            bodyRun.addBreak();
            bodyRun.setText("Si no solicitaste este cambio, considera cambiar tu contraseña por seguridad.");
            bodyRun.addBreak();
            bodyRun.addBreak();
            bodyRun.setBold(true);
            bodyRun.setText("Atentamente, el equipo de NovaTech.");
            bodyRun.setBold(false);
            document.write(out);
        }

        return pdfFile;
    }
}
