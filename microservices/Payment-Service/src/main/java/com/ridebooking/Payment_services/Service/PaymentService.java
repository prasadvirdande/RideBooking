package com.ridebooking.Payment_services.Service;

import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.ridebooking.Payment_services.DTO.PaymentRequestDTO;
import com.ridebooking.Payment_services.DTO.PaymentResponseDTO;
import com.ridebooking.Payment_services.DTO.UserResponseDto;
import com.ridebooking.Payment_services.Entity.Enums.PaymentMethods;
import com.ridebooking.Payment_services.Entity.Enums.PaymentStatus;
import com.ridebooking.Payment_services.Entity.Payment;
import com.ridebooking.Payment_services.FeignCLient.Feign;
import com.ridebooking.Payment_services.FeignCLient.RideClient;
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

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    public PaymentService(PaymentRepo paymentRepo,
                          Feign feign,
                          RideClient rideClient) {
        this.paymentRepo = paymentRepo;
        this.feign = feign;
        this.rideClient = rideClient;
    }

    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO request) {

        // Don't create another payment if one is already pending
        Optional<Payment> existingPayment =
                paymentRepo.findByRideId(request.getRideId());

        if (existingPayment.isPresent()
                && existingPayment.get().getPaymentStatus() == PaymentStatus.PENDING) {

            Payment payment = existingPayment.get();

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
            throw new RuntimeException("Unable to create Razorpay Payment Link", e);
        }

    }

    @Override
    public PaymentResponseDTO getPaymentByRideId(UUID rideId) {

            Payment payment = paymentRepo.findByRideId(rideId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

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
    @Transactional
    public void handleWebhook(String payload, String signature) {

        try {

            // Verify Razorpay webhook signature
            boolean isValid = Utils.verifyWebhookSignature(
                    payload,
                    signature,
                    webhookSecret
            );

            if (!isValid) {
                throw new RuntimeException("Invalid Razorpay webhook signature");
            }

            System.out.println("========== WEBHOOK ==========");
            System.out.println(payload);
            System.out.println("=============================");

            JSONObject json = new JSONObject(payload);

            String event = json.getString("event");
            System.out.println("Event : " + event);

            if (!"payment_link.paid".equals(event)
                    && !"payment.captured".equals(event)) {
                return;
            }

            String paymentLinkId;

            if ("payment_link.paid".equals(event)) {

                // Payment Link webhook
                paymentLinkId = json
                        .getJSONObject("payload")
                        .getJSONObject("payment_link")
                        .getJSONObject("entity")
                        .getString("id");

            } else {

                // payment.captured webhook
                JSONObject paymentEntity = json
                        .getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

                String description = paymentEntity.optString("description", "");

                if (description.startsWith("#")) {
                    description = description.substring(1);
                }

                paymentLinkId = "plink_" + description;
            }

            System.out.println("Payment Link Id : " + paymentLinkId);

            Payment payment = (Payment) paymentRepo
                    .findByGatewayOrderId(paymentLinkId)
                    .orElseThrow(() ->
                            new RuntimeException("Payment not found : " + paymentLinkId));
            // Ignore duplicate webhook deliverie+++
            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                System.out.println("Payment already processed.");
                return;
            }

            payment.setPaymentStatus(PaymentStatus.SUCCESS);

            JSONObject paymentEntity = json
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            payment.setGatewayPaymentId(paymentEntity.getString("id"));

            payment.setPaymentTime(LocalDateTime.now());

            paymentRepo.save(payment);

            // Notify Ride Service
            rideClient.paymentSuccess(payment.getRideId());

            System.out.println("Payment successful for Ride : "
                    + payment.getRideId());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Webhook processing failed", e);
        }
    }
    }

