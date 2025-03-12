package com.talhaatif.ticketbook.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    // Stripe API KEY
    @Value("${stripe.api.key}")
    private String stripeApiKey;

    public Map<String, String> createPaymentIntent(double amount, String currency, String userId) {
        Stripe.apiKey = stripeApiKey;
        try {

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) (amount * 100))  // Convert amount to cents
                    .setCurrency(currency)
                    .putMetadata("userId", userId)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret()); // Send this to the frontend

            return response;
        }
        catch (StripeException e) {
            throw new RuntimeException("Payment failed: " + e.getMessage());
        }

    }
}

