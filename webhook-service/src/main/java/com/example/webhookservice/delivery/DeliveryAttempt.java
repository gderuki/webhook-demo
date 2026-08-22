package com.example.webhookservice.delivery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_attempt", indexes = {
        @Index(name = "idx_delivery_attempt_delivery", columnList = "delivery_id, attempt_number")
})
public class DeliveryAttempt {
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_id", nullable = false, foreignKey = @ForeignKey(name = "fk_delivery_attempt_delivery"))
    private Delivery delivery;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Column(name = "result", nullable = false, length = 64)
    private String result;

    @Column(name = "duration", nullable = false)
    private long duration;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected DeliveryAttempt() {}

    public DeliveryAttempt(Delivery delivery, int attemptNumber, String result, long duration, Instant createdAt) {
        this.id = UUID.randomUUID().toString();
        this.delivery = delivery;
        this.attemptNumber = attemptNumber;
        this.result = result;
        this.duration = duration;
        this.createdAt = createdAt;
    }

    public String id() { return id; }
    public Delivery delivery() { return delivery; }
    public int attemptNumber() { return attemptNumber; }
    public String result() { return result; }
    public long duration() { return duration; }
    public Instant createdAt() { return createdAt; }
}
