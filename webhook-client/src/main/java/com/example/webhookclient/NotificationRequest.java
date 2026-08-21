package com.example.webhookclient;

public record NotificationRequest(String type, String message, String eventId) {

    public NotificationRequest(String type, String message) {
        this(type, message, null);
    }
}
