package com.example.webhookservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;

@Service
public class NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(NotificationSender.class);

    private final RestClient restClient;
    private final List<String> webhookUrls;

    public NotificationSender(@Value("${WEBHOOK_URLS:${WEBHOOK_URL:http://localhost:8081/webhooks/notifications}}") String webhookUrlsCsv) {
        this.webhookUrls = Arrays.stream(webhookUrlsCsv.split(","))
                .map(String::trim)
                .filter(url -> !url.isEmpty())
                .toList();
        this.restClient = RestClient.builder().build();
    }

    public boolean send(NotificationRequest request) {
        boolean allDelivered = true;

        for (String webhookUrl : webhookUrls) {
            try {
                log.info("Delivering notification to client: {} | payload={}", webhookUrl, request);
                restClient.post()
                        .uri(webhookUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .toBodilessEntity();
                log.info("Delivered notification to client: {} | payload={}", webhookUrl, request);
            } catch (RestClientException ex) {
                log.error("Failed to deliver notification to {}: {}", webhookUrl, request, ex);
                allDelivered = false;
            }
        }

        return allDelivered;
    }
}
