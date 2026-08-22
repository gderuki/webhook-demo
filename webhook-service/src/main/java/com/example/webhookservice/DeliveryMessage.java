package com.example.webhookservice;

import java.io.Serializable;

public record DeliveryMessage(
        String deliveryId,
        String eventId,
        String eventType,
        String subscriptionId,
        String callbackUrl,
        String message
) implements Serializable {
}
