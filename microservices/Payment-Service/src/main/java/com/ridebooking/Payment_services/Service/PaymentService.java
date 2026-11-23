package com.ridebooking.Payment_services.Service;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.ridebooking.Payment_services.DTO.Event.PaymentCompletedEvent;
import com.ridebooking.Payment_services.DTO.Event.PaymentReceiptEvent;
import com.ridebooking.Payment_services.DTO.PaymentRequestDTO;
import com.ridebooking.Payment_services.DTO.PaymentResponseDTO;
import com.ridebooking.Payment_services.DTO.UserResponseDto;
import com.ridebooking.Payment_services.Entity.Enums.IdempotencyStatus;
import com.ridebooking.Payment_services.Entity.Enums.PaymentMethods;
import com.ridebooking.Payment_services.Entity.Enums.PaymentStatus;
import com.ridebooking.Payment_services.Entity.Idempotency;
import com.ridebooking.Payment_services.Entity.Payment;
import com.ridebooking.Payment_services.ExceptionHandling.PaymentNotFOund;
import com.ridebooking.Payment_services.FeignCLient.Feign;
import com.ridebooking.Payment_services.Repository.IdempotencyREpo;
import com.ridebooking.Payment_services.Repository.PaymentRepo;
import com.ridebooking.Payment_services.Service.Kafka.NotificationProducer;
import com.ridebooking.Payment_services.Service.Kafka.PaymentProducer;

