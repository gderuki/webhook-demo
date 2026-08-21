package com.example.webhookservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private final int maxAttempts;
    private final long retryDelayMs;
    private final long retryBackoffMs;

    public NotificationSender(
            SubscriptionRepository subscriptionRepository,
            @Value("${WEBHOOK_HTTP_TIMEOUT_MS:2000}") int httpTimeoutMs,
            @Value("${WEBHOOK_RETRY_MAX_ATTEMPTS:3}") int maxAttempts,
            @Value("${WEBHOOK_RETRY_INITIAL_DELAY_MS:500}") long retryDelayMs,
            @Value("${WEBHOOK_RETRY_BACKOFF_MS:1000}") long retryBackoffMs
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.maxAttempts = maxAttempts;
        this.retryDelayMs = retryDelayMs;
        this.retryBackoffMs = retryBackoffMs;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(httpTimeoutMs);
        requestFactory.setReadTimeout(httpTimeoutMs);

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
            boolean delivered = sendWithRetry(normalized, subscription);
            if (!delivered) {
                allDelivered = false;
            }
        }

        return allDelivered;
    }

    private boolean sendWithRetry(NotificationRequest request, Subscription subscription) {
        String callbackUrl = subscription.callbackUrl();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            long start = System.nanoTime();

            try {
                restClient.post()
                        .uri(callbackUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .toBodilessEntity();

                long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                log.info("event={} subscription={} attempt={} result=SUCCESS duration={} ms callback={}",
                        request.eventId(), subscription.id(), attempt, durationMs, callbackUrl);
                return true;
            } catch (RestClientException ex) {
                long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                String result = resultLabel(ex);

                log.info("event={} subscription={} attempt={} result={} duration={} ms callback={}",
                        request.eventId(), subscription.id(), attempt, result, durationMs, callbackUrl);

                if (!shouldRetry(ex) || attempt >= maxAttempts) {
                    log.error("event={} subscription={} attempt={} result={} duration={} ms callback={} final_failure={}",
                            request.eventId(), subscription.id(), attempt, result, durationMs, callbackUrl, ex.getMessage());
                    return false;
                }

                long sleepMs = (attempt == 1) ? retryDelayMs : retryBackoffMs;
                log.warn("event={} subscription={} attempt={} result={} duration={} ms retrying in {} ms callback={}",
                        request.eventId(), subscription.id(), attempt, result, durationMs, sleepMs, callbackUrl);

                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }

        return false;
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

    private boolean shouldRetry(RestClientException ex) {
        if (ex instanceof ResourceAccessException) {
            return true;
        }
        if (ex instanceof HttpStatusCodeException httpEx) {
            return httpEx.getStatusCode().is5xxServerError();
        }
        return false;
    }

    private String resultLabel(RestClientException ex) {
        if (ex instanceof ResourceAccessException) {
            return "TIMEOUT";
        }
        if (ex instanceof HttpStatusCodeException httpEx) {
            return "HTTP_" + httpEx.getStatusCode().value();
        }
        return "ERROR";
    }
}
