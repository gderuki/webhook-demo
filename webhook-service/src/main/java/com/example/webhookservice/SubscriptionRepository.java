package com.example.webhookservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    @Query("select s from Subscription s join s.eventTypes et where et = :eventType")
    List<Subscription> findByEventType(String eventType);
}
