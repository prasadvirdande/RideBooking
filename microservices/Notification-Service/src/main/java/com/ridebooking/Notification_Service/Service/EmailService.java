package com.ridebooking.Notification_Service.Service;

import com.ridebooking.Notification_Service.Entity.Invoice;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final InvoiceService invoiceService;

    public void sendPaymentReceipt(
            String email,
            String rideId,
            BigDecimal amount,
            String paymentId
    ) {

        try {

            Invoice invoice = invoiceService.generateAndSaveInvoice(
                    email,
                    rideId,
                    paymentId,
                    amount
            );

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setFrom("prasadvirdande@gmail.com");
            helper.setTo(email);
            helper.setSubject("Payment Successful");

            helper.setText(
                    """
                    Hi,

                    Your payment has been received successfully.

                    Ride ID : %s
                    Payment ID : %s
                    Amount : ₹%s

                    Please find your invoice attached.

                    Thank you for riding with us.

                    Regards,
                    Ride Booking Team
                    """
                            .formatted(
                                    rideId,
                                    paymentId,
                                    amount
                            )
            );

            helper.addAttachment(
                    invoice.getInvoiceNumber() + ".pdf",
                    new ByteArrayResource(invoice.getPdf())
            );

            mailSender.send(message);

        } catch (Exception e) {

            throw new RuntimeException("Unable to send invoice email", e);
        }
    }
}