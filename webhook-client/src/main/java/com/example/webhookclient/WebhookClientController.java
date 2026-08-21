package com.example.webhookclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebhookClientController {

    private static final Logger log = LoggerFactory.getLogger(WebhookClientController.class);

    @PostMapping("/webhooks/notifications")
    public ResponseEntity<Void> receiveWebhook(@RequestBody NotificationRequest request) {
        log.info("Webhook received: type={}, message={}", request.type(), request.message());
        return ResponseEntity.ok().build();
    }
}
