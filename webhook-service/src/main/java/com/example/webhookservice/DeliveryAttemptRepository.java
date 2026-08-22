package com.example.webhookservice;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, String> {
}
