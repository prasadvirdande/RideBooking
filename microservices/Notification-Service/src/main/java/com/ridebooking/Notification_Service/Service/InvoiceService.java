package com.ridebooking.Notification_Service.Service;

import com.ridebooking.Notification_Service.Entity.Invoice;
import com.ridebooking.Notification_Service.Repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PDFgenerationService pdfGenerationService;

    public Invoice generateAndSaveInvoice(
            String email,
            String rideId,
            String paymentId,
            BigDecimal amount
    ) {

        String invoiceNumber = "INV-" + System.currentTimeMillis();

        byte[] pdf = pdfGenerationService.generateInvoice(
                invoiceNumber,
                rideId,
                paymentId,
                amount
        );

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .rideId(rideId)
                .paymentId(paymentId)
                .userEmail(email)
                .amount(amount)
                .generatedAt(LocalDateTime.now())
                .pdf(pdf)
                .build();

        return invoiceRepository.save(invoice);
    }
}