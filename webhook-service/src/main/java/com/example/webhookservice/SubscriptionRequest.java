package com.example.webhookservice;

import java.util.List;

public record SubscriptionRequest(String callbackUrl, List<String> eventTypes) {
}
