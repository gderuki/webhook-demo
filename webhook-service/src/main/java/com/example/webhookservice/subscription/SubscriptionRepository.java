package com.example.webhookservice.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
    Optional<Subscription> findByCallbackUrl(String callbackUrl);

    @Query("select s from Subscription s join s.eventTypes et where et = :eventType")
    List<Subscription> findByEventType(String eventType);
}
