package com.example.webhookservice.api;

public record NotificationRequest(String type, String message, String eventId) {
    public NotificationRequest(String type, String message) {
        this(type, message, null);
    }
}
