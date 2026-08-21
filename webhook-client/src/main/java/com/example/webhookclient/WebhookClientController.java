package com.example.webhookclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
public class WebhookClientController {

    private static final Logger log = LoggerFactory.getLogger(WebhookClientController.class);

    private final String clientName;
    private final String clientBehavior;

    public WebhookClientController(
            @Value("${CLIENT_NAME:client}") String clientName,
            @Value("${CLIENT_BEHAVIOR:SUCCESS}") String clientBehavior
    ) {
        this.clientName = clientName;
        this.clientBehavior = clientBehavior;
    }

    @PostMapping("/webhooks/notifications")
    public ResponseEntity<Void> receiveWebhook(@RequestBody NotificationRequest request) throws InterruptedException {
        log.info("[{}] Webhook received: eventId={}, type={}, message={}", clientName, request.eventId(), request.type(), request.message());

        switch (clientBehavior.toUpperCase()) {
            case "SUCCESS" -> {
                return ResponseEntity.ok().build();
            }
            case "RANDOM_500" -> {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    log.warn("[{}] Simulating RANDOM_500 response", clientName);
                    return ResponseEntity.status(500).build();
                }
                return ResponseEntity.ok().build();
            }
            case "TIMEOUT" -> {
                log.warn("[{}] Simulating TIMEOUT response", clientName);
                Thread.sleep(5000);
                return ResponseEntity.ok().build();
            }
            default -> {
                return ResponseEntity.ok().build();
            }
        }
    }
}
