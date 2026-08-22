package com.example.webhookservice.api;

import com.example.webhookservice.delivery.NotificationSender;
import com.example.webhookservice.subscription.Subscription;
import com.example.webhookservice.subscription.SubscriptionRepository;
import com.example.webhookservice.subscription.SubscriptionRequest;
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
        boolean accepted = notificationSender.send(request);
        return accepted
                ? ResponseEntity.accepted().build()
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<Subscription> createSubscription(@RequestBody SubscriptionRequest request) {
        // Normal sequential retries/restarts are idempotent via this find-or-create flow.
        // The database unique constraint on callback_url still prevents duplicate rows if concurrent
        // requests race, while preserving the current demo behavior without redesigning registration.
        Subscription existing = subscriptionRepository.findByCallbackUrl(request.callbackUrl()).orElse(null);
        if (existing != null) {
            existing.updateEventTypes(request.eventTypes());
            return ResponseEntity.ok(subscriptionRepository.save(existing));
        }

        Subscription subscription = Subscription.create(request.callbackUrl(), request.eventTypes());
        return ResponseEntity.ok(subscriptionRepository.save(subscription));
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<Subscription>> getSubscriptions() {
        return ResponseEntity.ok(subscriptionRepository.findAll());
    }
}
