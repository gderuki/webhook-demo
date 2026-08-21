package com.example.webhookservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WebhookController {

    private final NotificationSender notificationSender;
    private final SubscriptionRepository subscriptionRepository;

    public WebhookController(NotificationSender notificationSender, SubscriptionRepository subscriptionRepository) {
        this.notificationSender = notificationSender;
        this.subscriptionRepository = subscriptionRepository;
    }

    @PostMapping("/notifications")
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest request) {
        return notificationSender.send(request)
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<Subscription> createSubscription(@RequestBody SubscriptionRequest request) {
        Subscription subscription = Subscription.create(request.callbackUrl(), request.eventTypes());
        return ResponseEntity.ok(subscriptionRepository.save(subscription));
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<Subscription>> getSubscriptions() {
        return ResponseEntity.ok(subscriptionRepository.findAll());
    }
}
