package com.example.webhookservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
public class NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(NotificationSender.class);

    private final RestClient restClient;
    private final SubscriptionRepository subscriptionRepository;

    public NotificationSender(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.restClient = RestClient.builder().build();
    }

    public boolean send(NotificationRequest request) {
        List<Subscription> subscribers = subscriptionRepository.findByEventType(request.type());

        if (subscribers.isEmpty()) {
            log.info("No subscribers registered for event type {}", request.type());
            return true;
        }

        boolean allDelivered = true;

        for (Subscription subscription : subscribers) {
            String callbackUrl = subscription.callbackUrl();

            log.info("Delivering {} to subscription {} -> {}", request.type(), subscription.id(), callbackUrl);

            try {
                restClient.post()
                        .uri(callbackUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .toBodilessEntity();

                log.info("Delivered {} to subscription {} -> {}", request.type(), subscription.id(), callbackUrl);
            } catch (RestClientException ex) {
                log.error("Failed to deliver {} to subscription {} -> {}", request.type(), subscription.id(), callbackUrl, ex);
                allDelivered = false;
            }
        }

        return allDelivered;
    }
}
