package com.example.webhookservice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(
        name = "delivery",
        indexes = {
                @Index(name = "idx_delivery_event_subscription", columnList = "event_id, subscription_id"),
                @Index(name = "idx_delivery_status", columnList = "status")
        }
)
public class Delivery {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "event_id", nullable = false, length = 128)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "subscription_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_delivery_subscription")
    )
    private Subscription subscription;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    protected Delivery() {
    }

    public Delivery(String eventId, String eventType, Subscription subscription, String status) {
        this.id = UUID.randomUUID().toString();
        this.eventId = eventId;
        this.eventType = eventType;
        this.subscription = subscription;
        this.status = status;
    }

    public String id() {
        return id;
    }

    public String eventId() {
        return eventId;
    }

    public String eventType() {
        return eventType;
    }

    public String status() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Subscription subscription() {
        return subscription;
    }
}
