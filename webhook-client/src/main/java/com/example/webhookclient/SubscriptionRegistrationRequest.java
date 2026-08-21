package com.example.webhookclient;

import java.util.List;

public record SubscriptionRegistrationRequest(String callbackUrl, List<String> eventTypes) {
}
