package com.example.webhookservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ConditionalOnProperty(name = "webhook.worker.enabled", havingValue = "true")
public class DeliveryWorker {

    private static final Logger log = LoggerFactory.getLogger(DeliveryWorker.class);

    private final DeliveryRepository deliveryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationSender notificationSender;

    public DeliveryWorker(
            DeliveryRepository deliveryRepository,
            SubscriptionRepository subscriptionRepository,
            NotificationSender notificationSender
    ) {
        this.deliveryRepository = deliveryRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.notificationSender = notificationSender;
    }

    @RabbitListener(queues = "${webhook.delivery.queue:webhook-deliveries}")
    public void handle(DeliveryMessage message) {
        Optional<Delivery> deliveryOpt = deliveryRepository.findById(message.deliveryId());
        if (deliveryOpt.isEmpty()) {
            log.warn("worker missing deliveryId={} event={} callback={}", message.deliveryId(), message.eventId(), message.callbackUrl());
            return;
        }

        Delivery delivery = deliveryOpt.get();
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findById(message.subscriptionId());
        if (subscriptionOpt.isEmpty()) {
            log.warn("worker missing subscriptionId={} event={} deliveryId={}", message.subscriptionId(), message.eventId(), message.deliveryId());
            return;
        }

        NotificationRequest request = new NotificationRequest(message.eventType(), message.message(), message.eventId());
        log.info("worker event={} subscription={} deliveryId={} callback={} started",
                message.eventId(), subscriptionOpt.get().id(), delivery.id(), subscriptionOpt.get().callbackUrl());

        boolean delivered = notificationSender.processDelivery(request, subscriptionOpt.get(), delivery);
        log.info("worker event={} subscription={} deliveryId={} result={} finished",
                message.eventId(), subscriptionOpt.get().id(), delivery.id(), delivered ? "SUCCESS" : "FAILED");
    }
}
