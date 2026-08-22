package com.example.webhookservice.messaging;

import com.example.webhookservice.api.NotificationRequest;
import com.example.webhookservice.delivery.Delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DeliveryProducer {
    private static final Logger log = LoggerFactory.getLogger(DeliveryProducer.class);
    private final RabbitTemplate rabbitTemplate;
    private final String queueName;

    public DeliveryProducer(RabbitTemplate rabbitTemplate, @Value("${webhook.delivery.queue:webhook-deliveries}") String queueName) {
        this.rabbitTemplate = rabbitTemplate;
        this.queueName = queueName;
    }

    public void publish(Delivery delivery, NotificationRequest request) {
        DeliveryMessage message = new DeliveryMessage(delivery.id(), delivery.eventId(), delivery.eventType(), delivery.subscription().id(), delivery.subscription().callbackUrl(), request.message());
        rabbitTemplate.convertAndSend(queueName, message);
        log.info("producer event={} subscription={} deliveryId={} queue={} callback={} published", request.eventId(), delivery.subscription().id(), delivery.id(), queueName, delivery.subscription().callbackUrl());
    }
}
