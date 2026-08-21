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

    public ClientSubscriptionRegistrar(
            @Value("${CALLBACK_URL:}") String callbackUrl,
            @Value("${WEBHOOK_SERVICE_URL:}") String webhookServiceUrl,
            @Value("${CLIENT_NAME:client}") String clientName,
            @Value("${EVENT_TYPES:EVENT_COMPLETED}") String eventTypesCsv
    ) {
        this.restClient = RestClient.builder().build();
        this.callbackUrl = callbackUrl;
        this.webhookServiceUrl = webhookServiceUrl;
        this.clientName = clientName;
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

        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        SubscriptionRegistrationRequest request = new SubscriptionRegistrationRequest(
                callbackUrl,
                eventTypes
        );

        try {
            log.info("[{}] Registering callback {} with service {} for events {}", clientName, callbackUrl, webhookServiceUrl, eventTypes);
            restClient.post()
                    .uri(webhookServiceUrl + "/subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("[{}] Registered callback {} with service {} for events {}", clientName, callbackUrl, webhookServiceUrl, eventTypes);
        } catch (RestClientException ex) {
            log.error("[{}] Failed to register callback {} with service {}", clientName, callbackUrl, webhookServiceUrl, ex);
        }
    }
}
