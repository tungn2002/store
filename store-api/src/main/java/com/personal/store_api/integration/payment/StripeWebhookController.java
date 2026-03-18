package com.personal.store_api.integration.payment;

import com.personal.store_api.entity.Order;
import com.personal.store_api.service.OrderService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final OrderService orderService;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    @Transactional
    @PostMapping("/stripe")
    @PreAuthorize("hasAuthority('webhook.stripe')")
    public ResponseEntity<?> handleStripeWebhook(
            HttpServletRequest request,
            @RequestBody String payload) {

        String sigHeader = request.getHeader("Stripe-Signature");

        log.info("Received webhook signature: {}", sigHeader != null ? "present" : "missing");
        log.info("Webhook secret configured: {}", webhookSecret != null && !webhookSecret.isEmpty() ? "yes" : "no");

        Event event;
        try {
            if (webhookSecret == null || webhookSecret.isEmpty()) {
                log.warn("Webhook secret not configured, skipping signature verification");
                event = Webhook.constructEvent(payload, sigHeader, "whsec_test");
            } else {
                log.info("Verifying webhook signature with configured secret");
                event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            }
        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        } catch (Exception e) {
            log.error("Error parsing webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Invalid payload");
        }

        Integer orderId;

        log.info("Processing event type: {}", event.getType());

        switch (event.getType()) {
            case "checkout.session.completed":
                // Extract order_id from metadata using regex
                String orderIdFromMetadata = extractOrderIdFromJson(payload);

                if (orderIdFromMetadata != null) {
                    orderId = Integer.parseInt(orderIdFromMetadata);
                    log.info("Payment completed for order: {}", orderId);
                    orderService.updateOrderStatus(orderId, Order.Status.PAID);
                } else {
                    log.error("Could not extract order_id from checkout session");
                }
                break;

            case "checkout.session.expired":
                String orderIdExpired = extractOrderIdFromJson(payload);
                if (orderIdExpired != null) {
                    orderId = Integer.parseInt(orderIdExpired);
                    log.info("Payment expired for order: {}, restoring stock", orderId);
                    orderService.updateOrderStatus(orderId, Order.Status.CANCEL);
                    orderService.restoreStock(orderId);
                } else {
                    log.error("Could not extract order_id from expired session");
                }
                break;

            case "payment_intent.payment_failed":
                // Extract order ID from payment intent metadata
                var paymentIntent = event.getDataObjectDeserializer().getObject().orElse(null);
                if (paymentIntent != null && paymentIntent instanceof com.stripe.model.PaymentIntent) {
                    var intent = (com.stripe.model.PaymentIntent) paymentIntent;
                    String orderIdStr = intent.getMetadata().get("order_id");
                    if (orderIdStr != null) {
                        orderId = Integer.parseInt(orderIdStr);
                        log.info("Payment failed for order: {}, restoring stock", orderId);
                        orderService.updateOrderStatus(orderId, Order.Status.CANCEL);
                        orderService.restoreStock(orderId);
                    }
                }
                break;

            default:
                log.info("Unhandled event type: {}", event.getType());
        }

        Map<String, String> response = new HashMap<>();
        response.put("received", "true");
        return ResponseEntity.ok(response);
    }

    private String extractOrderIdFromJson(String payload) {
        // Extract order_id from metadata section using regex
        Pattern pattern = Pattern.compile("\"metadata\"\\s*:\\s*\\{[^}]*\"order_id\"\\s*:\\s*\"(\\d+)\"");
        Matcher matcher = pattern.matcher(payload);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
