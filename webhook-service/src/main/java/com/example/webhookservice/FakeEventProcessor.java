package com.example.webhookservice;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class FakeEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(FakeEventProcessor.class);

    private final NotificationSender notificationSender;
    private final boolean enabled;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Random random = new Random();

    public FakeEventProcessor(NotificationSender notificationSender,
                              @Value("${FAKE_EVENT_PROCESSOR_ENABLED:true}") boolean enabled) {
        this.notificationSender = notificationSender;
        this.enabled = enabled;
    }

    @PostConstruct
    public void start() {
        if (!enabled) {
            log.info("Fake event processor disabled via FAKE_EVENT_PROCESSOR_ENABLED=false");
            return;
        }

        executor.submit(this::runLoop);
        log.info("Fake event processor started");
    }

    @PreDestroy
    public void stop() {
        executor.shutdownNow();
        log.info("Fake event processor stopped");
    }

    private void runLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                log.info("Waiting 5000 ms before creating next fake event batch");
                Thread.sleep(5000);

                String eventId = UUID.randomUUID().toString().substring(0, 8);
                long processingMs = randomBetween(1000, 5000);
                log.info("Starting fake event batch {} (processing time {} ms)", eventId, processingMs);
                Thread.sleep(processingMs);

                String[] eventTypes = {"EVENT_COMPLETED", "OTHER_EVENT"};

                for (String eventType : eventTypes) {
                    NotificationRequest notification = new NotificationRequest(
                            eventType,
                            "Event " + eventId + " completed for " + eventType,
                            eventId + "-" + eventType
                    );

                    boolean delivered = notificationSender.send(notification);
                    log.info("Fake event {} type {} finished and delivered={} ", eventId, eventType, delivered);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                log.error("Error in fake event processor", ex);
            }
        }
    }

    private long randomBetween(long minInclusive, long maxInclusive) {
        return minInclusive + random.nextLong(maxInclusive - minInclusive + 1);
    }
}
