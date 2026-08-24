# Webhook Demo

## Purpose
Learning project for webhook architecture, reliability, and async decoupling using RabbitMQ.

## Current Architecture
```text
webhook-client-a/b/c -> webhook-service -> RabbitMQ -> webhook-worker x N -> webhook-client-a/b/c
                          |
                          +-> PostgreSQL (subscriptions + delivery history)
```
`webhook-service` and `webhook-worker` are the same Java artifact/image running in different roles, selected by `WEBHOOK_WORKER_ENABLED` and `FAKE_EVENT_PROCESSOR_ENABLED`. The producer persists each delivery and publishes a message to the shared queue. Multiple `webhook-worker` instances compete for the same `webhook-deliveries` queue, and each worker performs the actual callback with the existing retry logic. PostgreSQL stores subscriptions, delivery records, and per-attempt results.

## Current Features
- Docker Compose stack with PostgreSQL, RabbitMQ, service, worker, and client apps
- PostgreSQL + Flyway + Spring Data JPA
- durable subscription and delivery persistence
- idempotent subscription registration by `callbackUrl`
- `PENDING -> SUCCESS/FAILED` delivery lifecycle
- RabbitMQ queue-based async delivery
- producer/consumer decoupling
- multiple competing consumers on the same queue
- worker-side delivery retries with backoff
- HTTP timeout and 5xx retry handling
- per-delivery and per-attempt persistence
- dynamic `POST /subscriptions`
- event filtering by event type
- `POST /notifications` returns `202 Accepted` when the notification is accepted for async processing
- fake event processor generating webhook traffic
- client behaviors: `SUCCESS`, `RANDOM_500`, and `TIMEOUT`
- worker identity visible in logs via runtime hostname

## Package Layout
```text
com.example.webhookservice
├── api/
│   ├── WebhookController
│   └── NotificationRequest
├── subscription/
│   ├── Subscription
│   ├── SubscriptionRequest
│   └── SubscriptionRepository
├── delivery/
│   ├── Delivery
│   ├── DeliveryAttempt
│   ├── DeliveryRepository
│   ├── DeliveryAttemptRepository
│   └── NotificationSender
├── messaging/
│   ├── DeliveryMessage
│   ├── DeliveryProducer
│   ├── DeliveryWorker
│   └── DeliveryQueueConfig
├── simulation/
│   └── FakeEventProcessor
└── WebhookServiceApplication
```

## Design Notes
- the service does not wait for downstream webhook completion when publishing to RabbitMQ
- the worker is responsible for the actual callback and retry loop
- queue buffering lets the producer keep moving while a slow client is still retrying
- delivery status and attempt history remain stored in PostgreSQL for inspection
- `callbackUrl` is treated as the subscription identity for the demo
- new delivery rows start as `PENDING` and are finalized by the worker

## Run
```bash
docker compose up --build -d
docker compose up -d --scale webhook-worker=3
```
Useful checks:
```bash
docker compose ps
docker compose logs -f webhook-service
docker compose logs -f webhook-worker
docker exec webhook-demo-rabbitmq-1 rabbitmqctl list_consumers
curl http://localhost:8080/subscriptions
```
RabbitMQ management is available at:
```text
http://localhost:15672
guest / guest
```

## Useful API
- `POST /subscriptions` — register callback URL + event types
- `GET /subscriptions` — list current subscriptions
- `POST /notifications` — trigger webhook delivery

Example:
```json
{"callbackUrl":"http://host.docker.internal:18081/webhooks/notifications","eventTypes":["EVENT_COMPLETED"]}
```

## Current Limitations
- one shared queue with multiple competing consumers in the current scaling milestone
- no dead-letter queue or retry visibility dashboard yet
- no delivery idempotency/deduplication yet
- no broader orchestration beyond the queue-based async pattern

## Next Step
This async queueing milestone is the current baseline. The current scope stays focused on queue-based scaling and runtime proof; larger operational improvements like backlog dashboards and smarter retry tooling remain future work.

## Agent Handoff
- read this README before making changes
- inspect code before assuming README details are still correct
- keep scope tight; do not implement future architecture
- update README before a user-requested final commit or milestone handoff
