package com.example.webhookclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebhookClientController {

    private static final Logger log = LoggerFactory.getLogger(WebhookClientController.class);

    private final String clientName;

    public WebhookClientController(@Value("${CLIENT_NAME:client}") String clientName) {
        this.clientName = clientName;
    }

    @PostMapping("/webhooks/notifications")
    public ResponseEntity<Void> receiveWebhook(@RequestBody NotificationRequest request) {
        log.info("[{}] Webhook received: type={}, message={}", clientName, request.type(), request.message());
        return ResponseEntity.ok().build();
    }
}
