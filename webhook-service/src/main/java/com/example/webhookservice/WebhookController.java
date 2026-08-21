package com.example.webhookservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@RestController
public class WebhookController {

    private final RestClient restClient;
    private final String webhookUrl;

    public WebhookController(@Value("${WEBHOOK_URL:http://localhost:8081/webhooks/notifications}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.restClient = RestClient.builder().build();
    }

    @PostMapping("/notifications")
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest request) {
        try {
            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            return ResponseEntity.ok().build();
        } catch (RestClientException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
