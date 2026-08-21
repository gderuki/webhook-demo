package com.example.webhookservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(NotificationSender.class);

    private final RestClient restClient;
    private final SubscriptionRepository subscriptionRepository;

    public NotificationSender(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(2000);
        requestFactory.setReadTimeout(2000);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public boolean send(NotificationRequest request) {
        NotificationRequest normalized = normalizeEventId(request);
        List<Subscription> subscribers = subscriptionRepository.findByEventType(normalized.type());

        if (subscribers.isEmpty()) {
            log.info("No subscribers registered for event type {}", normalized.type());
            return true;
        }

        boolean allDelivered = true;

        for (Subscription subscription : subscribers) {
            String callbackUrl = subscription.callbackUrl();
            long start = System.nanoTime();

            try {
                restClient.post()
                        .uri(callbackUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(normalized)
                        .retrieve()
                        .toBodilessEntity();

                long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                log.info("Delivery event {} subscription {} callback {} result=SUCCESS duration={} ms",
                        normalized.eventId(), subscription.id(), callbackUrl, durationMs);
            } catch (RestClientException ex) {
                long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                String result = resultLabel(ex);
                log.error("Delivery event {} subscription {} callback {} result={} duration={} ms error={}",
                        normalized.eventId(), subscription.id(), callbackUrl, result, durationMs, ex.getMessage());
                allDelivered = false;
            }
        }

        return allDelivered;
    }

    private NotificationRequest normalizeEventId(NotificationRequest request) {
        if (request == null) {
            return new NotificationRequest("UNKNOWN", "", UUID.randomUUID().toString());
        }

        if (request.eventId() == null || request.eventId().isBlank()) {
            return new NotificationRequest(request.type(), request.message(), UUID.randomUUID().toString());
        }

        return request;
    }

    private String resultLabel(RestClientException ex) {
        if (ex instanceof ResourceAccessException) {
            return "TIMEOUT";
        }
        if (ex instanceof HttpStatusCodeException httpEx) {
            return "HTTP " + httpEx.getStatusCode().value();
        }
        return "ERROR";
    }
}
