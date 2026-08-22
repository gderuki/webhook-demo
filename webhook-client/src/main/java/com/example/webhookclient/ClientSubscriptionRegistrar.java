package com.example.webhookclient;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class ClientSubscriptionRegistrar {

    private static final Logger log = LoggerFactory.getLogger(ClientSubscriptionRegistrar.class);

    private final RestClient restClient;
    private final String callbackUrl;
    private final String webhookServiceUrl;
    private final String clientName;
    private final List<String> eventTypes;
    private final int maxAttempts;
    private final long retryDelayMs;

    public ClientSubscriptionRegistrar(
            @Value("${CALLBACK_URL:}") String callbackUrl,
            @Value("${WEBHOOK_SERVICE_URL:}") String webhookServiceUrl,
            @Value("${CLIENT_NAME:client}") String clientName,
            @Value("${EVENT_TYPES:EVENT_COMPLETED}") String eventTypesCsv,
            @Value("${REGISTRATION_RETRY_MAX_ATTEMPTS:10}") int maxAttempts,
            @Value("${REGISTRATION_RETRY_DELAY_MS:1000}") long retryDelayMs
    ) {
        this.restClient = RestClient.builder().build();
        this.callbackUrl = callbackUrl;
        this.webhookServiceUrl = webhookServiceUrl;
        this.clientName = clientName;
        this.maxAttempts = maxAttempts;
        this.retryDelayMs = retryDelayMs;
        this.eventTypes = java.util.Arrays.stream(eventTypesCsv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    @PostConstruct
    public void register() {
        if (callbackUrl == null || callbackUrl.isBlank() || webhookServiceUrl == null || webhookServiceUrl.isBlank()) {
            log.warn("[{}] Missing CALLBACK_URL or WEBHOOK_SERVICE_URL, skipping subscription registration", clientName);
            return;
        }

        SubscriptionRegistrationRequest request = new SubscriptionRegistrationRequest(
                callbackUrl,
                eventTypes
        );

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("[{}] Registering callback {} with service {} for events {} (attempt {}/{})",
                        clientName, callbackUrl, webhookServiceUrl, eventTypes, attempt, maxAttempts);
                restClient.post()
                        .uri(webhookServiceUrl + "/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .toBodilessEntity();
                log.info("[{}] Registered callback {} with service {} for events {}",
                        clientName, callbackUrl, webhookServiceUrl, eventTypes);
                return;
            } catch (RestClientException ex) {
                if (attempt == maxAttempts) {
                    log.error("[{}] Failed to register callback {} with service {} after {} attempts",
                            clientName, callbackUrl, webhookServiceUrl, maxAttempts, ex);
                    return;
                }

                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
