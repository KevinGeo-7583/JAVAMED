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

                // Header Banner
                stream.beginText();
                stream.setFont(boldFont, 20);
                stream.newLineAtOffset(leftMargin, y);
                stream.showText("JavaMed - Clinical Consultation Summary");
                stream.endText();

                y -= 25;
                stream.beginText();
                stream.setFont(regularFont, 10);
                stream.newLineAtOffset(leftMargin, y);
                stream.showText("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                stream.endText();

                // Divider Line
                y -= 15;
                stream.moveTo(leftMargin, y);
                stream.lineTo(550, y);
                stream.stroke();

                // Metadata Section
                y -= 25;
                drawKeyValue(stream, boldFont, regularFont, leftMargin, y, "Attending Physician:", "Dr. " + doctor.getFullName());
                y -= 18;
                drawKeyValue(stream, boldFont, regularFont, leftMargin, y, "Patient Name:", patient.getFullName() + " (ID: " + patient.getUserId() + ")");
                y -= 18;
                drawKeyValue(stream, boldFont, regularFont, leftMargin, y, "Patient Email:", patient.getEmail());

                // Medical Content Sections
                y -= 35;
                stream.beginText();
                stream.setFont(boldFont, 13);
                stream.newLineAtOffset(leftMargin, y);
                stream.showText("Diagnosis:");
                stream.endText();

                y -= 18;
                stream.beginText();
                stream.setFont(regularFont, 11);
                stream.newLineAtOffset(leftMargin + 10, y);
                stream.showText(record.getDiagnosis());
                stream.endText();

                y -= 28;
                stream.beginText();
                stream.setFont(boldFont, 13);
                stream.newLineAtOffset(leftMargin, y);
                stream.showText("Prescription & Instructions:");
                stream.endText();

                y -= 18;
                stream.beginText();
                stream.setFont(regularFont, 11);
                stream.newLineAtOffset(leftMargin + 10, y);
                String rx = (record.getPrescription() != null && !record.getPrescription().isBlank()) 
                            ? record.getPrescription() : "None recorded.";
                stream.showText(rx);
                stream.endText();

                y -= 28;
                stream.beginText();
                stream.setFont(boldFont, 13);
                stream.newLineAtOffset(leftMargin, y);
                stream.showText("Clinical Notes:");
                stream.endText();

                y -= 18;
                stream.beginText();
                stream.setFont(regularFont, 11);
                stream.newLineAtOffset(leftMargin + 10, y);
                String notes = (record.getClinicalNotes() != null && !record.getClinicalNotes().isBlank()) 
                               ? record.getClinicalNotes() : "None recorded.";
                stream.showText(notes);
                stream.endText();
            }

            document.save(destination);
            return destination;
        }
    }

    private void drawKeyValue(PDPageContentStream stream, PDType1Font keyFont, PDType1Font valFont, 
                              float x, float y, String key, String val) throws IOException {
        stream.beginText();
        stream.setFont(keyFont, 11);
        stream.newLineAtOffset(x, y);
        stream.showText(key);
        stream.setFont(valFont, 11);
        stream.newLineAtOffset(140, 0);
        stream.showText(val);
        stream.endText();
    }
}