package com.ridebooking.Payment_services.Service;

import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.ridebooking.Payment_services.DTO.PaymentReceiptRequest;
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
import com.ridebooking.Payment_services.FeignCLient.NotificationFeign;
import com.ridebooking.Payment_services.FeignCLient.RideClient;
import com.ridebooking.Payment_services.Repository.IdempotencyREpo;
import com.ridebooking.Payment_services.Repository.PaymentRepo;
import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService implements PaymentServiceINter {

    private final PaymentRepo paymentRepo;
    private final Feign feign;
    private final RideClient rideClient;
    private final NotificationFeign notificationFeign;
    private final IdempotencyREpo idempotencyREpo;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    public PaymentService(PaymentRepo paymentRepo,
                          Feign feign,
                          RideClient rideClient, NotificationFeign notificationFeign, IdempotencyREpo idempotencyREpo) {
        this.paymentRepo = paymentRepo;
        this.feign = feign;
        this.rideClient = rideClient;
        this.notificationFeign = notificationFeign;
        this.idempotencyREpo = idempotencyREpo;
    }

    @Override
    public PaymentResponseDTO createPayment(
            PaymentRequestDTO request,
            String idempotencyKey) {

        // 1. Check whether this exact request was already processed
        Optional<Idempotency> existingIdempotency =
                idempotencyREpo.findById(UUID.fromString(idempotencyKey));

        if (existingIdempotency.isPresent()) {
            throw new RuntimeException("Duplicate payment request");
        }

        // 2. Mark this request as PROCESSING
        Idempotency idempotency = Idempotency.builder()
                .id(UUID.fromString(idempotencyKey))
                .status(IdempotencyStatus.PROCESSING)
                .build();

        idempotencyREpo.save(idempotency);

        // 3. Your existing check
        Optional<Payment> existingPayment =
                paymentRepo.findByRideId(request.getRideId());

        if (existingPayment.isPresent()
                && existingPayment.get().getPaymentStatus() == PaymentStatus.PENDING) {

            Payment payment = existingPayment.get();

            idempotency.setStatus(IdempotencyStatus.COMPLETED);
            idempotencyREpo.save(idempotency);

            return PaymentResponseDTO.builder()
                    .paymentId(payment.getPaymentId())
                    .rideId(payment.getRideId())
                    .userId(payment.getUserId())
                    .driverId(payment.getDriverId())
                    .amount(payment.getAmount())
                    .paymentStatus(payment.getPaymentStatus())
                    .paymentMethod(payment.getPaymentMethod())
                    .gatewayName(payment.getGatewayName())
                    .gatewayOrderId(payment.getGatewayOrderId())
                    .paymentLink(payment.getPaymentLink())
                    .currency("INR")
                    .keyId(keyId)
                    .qrCode(payment.getQrCode())
                    .build();
        }

        try {

            RazorpayClient razorpayClient =
                    new RazorpayClient(keyId, keySecret);

            UserResponseDto user =
                    feign.getUserById(request.getUserId());

            JSONObject paymentLinkRequest = new JSONObject();

            paymentLinkRequest.put(
                    "amount",
                    request.getAmount()
                            .multiply(BigDecimal.valueOf(100))
                            .intValue());

            paymentLinkRequest.put("currency", "INR");
            paymentLinkRequest.put("accept_partial", false);

            paymentLinkRequest.put(
                    "description",
                    "Ride Payment : " + request.getRideId());

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

            Payment payment = Payment.builder()
                    .rideId(request.getRideId())
                    .userId(request.getUserId())
                    .driverId(request.getDriverId())
                    .amount(request.getAmount())
                    .paymentMethod(PaymentMethods.UPI)
                    .paymentStatus(PaymentStatus.PENDING)
                    .gatewayName("RAZORPAY")
                    .gatewayOrderId(paymentLink.get("id").toString())
                    .paymentLink(paymentLink.get("short_url").toString())
                    .build();

            Payment savedPayment = paymentRepo.save(payment);

            // 4. Mark idempotency as COMPLETED
            idempotency.setStatus(IdempotencyStatus.COMPLETED);
            idempotencyREpo.save(idempotency);

            return PaymentResponseDTO.builder()
                    .paymentId(savedPayment.getPaymentId())
                    .rideId(savedPayment.getRideId())
                    .userId(savedPayment.getUserId())
                    .driverId(savedPayment.getDriverId())
                    .amount(savedPayment.getAmount())
                    .paymentStatus(savedPayment.getPaymentStatus())
                    .paymentMethod(savedPayment.getPaymentMethod())
                    .gatewayName(savedPayment.getGatewayName())
                    .gatewayOrderId(savedPayment.getGatewayOrderId())
                    .paymentLink(savedPayment.getPaymentLink())
                    .currency("INR")
                    .keyId(keyId)
                    .qrCode(savedPayment.getQrCode())
                    .build();

        } catch (RazorpayException e) {

            idempotency.setStatus(IdempotencyStatus.FAILED);
            idempotencyREpo.save(idempotency);

            throw new RuntimeException(
                    "Unable to create Razorpay Payment Link", e);
        }
    }
    @Override
    public PaymentResponseDTO getPaymentByRideId(UUID rideId) {

            Payment payment = paymentRepo.findByRideId(rideId)
                    .orElseThrow(() -> new PaymentNotFOund("Payment not found"));

            return PaymentResponseDTO.builder()
                    .paymentId(payment.getPaymentId())
                    .rideId(payment.getRideId())
                    .userId(payment.getUserId())
                    .driverId(payment.getDriverId())
                    .amount(payment.getAmount())
                    .paymentStatus(payment.getPaymentStatus())
                    .paymentMethod(payment.getPaymentMethod())
                    .gatewayName(payment.getGatewayName())
                    .gatewayOrderId(payment.getGatewayOrderId())
                    .paymentLink(payment.getPaymentLink())
                    .currency("INR")
                    .keyId(keyId)
                    .qrCode(payment.getQrCode())
                    .build();
        }


    @Override

    public void handleWebhook(String payload, String signature) {


        if (payload == null || payload.isBlank()) {
            throw new RuntimeException("Webhook payload is empty");
        }

        if (signature == null || signature.isBlank()) {
            throw new RuntimeException("X-Razorpay-Signature is missing");
        }

        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new RuntimeException("Razorpay webhook secret is not configured");
        }


        boolean isValid;

        try {

            isValid = Utils.verifyWebhookSignature(
                    payload,
                    signature,
                    webhookSecret
            );

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Razorpay webhook signature verification failed",
                    e
            );
        }

        if (!isValid) {

            System.out.println(" INVALID RAZORPAY WEBHOOK SIGNATURE");

            throw new RuntimeException(
                    "Invalid Razorpay webhook signature"
            );
        }

        System.out.println("RAZORPAY WEBHOOK SIGNATURE VERIFIED");

        System.out.println("========== WEBHOOK ==========");
        System.out.println(payload);
        System.out.println("=============================");

        JSONObject json = new JSONObject(payload);

        String event = json.getString("event");

        System.out.println("Event : " + event);

        // =========================================================
        // 4. IGNORE UNNECESSARY EVENTS
        // =========================================================

        if (!"payment_link.paid".equals(event)
                && !"payment.captured".equals(event)) {

            System.out.println(
                    "Ignoring webhook event : " + event
            );

            return;
        }

        String paymentLinkId;

        if ("payment_link.paid".equals(event)) {

            paymentLinkId = json
                    .getJSONObject("payload")
                    .getJSONObject("payment_link")
                    .getJSONObject("entity")
                    .getString("id");

        } else {

            JSONObject paymentEntity = json
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String description =
                    paymentEntity.optString("description", "");

            if (description.startsWith("#")) {
                description = description.substring(1);
            }

            paymentLinkId = "plink_" + description;
        }

        System.out.println(
                "Payment Link Id : " + paymentLinkId
        );



        Payment payment = (Payment) paymentRepo
                .findByGatewayOrderId(paymentLinkId)
                .orElseThrow(() ->
                        new PaymentNotFOund(
                                "Payment not found : "
                                        + paymentLinkId
                        )
                );



        if (payment.getPaymentStatus()
                == PaymentStatus.SUCCESS) {

            System.out.println(
                    "Payment already processed."
            );

            return;
        }


        JSONObject paymentEntity = json
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String razorpayPaymentId =
                paymentEntity.getString("id");


        payment.setPaymentStatus(
                PaymentStatus.SUCCESS
        );

        payment.setGatewayPaymentId(
                razorpayPaymentId
        );

        payment.setPaymentTime(
                LocalDateTime.now()
        );

        paymentRepo.save(payment);

        System.out.println(
                "Payment marked SUCCESS : "
                        + payment.getRideId()
        );

        // =========================================================
        // 10. NOTIFY RIDE SERVICE
        // =========================================================

        rideClient.paymentSuccess(
                payment.getRideId()
        );

        // =========================================================
        // 11. GET USER
        // =========================================================

        UserResponseDto user =
                feign.getUserById(
                        payment.getUserId()
                );

        // =========================================================
        // 12. SEND PAYMENT RECEIPT
        // =========================================================

        notificationFeign.sendPaymentReceipt(
                PaymentReceiptRequest.builder()
                        .email(user.getEmail())
                        .rideId(
                                payment.getRideId().toString()
                        )
                        .paymentId(
                                payment.getGatewayPaymentId()
                        )
                        .amount(payment.getAmount())
                        .build()
        );

        System.out.println(
                "✅ Payment successful for Ride : "
                        + payment.getRideId()
        );
    }
    }

