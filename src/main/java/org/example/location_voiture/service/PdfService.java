package org.example.location_voiture.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.location_voiture.model.Facture;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.stream.Collectors;

@Service
public class PdfService {

    public byte[] generateInvoicePdfBytes(Facture facture) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Font styles
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

            // Title
            Paragraph title = new Paragraph("FACTURE", headerFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Header Section: Company and Invoice Details
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);

            // Company Info (Left)
            PdfPCell companyCell = new PdfPCell();
            companyCell.setBorder(Rectangle.NO_BORDER);
            companyCell.addElement(new Paragraph("LOCATION VOITURE", subHeaderFont));
            companyCell.addElement(new Paragraph("Antananarivo, Madagascar", normalFont));
            companyCell.addElement(new Paragraph("+261 34 00 000 00", normalFont));
            headerTable.addCell(companyCell);

            // Invoice Info (Right)
            PdfPCell invoiceInfoCell = new PdfPCell();
            invoiceInfoCell.setBorder(Rectangle.NO_BORDER);
            invoiceInfoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph numPara = new Paragraph("N° Facture: " + facture.getNumeroFacture(), boldFont);
            numPara.setAlignment(Element.ALIGN_RIGHT);
            invoiceInfoCell.addElement(numPara);
            
            Paragraph datePara = new Paragraph("Date: " + facture.getDateEmission(), normalFont);
            datePara.setAlignment(Element.ALIGN_RIGHT);
            invoiceInfoCell.addElement(datePara);
            
            Paragraph statutPara = new Paragraph("Statut: " + facture.getStatut(), normalFont);
            statutPara.setAlignment(Element.ALIGN_RIGHT);
            invoiceInfoCell.addElement(statutPara);
            headerTable.addCell(invoiceInfoCell);

            document.add(headerTable);
            document.add(new Paragraph("\n\n"));

            // Client Info Section
            Paragraph clientTitle = new Paragraph("DESTINATAIRE:", boldFont);
            document.add(clientTitle);
            document.add(new Paragraph(facture.getLocation().getClient().getNomComplet(), normalFont));
            document.add(new Paragraph(facture.getLocation().getClient().getAdresse(), normalFont));
            document.add(new Paragraph(facture.getLocation().getClient().getEmail(), normalFont));
            document.add(new Paragraph("\n\n"));

            // Table for Items
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4, 2, 2, 2});

            // Table Headers
            addTableHeader(table, "Désignation", boldFont);
            addTableHeader(table, "Quantité/Durée", boldFont);
            addTableHeader(table, "Prix Unitaire", boldFont);
            addTableHeader(table, "Total", boldFont);

            // Item Row
            long jours = java.time.temporal.ChronoUnit.DAYS.between(
                    facture.getLocation().getDateDebut(),
                    facture.getLocation().getDateFin()
            );
            if (jours <= 0) jours = 1;

            String voitures = facture.getLocation().getVoitures().stream()
                    .map(v -> v.getMarque() + " " + v.getModele())
                    .collect(Collectors.joining(", "));

            table.addCell(new Phrase("Location de voiture(s): " + voitures, normalFont));
            table.addCell(new Phrase(jours + " jour(s)", normalFont));
            table.addCell(new Phrase("-", normalFont)); 
            table.addCell(new Phrase(String.format("%.2f %s", facture.getMontantHT(), facture.getDevise()), normalFont));

            document.add(table);
            document.add(new Paragraph("\n"));

            // Summary Section
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(40);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            addSummaryRow(summaryTable, "Sous-total HT:", String.format("%.2f %s", facture.getMontantHT() - (facture.getPenalite() != null ? facture.getPenalite() : 0), facture.getDevise()), normalFont);
            if (facture.getPenalite() != null && facture.getPenalite() > 0) {
                Font penaltyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.RED);
                addSummaryRow(summaryTable, "PÉNALITÉ RETARD (+50%):", String.format("%.2f %s", facture.getPenalite(), facture.getDevise()), penaltyFont);
            }
            addSummaryRow(summaryTable, "Montant Total:", String.format("%.2f %s", facture.getMontantTTC(), facture.getDevise()), boldFont);

            document.add(summaryTable);

            document.close();
        } catch (DocumentException ex) {
            ex.printStackTrace();
        }

        return out.toByteArray();
    }

    public ByteArrayInputStream generateInvoicePdf(Facture facture) {
        return new ByteArrayInputStream(generateInvoicePdfBytes(facture));
    }

    private void addTableHeader(PdfPTable table, String headerTitle, Font font) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(Color.LIGHT_GRAY);
        header.setBorderWidth(1);
        header.setPhrase(new Phrase(headerTitle, font));
        header.setPadding(5);
        table.addCell(header);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, font));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        table.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Phrase(value, font));
        cellValue.setBorder(Rectangle.NO_BORDER);
        cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cellValue);
    }
}
