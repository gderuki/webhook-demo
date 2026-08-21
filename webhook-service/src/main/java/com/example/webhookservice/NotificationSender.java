package com.example.webhookservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(NotificationSender.class);

    private final RestClient restClient;
    private final String webhookUrl;

    public NotificationSender(@Value("${WEBHOOK_URL:http://localhost:8081/webhooks/notifications}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.restClient = RestClient.builder().build();
    }

    public boolean send(NotificationRequest request) {
        try {
            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException ex) {
            log.error("Failed to deliver notification: {}", request, ex);
            return false;
        }
    }
}
