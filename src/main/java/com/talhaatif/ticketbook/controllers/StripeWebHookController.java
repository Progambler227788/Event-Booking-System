//package com.talhaatif.ticketbook.controllers;
//
//import com.stripe.Stripe;
//import com.stripe.exception.SignatureVerificationException;
//import com.stripe.model.Event;
//import com.stripe.net.Webhook;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import javax.servlet.http.HttpServletRequest;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/stripe")
//public class StripeWebhookController {
//
//    @Value("${stripe.webhook.secret}")
//    private String webhookSecret;
//
//    @PostMapping("/webhook")
//    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request, @RequestHeader("Stripe-Signature") String sigHeader) {
//        String payload;
//        try (BufferedReader reader = request.getReader()) {
//            payload = reader.lines().collect(Collectors.joining("\n"));
//        } catch (IOException e) {
//            return ResponseEntity.badRequest().body("Error reading webhook payload");
//        }
//
//        Event event;
//        try {
//            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
//        } catch (SignatureVerificationException e) {
//            return ResponseEntity.badRequest().body("Invalid webhook signature");
//        }
//
//        if ("payment_intent.succeeded".equals(event.getType())) {
//            System.out.println("✅ Payment successful, update booking status in database.");
//            // Update the booking status in Firestore or MySQL
//        }
//
//        return ResponseEntity.ok("Webhook received");
//    }
//}
//
