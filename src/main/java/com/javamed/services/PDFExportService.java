package com.javamed.services;

import com.javamed.models.ClinicalRecord;
import com.javamed.models.User;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PDFExportService {

    public File generateClinicalReport(User patient, User doctor, ClinicalRecord record, File destination) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                float y = 740;
                float leftMargin = 50;

                // Title
                stream.beginText();
                stream.setFont(boldFont, 20);
                stream.newLineAtOffset(leftMargin, y);
                stream.showText("JavaMed - Clinical Consultation Summary");
                stream.endText();

                // Timestamp
                y -= 25;
                stream.beginText();
                stream.setFont(regularFont, 10);
                stream.newLineAtOffset(leftMargin, y);
                stream.showText("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                stream.endText();

                // Divider line (must be drawn OUTSIDE beginText/endText)
                y -= 15;
                stream.moveTo(leftMargin, y);
                stream.lineTo(550, y);
                stream.stroke();

                // Metadata Rows
                y -= 25;
                writeText(stream, boldFont, 11, leftMargin, y, "Attending Physician: ");
                writeText(stream, regularFont, 11, leftMargin + 140, y, "Dr. " + (doctor != null ? doctor.getFullName() : "N/A"));

                y -= 18;
                writeText(stream, boldFont, 11, leftMargin, y, "Patient Name: ");
                writeText(stream, regularFont, 11, leftMargin + 140, y, (patient != null ? patient.getFullName() : "N/A") + " (ID: " + (patient != null ? patient.getUserId() : "N/A") + ")");

                // Diagnosis Block
                y -= 30;
                writeText(stream, boldFont, 12, leftMargin, y, "Diagnosis:");
                y -= 18;
                String diag = (record.getDiagnosis() != null && !record.getDiagnosis().isBlank()) ? record.getDiagnosis() : "None";
                writeText(stream, regularFont, 11, leftMargin + 10, y, sanitize(diag));

                // Prescription Block
                y -= 28;
                writeText(stream, boldFont, 12, leftMargin, y, "Prescription & Instructions:");
                y -= 18;
                String rx = (record.getPrescription() != null && !record.getPrescription().isBlank()) ? record.getPrescription() : "None recorded.";
                writeText(stream, regularFont, 11, leftMargin + 10, y, sanitize(rx));

                // Clinical Notes Block
                y -= 28;
                writeText(stream, boldFont, 12, leftMargin, y, "Clinical Notes:");
                y -= 18;
                String notes = (record.getClinicalNotes() != null && !record.getClinicalNotes().isBlank()) ? record.getClinicalNotes() : "None recorded.";
                writeText(stream, regularFont, 11, leftMargin + 10, y, sanitize(notes));
            } // Stream automatically closes cleanly here with all text blocks terminated

            document.save(destination);
            return destination;
        }
    }

    private void writeText(PDPageContentStream stream, PDType1Font font, float size, float x, float y, String text) throws IOException {
        stream.beginText();
        stream.setFont(font, size);
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
    }

    private String sanitize(String text) {
        if (text == null) return "";
        // PDFBox Type 1 fonts only support single-line ISO-8859-1 strings without raw newlines
        return text.replace("\r", " ").replace("\n", " ").trim();
    }
}