import jakarta.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService implements PaymentServiceINter {

    private final PaymentRepo paymentRepo;
    private final Feign feign;
    private final PaymentProducer paymentProducer;
    private final NotificationProducer notificationProducer;
    private final IdempotencyREpo idempotencyREpo;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    public PaymentService(
            PaymentRepo paymentRepo,
            Feign feign,
            PaymentProducer paymentProducer,
            NotificationProducer notificationProducer,
            IdempotencyREpo idempotencyREpo) {

        this.paymentRepo = paymentRepo;
        this.feign = feign;
        this.paymentProducer = paymentProducer;
        this.notificationProducer = notificationProducer;
        this.idempotencyREpo = idempotencyREpo;
    }

    // =========================================================
    // CREATE PAYMENT
    // =========================================================

    @Override
    public PaymentResponseDTO createPayment(
            PaymentRequestDTO request,
            String idempotencyKey) {

        // 1. Validate request

        if (request == null) {
            throw new IllegalArgumentException(
                    "Payment request cannot be null"
            );
        }

        if (request.getRideId() == null
                || request.getUserId() == null
                || request.getDriverId() == null) {

            throw new IllegalArgumentException(
                    "Ride ID, User ID and Driver ID are required"
            );
        }

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency key is required"
            );
        }

        UUID key;

        try {
            key = UUID.fromString(idempotencyKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid idempotency key",
                    e
            );
        }

        // 2. Validate idempotency key against ride ID

        if (!key.equals(request.getRideId())) {
            throw new IllegalArgumentException(
                    "Idempotency key does not match ride ID"
            );
        }

        // 3. Normalize amount to two decimal places

        BigDecimal amount = normalizeAmount(request.getAmount());

        // 4. Check whether payment already exists for this ride

        Optional<Payment> existingPayment =
                paymentRepo.findByRideId(request.getRideId());

        if (existingPayment.isPresent()) {

            Payment payment = existingPayment.get();

            Idempotency idempotency =
                    idempotencyREpo.findById(key)
                            .orElseGet(() -> Idempotency.builder()
                                    .id(key)
                                    .build());

            idempotency.setStatus(IdempotencyStatus.COMPLETED);

            idempotencyREpo.save(idempotency);

            System.out.println(
                    "Returning existing payment for ride: "
                            + request.getRideId()
            );

            return buildPaymentResponse(payment);
        }

        // 5. Check idempotency record

        Optional<Idempotency> existingIdempotency =
                idempotencyREpo.findById(key);

        Idempotency idempotency;

        if (existingIdempotency.isPresent()) {

            idempotency = existingIdempotency.get();

            if (idempotency.getStatus()
                    == IdempotencyStatus.PROCESSING) {

                throw new RuntimeException(
                        "Payment creation is already in progress. Please retry."
                );
            }

            if (idempotency.getStatus()
                    == IdempotencyStatus.COMPLETED) {

                throw new RuntimeException(
                        "Payment is marked completed, but its payment record is missing."
                );
            }

            // Previous attempt failed. Permit retry.

            idempotency.setStatus(IdempotencyStatus.PROCESSING);

        } else {

            idempotency = Idempotency.builder()
                    .id(key)
                    .status(IdempotencyStatus.PROCESSING)
                    .build();
        }

        idempotencyREpo.save(idempotency);

        // 6. Create Razorpay payment link

        try {

            RazorpayClient razorpayClient =
                    new RazorpayClient(keyId, keySecret);

            UserResponseDto user =
                    feign.getUserById(request.getUserId());

            JSONObject paymentLinkRequest = new JSONObject();

            // IMPORTANT:
            // Razorpay expects the amount in paise.
            // Example: Rs. 216.45 = 21645 paise.

            int amountInPaise = convertToPaise(amount);

            paymentLinkRequest.put("amount", amountInPaise);

            paymentLinkRequest.put("currency", "INR");

            paymentLinkRequest.put("accept_partial", false);

            paymentLinkRequest.put(
                    "description",
                    "Ride Payment : " + request.getRideId()
            );

            JSONObject customer = new JSONObject();

            customer.put("name", user.getFullName());
            customer.put("email", user.getEmail());
            customer.put("contact", user.getPhoneNumber());

            paymentLinkRequest.put("customer", customer);

            JSONObject notify = new JSONObject();

            notify.put("sms", false);
            notify.put("email", false);

            paymentLinkRequest.put("notify", notify);

            PaymentLink paymentLink =
                    razorpayClient.paymentLink.create(paymentLinkRequest);

            // 7. Save payment in database

            Payment payment = Payment.builder()
                    .rideId(request.getRideId())
                    .userId(request.getUserId())
                    .driverId(request.getDriverId())
                    .amount(amount)
                    .paymentMethod(PaymentMethods.UPI)
                    .paymentStatus(PaymentStatus.PENDING)
                    .gatewayName("RAZORPAY")
                    .gatewayOrderId(
                            paymentLink.get("id").toString()
                    )
                    .paymentLink(
                            paymentLink.get("short_url").toString()
                    )
                    .build();

            Payment savedPayment = paymentRepo.save(payment);

            // 8. Mark idempotency completed

            idempotency.setStatus(IdempotencyStatus.COMPLETED);

            idempotencyREpo.save(idempotency);

            System.out.println(
                    "Payment created successfully for ride: "
                            + savedPayment.getRideId()
            );

            return buildPaymentResponse(savedPayment);

        } catch (Exception e) {

            idempotency.setStatus(IdempotencyStatus.FAILED);

            idempotencyREpo.save(idempotency);

            throw new RuntimeException(
                    "Unable to create payment. Please retry.",
                    e
            );
        }
    }

    // =========================================================
    // AMOUNT CONVERSION
    // =========================================================

    private BigDecimal normalizeAmount(BigDecimal amount) {

        if (amount == null) {
            throw new IllegalArgumentException(
                    "Payment amount cannot be null"
            );
        }

        BigDecimal normalizedAmount =
                amount.setScale(2, RoundingMode.HALF_UP);

        if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        return normalizedAmount;
    }

    private int convertToPaise(BigDecimal amount) {

        return normalizeAmount(amount)
                .movePointRight(2)
                .intValueExact();
    }

    // =========================================================
    // PAYMENT RESPONSE
    // =========================================================

    private PaymentResponseDTO buildPaymentResponse(Payment payment) {

        return PaymentResponseDTO.builder()
                .paymentId(payment.getPaymentId())
                .rideId(payment.getRideId())
                .userId(payment.getUserId())
                .driverId(payment.getDriverId())
                .amount(payment.getAmount())
                .paymentStatus(payment.getPaymentStatus())
                .paymentLink(payment.getPaymentLink())
                .currency("INR")
                .build();
    }

    // =========================================================
    // GET PAYMENT BY RIDE ID
    // =========================================================

    @Override
    public PaymentResponseDTO getPaymentByRideId(UUID rideId) {

        Payment payment = paymentRepo.findByRideId(rideId)
                .orElseThrow(() ->
                        new PaymentNotFOund("Payment not found")
                );

        return buildPaymentResponse(payment);
    }

    // =========================================================
    // RAZORPAY WEBHOOK
    // =========================================================

    @Override
    @Transactional

    public void handleWebhook(String payload, String signature) {

        // 1. Validate webhook request
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Webhook payload is empty");
        }

        if (signature == null || signature.isBlank()) {
            throw new IllegalArgumentException("Webhook signature is missing");
        }

        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException(
                    "Razorpay webhook secret is not configured"
            );
        }

        try {

            // 2. Verify Razorpay webhook signature
            boolean isValid = Utils.verifyWebhookSignature(
                    payload,
                    signature,
                    webhookSecret
            );

            if (!isValid) {
                throw new SecurityException(
                        "Invalid Razorpay webhook signature"
                );
            }

            log.info("Razorpay webhook signature verified successfully");

            // 3. Parse webhook event
            JSONObject webhookJson = new JSONObject(payload);

            String event = webhookJson.optString("event");

            log.info("Received Razorpay webhook event: {}", event);

            // 4. Process only payment_link.paid
            if (!"payment_link.paid".equals(event)) {
                log.info("Ignoring Razorpay webhook event: {}", event);
                return;
            }

            JSONObject payloadObject = webhookJson.getJSONObject("payload");

            JSONObject paymentLinkEntity = payloadObject
                    .getJSONObject("payment_link")
                    .getJSONObject("entity");

            JSONObject paymentEntity = payloadObject
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            // 5. Extract Razorpay details
            String paymentLinkId = paymentLinkEntity.getString("id");

            String razorpayPaymentId = paymentEntity.getString("id");

            String paymentLinkStatus = paymentLinkEntity.optString("status");

            String paymentStatus = paymentEntity.optString("status");

            log.info("Payment Link ID: {}", paymentLinkId);
            log.info("Razorpay Payment ID: {}", razorpayPaymentId);

            // 6. Validate payment statuses
            if (!"paid".equalsIgnoreCase(paymentLinkStatus)) {
                log.warn("Payment link is not paid. Status: {}", paymentLinkStatus);
                return;
            }

            if (!"captured".equalsIgnoreCase(paymentStatus)) {
                log.warn("Payment is not captured. Status: {}", paymentStatus);
                return;
            }

            // 7. Find payment using Razorpay Payment Link ID
            Payment payment = paymentRepo
                    .findByGatewayOrderId(paymentLinkId)
                    .orElseThrow(() -> new RuntimeException(
                            "Payment not found for Payment Link ID: " + paymentLinkId
                    ));

            // 8. Validate essential payment data
            if (payment.getRideId() == null) {
                throw new IllegalStateException(
                        "Ride ID is missing for payment: " + payment.getPaymentId()
                );
            }

            if (payment.getUserId() == null) {
                throw new IllegalStateException(
                        "User ID is missing for payment: " + payment.getPaymentId()
                );
            }

            // 9. Idempotency check
            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                log.info(
                        "Payment already processed successfully. Payment ID: {}",
                        payment.getPaymentId()
                );
                return;
            }

            // 10. Validate amount and currency

            // Database amount is stored in INR
            BigDecimal expectedAmountInRupees = payment.getAmount();

            if (expectedAmountInRupees == null) {
                throw new IllegalStateException("Payment amount is missing");
            }

            // Convert INR into paise
            BigDecimal expectedAmountInPaise =
                    expectedAmountInRupees.movePointRight(2);

            // Razorpay returns amount in paise
            BigDecimal receivedAmountInPaise = BigDecimal.valueOf(
                    paymentEntity.getLong("amount")
            );

            String currency = paymentEntity.optString("currency");

            if (expectedAmountInPaise.compareTo(receivedAmountInPaise) != 0) {
                throw new IllegalArgumentException(
                        "Payment amount mismatch. Expected: "
                                + expectedAmountInPaise
                                + " paise, Received: "
                                + receivedAmountInPaise
                                + " paise"
                );
            }

            if (!"INR".equalsIgnoreCase(currency)) {
                throw new IllegalArgumentException(
                        "Unsupported payment currency: " + currency
                );
            }

            log.info(
                    "Payment amount validated successfully: {} INR",
                    expectedAmountInRupees
            );

            // 11. Update payment record
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setGatewayPaymentId(razorpayPaymentId);
            payment.setPaymentTime(LocalDateTime.now());

            paymentRepo.save(payment);

            log.info(
                    "Payment marked as SUCCESS. Internal Payment ID: {}",
                    payment.getPaymentId()
            );

            // 12. Create PaymentCompletedEvent
            PaymentCompletedEvent paymentCompletedEvent =
                    new PaymentCompletedEvent(
                            payment.getPaymentId(),
                            payment.getRideId(),
                            payment.getUserId(),
                            payment.getDriverId(),
                            payment.getAmount(),
                            payment.getPaymentStatus().name()
                    );

            // Publish payment completed event
            paymentProducer.publishPaymentCompleted(paymentCompletedEvent);

            log.info(
                    "PaymentCompletedEvent published. Payment ID: {}, Ride ID: {}",
                    payment.getPaymentId(),
                    payment.getRideId()
            );

            // 13. Fetch user details
            UserResponseDto user = feign.getUserById(payment.getUserId());

            if (user == null || user.getEmail() == null
                    || user.getEmail().isBlank()) {
                throw new IllegalStateException(
                        "User email is missing for user: " + payment.getUserId()
                );
            }

            // 14. Create PaymentReceiptEvent
            PaymentReceiptEvent paymentReceiptEvent =
                    new PaymentReceiptEvent(
                            payment.getPaymentId(),
                            payment.getRideId(),
                            payment.getUserId(),
                            user.getEmail(),
                            payment.getAmount(),
                            payment.getPaymentStatus().name(),
                            payment.getPaymentMethod().name(),
                            payment.getPaymentTime()
                    );

            // Publish receipt event only once
            notificationProducer.publishPaymentReceipt(paymentReceiptEvent);

            log.info(
                    "PaymentReceiptEvent published. Payment ID: {}",
                    payment.getPaymentId()
            );

        } catch (SecurityException | IllegalArgumentException e) {

            log.error("Webhook validation failed: {}", e.getMessage());

            throw e;

        } catch (Exception e) {

            log.error("Error processing Razorpay webhook", e);

            throw new RuntimeException(
                    "Webhook processing failed",
                    e
            );
        }
    }
}