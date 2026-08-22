package com.example.webhookservice;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "subscription",
        indexes = {
                @Index(name = "idx_subscription_callback_url", columnList = "callback_url")
        }
)
public class Subscription {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "callback_url", nullable = false, length = 2048)
    private String callbackUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "subscription_event_type",
            joinColumns = @JoinColumn(
                    name = "subscription_id",
                    nullable = false,
                    foreignKey = @ForeignKey(name = "fk_subscription_event_type_subscription")
            )
    )
    @OrderColumn(name = "sort_order")
    @Column(name = "event_type", nullable = false, length = 128)
    private List<String> eventTypes = new ArrayList<>();

    protected Subscription() {
    }

    public Subscription(String callbackUrl, List<String> eventTypes) {
        this.id = UUID.randomUUID().toString();
        this.callbackUrl = callbackUrl;
        this.eventTypes = eventTypes == null ? new ArrayList<>() : new ArrayList<>(eventTypes);
    }

    public static Subscription create(String callbackUrl, List<String> eventTypes) {
        return new Subscription(callbackUrl, eventTypes);
    }

    public String id() {
        return id;
    }

    public String callbackUrl() {
        return callbackUrl;
    }

    public List<String> eventTypes() {
        return List.copyOf(eventTypes);
    }
}
