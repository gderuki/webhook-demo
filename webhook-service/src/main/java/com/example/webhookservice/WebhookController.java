package com.example.webhookservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebhookController {

    private final NotificationSender notificationSender;

    public WebhookController(NotificationSender notificationSender) {
        this.notificationSender = notificationSender;
    }

    @PostMapping("/notifications")
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest request) {
        return notificationSender.send(request)
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
