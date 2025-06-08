package com.talhaatif.ticketbook.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.EphemeralKey;
import com.stripe.model.PaymentIntent;
import com.stripe.param.EphemeralKeyCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.talhaatif.ticketbook.dto.StripeIntentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    // Stripe API KEY
    @Value("${stripe.api.key}")
    private String stripeApiKey;


    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    public StripeIntentResponse createPaymentIntent(double amount, String currency, String userId) {
        Stripe.apiKey = stripeApiKey;

        try {
            // 1. Create or retrieve a Stripe Customer
            Customer customer = Customer.create(Map.of(
                    "name", "User_" + userId,
                    "metadata", Map.of("userId", userId)
            ));

            // 2. Create PaymentIntent
            PaymentIntentCreateParams paymentIntentParams = PaymentIntentCreateParams.builder()
                    .setAmount((long) (amount * 100))
                    .setCurrency(currency)
                    .setCustomer(customer.getId())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(paymentIntentParams);


            // 3. Create Ephemeral Key (NEW WAY in SDK v21+)
            EphemeralKeyCreateParams ephemeralKeyParams = EphemeralKeyCreateParams.builder()
                    .setCustomer(customer.getId())
                    .setStripeVersion("2020-08-27")
                    .build();

            EphemeralKey ephemeralKey = EphemeralKey.create(ephemeralKeyParams);

            // 4. Return all required data
            return new StripeIntentResponse(
                    paymentIntent.getClientSecret(),
                    ephemeralKey.getSecret(), // to work for saved cards
                    customer.getId(),
                    stripePublishableKey
            );
        } catch (StripeException e) {
            throw new RuntimeException("Payment failed: " + e.getMessage());
        }
    }

    public PaymentIntent confirmPayment(String paymentIntentId) {

        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            if (!"succeeded".equals(intent.getStatus())) {
                throw new RuntimeException("Payment not completed");
            }
            return intent;

        }

        catch (StripeException e) {
            throw new RuntimeException("Payment failed: " + e.getMessage());
        }

    }
}

