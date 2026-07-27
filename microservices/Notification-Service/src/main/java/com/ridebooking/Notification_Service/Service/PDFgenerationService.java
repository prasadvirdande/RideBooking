package com.ridebooking.Notification_Service.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Service
public class PDFgenerationService {

    public byte[] generateInvoice(

            String invoiceNumber,
            String rideId,
            String paymentId,
            BigDecimal amount

    ) {

        try {

            Document document = new Document();

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font title =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            18
                    );

            document.add(new Paragraph("Ride Booking Invoice", title));

            document.add(new Paragraph(" "));

            document.add(new Paragraph("Invoice Number : " + invoiceNumber));

            document.add(new Paragraph("Ride ID : " + rideId));

            document.add(new Paragraph("Payment ID : " + paymentId));

            document.add(new Paragraph("Amount : ₹" + amount));

            document.add(new Paragraph("Status : SUCCESS"));

            document.add(new Paragraph(" "));

            document.add(new Paragraph(
                    "Thank you for riding with us."
            ));

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to generate invoice PDF",
                    e
            );
        }
    }
}