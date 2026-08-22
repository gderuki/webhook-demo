package com.example.webhookservice.subscription;

import java.util.List;

public record SubscriptionRequest(String callbackUrl, List<String> eventTypes) {
}
