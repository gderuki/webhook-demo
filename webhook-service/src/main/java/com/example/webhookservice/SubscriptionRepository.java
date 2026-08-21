package com.example.webhookservice;

import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class SubscriptionRepository {

    private final ConcurrentMap<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    public Subscription save(Subscription subscription) {
        subscriptions.put(subscription.id(), subscription);
        return subscription;
    }

    public List<Subscription> findAll() {
        return subscriptions.values().stream()
                .sorted(Comparator.comparing(Subscription::id))
                .toList();
    }

    public List<Subscription> findByEventType(String eventType) {
        return subscriptions.values().stream()
                .filter(subscription -> subscription.eventTypes().contains(eventType))
                .toList();
    }
}
