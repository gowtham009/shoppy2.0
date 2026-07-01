package com.gkshoppy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PaymentWebhookController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentWebhookController.class);

    // NOTE: This is a scaffold only. For production verify the Stripe signature using the Stripe SDK and
    // your webhook signing secret (STRIPE_WEBHOOK_SECRET).
    @PostMapping(path = "/payment/webhook", consumes = "application/json")
    public ResponseEntity<?> handleWebhook(@RequestBody Map<String, Object> payload,
                                           @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        logger.info("Received payment webhook. sigHeader={}", sigHeader);
        logger.info("Payload: {}", payload);
        // TODO: verify signature and handle event types (payment_intent.succeeded, payment_intent.payment_failed, etc.)
        return ResponseEntity.ok(Map.of("status", "received"));
    }
}
