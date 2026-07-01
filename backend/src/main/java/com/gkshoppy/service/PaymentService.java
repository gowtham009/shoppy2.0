package com.gkshoppy.service;

import com.gkshoppy.model.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Value("${stripe.apiKey:}")
    private String stripeApiKey;

    // Payment scaffold — uses Stripe REST API via HttpClient if stripe.apiKey is configured, otherwise falls back to a local stub
    public boolean processPayment(Order order) {
        // For synchronous checkout, attempt to create a payment intent and treat creation as success
        String clientSecret = createPaymentIntent(order);
        return clientSecret != null;
    }

    public String createPaymentIntentForAmount(long amountInMinorUnits) {
        // wrapper for callers who only have amount
        Order dummy = new Order();
        dummy.setTotal(amountInMinorUnits / 100.0);
        return createPaymentIntent(dummy);
    }

    public String createPaymentIntent(Order order) {
        if (stripeApiKey == null || stripeApiKey.isBlank()) {
            // stub: return a placeholder client secret
            return "stub_client_secret_for_order_" + System.currentTimeMillis();
        }

        long amount = Math.max(0, Math.round(order.getTotal() * 100)); // amount in smallest currency unit
        Map<String, String> form = Map.of(
                "amount", String.valueOf(amount),
                "currency", "inr",
                "payment_method_types[]", "card"
        );
        String body = form.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.stripe.com/v1/payment_intents"))
                .header("Authorization", "Bearer " + stripeApiKey)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            int status = resp.statusCode();
            if (status >= 200 && status < 300) {
                // attempt to parse client_secret from response
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                try {
                    com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(resp.body());
                    if (node.has("client_secret")) {
                        return node.get("client_secret").asText();
                    }
                    if (node.has("id")) {
                        return node.get("id").asText();
                    }
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    // ignore parsing issues
                    return "created";
                }
                return "created";
            } else {
                // log resp.body() in real app
                return null;
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}

