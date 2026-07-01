package com.gkshoppy.controller;

import com.gkshoppy.model.Order;
import com.gkshoppy.repository.OrderRepository;
import com.gkshoppy.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;

    public PaymentController(PaymentService paymentService, OrderRepository orderRepository) {
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;
    }

    @PostMapping(path = "/payment/create-intent", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> createIntent(@RequestParam(required = false) Long orderId, @RequestParam(required = false) Long amount) {
        try {
            String clientSecret = null;
            if (orderId != null) {
                Order o = orderRepository.findById(orderId).orElse(null);
                if (o == null) return ResponseEntity.badRequest().body(java.util.Map.of("error", "order_not_found"));
                clientSecret = paymentService.createPaymentIntent(o);
            } else if (amount != null) {
                clientSecret = paymentService.createPaymentIntentForAmount(amount);
            } else {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "missing_orderId_or_amount"));
            }
            if (clientSecret == null) return ResponseEntity.status(502).body(java.util.Map.of("error", "payment_provider_error"));
            return ResponseEntity.ok(java.util.Map.of("clientSecret", clientSecret));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(java.util.Map.of("error", "internal_error", "message", ex.getMessage()));
        }
    }

        @org.springframework.web.bind.annotation.GetMapping(path = "/payment/config", produces = "application/json")
        @ResponseBody
        public ResponseEntity<?> getConfig() {
            // Return publishable key for client-side Stripe (optional). Read from env or system property.
            String publishable = System.getenv("STRIPE_PUBLISHABLE_KEY");
            if (publishable == null || publishable.isBlank()) {
                // try spring property fallback
                publishable = System.getProperty("stripe.publishableKey", "");
            }
            return ResponseEntity.ok(java.util.Map.of("publishableKey", publishable));
        }
    }

