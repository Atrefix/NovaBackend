package com.cursoIntegrador.novaBackend.Util;

import java.io.ByteArrayOutputStream;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

public class PdfGenerator {

    public static byte[] generatePdfFromHtml(String html) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, "file:/");
            builder.toStream(outputStream);

            builder.run();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        }
    }

}
