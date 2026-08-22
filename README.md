# Webhook Demo

## Purpose
Learning project for webhook architecture, reliability, and async decoupling using RabbitMQ.

## Current Architecture
```text
webhook-client-a/b/c -> webhook-service or webhook-worker -> RabbitMQ -> webhook-worker or webhook-service -> webhook-client-a/b/c
                          |
                          +-> PostgreSQL (subscriptions + delivery history)
```
`webhook-service` and `webhook-worker` are the same Java artifact/image running in different roles, selected by `WEBHOOK_WORKER_ENABLED` and `FAKE_EVENT_PROCESSOR_ENABLED`. The producer persists each delivery and immediately publishes a message to the queue. The worker consumes queued items and performs the actual webhook call with retry logic. PostgreSQL stores subscriptions, delivery records, and per-attempt results.

## Current Features
- Docker Compose stack with PostgreSQL, RabbitMQ, service, worker, and client apps
- PostgreSQL + Flyway + Spring Data JPA
- durable subscription and delivery persistence
- idempotent subscription registration by `callbackUrl`
- `PENDING -> SUCCESS/FAILED` delivery lifecycle
- RabbitMQ queue-based async delivery
- producer/consumer decoupling
- worker-side delivery retries with backoff
- HTTP timeout and 5xx retry handling
- per-delivery and per-attempt persistence
- dynamic `POST /subscriptions`
- event filtering by event type
- `POST /notifications` returns `202 Accepted` when the notification is accepted for async processing
- fake event processor generating webhook traffic
- client behaviors: `SUCCESS`, `RANDOM_500`, and `TIMEOUT`

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
```
Useful checks:
```bash
docker compose ps
docker compose logs -f webhook-service
docker compose logs -f webhook-worker
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
- single queue and single worker in this demo
- no dead-letter queue or retry visibility dashboard yet
- no multi-instance coordination beyond the queue abstraction
- no delivery idempotency/deduplication yet

## Next Step
This async queueing milestone is the current baseline. The next improvement would be adding richer operational tooling around backlog monitoring, retries, and delivery observability.

## Agent Handoff
- read this README before making changes
- inspect code before assuming README details are still correct
- keep scope tight; do not implement future architecture
- update README before a user-requested final commit or milestone handoff
