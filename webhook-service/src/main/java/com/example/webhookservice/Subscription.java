package com.example.webhookservice;

import java.util.List;
import java.util.UUID;

public record Subscription(String id, String callbackUrl, List<String> eventTypes) {

    public static Subscription create(String callbackUrl, List<String> eventTypes) {
        return new Subscription(
                UUID.randomUUID().toString(),
                callbackUrl,
                eventTypes == null ? List.of() : List.copyOf(eventTypes)
        );
    }
}
