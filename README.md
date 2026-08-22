# Webhook Demo

## Purpose
Learning project for webhook architecture, reliability, and system-design tradeoffs.

## Current Architecture
```text
webhook-client-a/b/c -> webhook-service -> PostgreSQL (subscriptions + delivery history)
                                 \-> event filtering + sequential retry delivery
```
Service matches subscriptions to event types, delivers callbacks one by one, and stores durable subscription + delivery state in PostgreSQL. Clients simulate `SUCCESS`, `RANDOM_500`, and `TIMEOUT` behaviors.

## Current Features
- Docker Compose setup
- PostgreSQL + Flyway + Spring Data JPA
- webhook service + multiple clients from same image
- dynamic `POST /subscriptions`
- callback URL + event type subscriptions
- persistent subscription storage
- event filtering
- configurable client behavior (`SUCCESS`, `RANDOM_500`, `TIMEOUT`)
- synchronous sequential delivery
- HTTP timeout
- retry with limited attempts and backoff
- per-delivery and per-attempt persistence
- fake event processor

## Design Notes
- subscriptions are persisted in PostgreSQL
- delivery rows and retry attempts are stored for each event/subscription pair
- delivery remains intentionally synchronous and sequential
- clients intentionally simulate failures
- Docker Compose configures startup; subscriptions are registered via the runtime API

## Run
```bash
docker compose up --build -d
```
Inspect:
```bash
docker compose ps
docker compose logs -f webhook-service
curl http://localhost:8080/subscriptions
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
- state is still local to a single Postgres instance
- no durable messaging beyond the current DB-backed flow
- no shared state across multiple service instances yet
- no idempotency or DLQ yet

## Next Step
Use the persisted delivery state as the base for later operational improvements, such as multi-instance awareness and richer delivery visibility.

## Agent Handoff
- read this README before making changes
- inspect code before assuming README details are still correct
- keep scope tight; do not implement future architecture
- update README when behavior materially changes or before finalizing a milestone
