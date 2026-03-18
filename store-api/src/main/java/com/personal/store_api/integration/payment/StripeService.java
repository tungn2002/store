package com.personal.store_api.integration.payment;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class StripeService {

    public String createCheckoutSession(Integer orderId, String customerName, String customerEmail,
                                        List<SessionLineItem> lineItems, BigDecimal totalAmount,
                                        String successUrl, String cancelUrl) throws StripeException {
        
        log.info("Creating Stripe checkout session for order: {}", orderId);
        log.info("Success URL: {}", successUrl);
        log.info("Cancel URL: {}", cancelUrl);
        
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}&order_id=" + orderId)
                .setCancelUrl(cancelUrl + "?order_id=" + orderId)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .putMetadata("order_id", String.valueOf(orderId))
                .setClientReferenceId(String.valueOf(orderId));

        if (customerEmail != null && !customerEmail.isBlank()) {
            paramsBuilder.setCustomerEmail(customerEmail);
        }

        for (SessionLineItem lineItem : lineItems) {
            paramsBuilder.addLineItem(SessionCreateParams.LineItem.builder()
                    .setQuantity((long) lineItem.quantity())
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("vnd")
                            .setUnitAmountDecimal(BigDecimal.valueOf(lineItem.price()).multiply(BigDecimal.valueOf(100)))
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(lineItem.productName())
                                    .build())
                            .build())
                    .build());
        }

        Session session = Session.create(paramsBuilder.build());
        log.info("Stripe session created: {}", session.getUrl());
        return session.getUrl();
    }

    public record SessionLineItem(String productName, int quantity, double price) {}
}
