package org.example.location_voiture.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.location_voiture.model.Location;
import org.example.location_voiture.model.Voiture;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class ContratPdfGenerator {

    public ByteArrayInputStream generateContratPdf(Location location) {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Font.UNDERLINE);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            // Title
            Paragraph title = new Paragraph("CONTRAT DE LOCATION DE VÉHICULE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30);
            document.add(title);

            // Intro text
            Paragraph intro = new Paragraph("Entre les soussignés :", boldFont);
            intro.setSpacingAfter(10);
            document.add(intro);

            // LOUAGEUR (Company)
            Paragraph companyLabel = new Paragraph("Le Loueur :", boldFont);
            Paragraph companyDetails = new Paragraph("AutoRent (Location Voitures)\nAdresse : Antananarivo, Madagascar\nTéléphone : +261 34 00 000 00\n", normalFont);
            companyDetails.setSpacingAfter(15);
            document.add(companyLabel);
            document.add(companyDetails);

            // LOCATAIRE (Client)
            Paragraph clientLabel = new Paragraph("Le Locataire :", boldFont);
            Paragraph clientDetails = new Paragraph(
                    "Nom & Prénom : " + location.getClient().getNom() + " " + location.getClient().getPrenom() + "\n" +
                    "CIN : " + location.getClient().getCin() + "\n" +
                    "Téléphone : " + location.getClient().getTelephone() + "\n", normalFont);
            clientDetails.setSpacingAfter(20);
            document.add(clientLabel);
            document.add(clientDetails);

            // VEHICLE INFO
            document.add(new Paragraph("Il a été convenu ce qui suit :", boldFont));
            document.add(new Paragraph("Article 1 : Objet du contrat", boldFont));
            Paragraph article1 = new Paragraph("Le Loueur met à disposition du Locataire le(s) véhicule(s) suivant(s) :", normalFont);
            article1.setSpacingAfter(10);
            document.add(article1);

            for (Voiture v : location.getVoitures()) {
                Paragraph carInfo = new Paragraph("- " + v.getMarque() + " " + v.getModele() + " (Immatriculation : " + v.getPlaqueImmatriculation() + ")", normalFont);
                carInfo.setSpacingAfter(5);
                document.add(carInfo);
            }

            // DATES & TARIFS
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            document.add(new Paragraph("\nArticle 2 : Durée et Tarification", boldFont));
            Paragraph article2 = new Paragraph(
                    "Début de location : " + location.getDateDebut().format(dtf) + "\n" +
                    "Fin de location : " + location.getDateFin().format(dtf) + "\n\n" +
                    "Montant total convenu : " + String.format("%.0f", location.getMontantTotal()) + " MGA\n", normalFont);
            article2.setSpacingAfter(20);
            document.add(article2);

            // CONDITIONS
            document.add(new Paragraph("Article 3 : Conditions Générales", boldFont));
            Paragraph conditions = new Paragraph(
                    "1. Le locataire s'engage à restituer le véhicule dans le même état qu'à la remise des clés.\n" +
                    "2. En cas de retard, des pénalités seront appliquées.\n" +
                    "3. Le véhicule ne peut être conduit que par les chauffeurs autorisés.\n", normalFont);
            conditions.setSpacingAfter(30);
            document.add(conditions);

            // SIGNATURES
            PdfPTable signatureTable = new PdfPTable(2);
            signatureTable.setWidthPercentage(100);
            
            PdfPCell cellLoueur = new PdfPCell(new Phrase("Le Loueur\n(Signature et Cachet)", boldFont));
            cellLoueur.setBorder(Rectangle.NO_BORDER);
            cellLoueur.setHorizontalAlignment(Element.ALIGN_CENTER);
            
            PdfPCell cellLocataire = new PdfPCell();
            cellLocataire.setBorder(Rectangle.NO_BORDER);
            cellLocataire.setHorizontalAlignment(Element.ALIGN_CENTER);
            
            Paragraph pLocataire = new Paragraph("Le Locataire\n(Lu et approuvé)\n", boldFont);
            pLocataire.setAlignment(Element.ALIGN_CENTER);
            cellLocataire.addElement(pLocataire);

            if (location.getSignatureClient() != null && location.getSignatureClient().startsWith("data:image")) {
                try {
                    String base64Image = location.getSignatureClient().split(",")[1];
                    byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);
                    Image signatureImg = Image.getInstance(imageBytes);
                    signatureImg.scaleToFit(150, 80);
                    signatureImg.setAlignment(Element.ALIGN_CENTER);
                    cellLocataire.addElement(signatureImg);
                } catch (Exception e) {
                    System.err.println("Failed to embed signature : " + e.getMessage());
                }
            }

            signatureTable.addCell(cellLoueur);
            signatureTable.addCell(cellLocataire);

            document.add(signatureTable);

            document.close();

        } catch (DocumentException ex) {
            ex.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
