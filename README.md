# Webhook Demo

## Purpose
Learning project for webhook architecture, reliability, and system-design tradeoffs.

## Current Architecture
```text
webhook-client-a/b/c -> webhook-service -> in-memory subscriptions + event filter + sequential retry delivery
```
Service keeps subscriptions in memory, matches event types, and sends callbacks one by one. Clients simulate `SUCCESS`, `RANDOM_500`, and `TIMEOUT` behaviors.

## Current Features
- Docker Compose setup
- webhook service + multiple clients from same image
- dynamic `POST /subscriptions`
- callback URL + event type subscriptions
- in-memory subscription storage
- event filtering
- configurable client behavior (`SUCCESS`, `RANDOM_500`, `TIMEOUT`)
- synchronous sequential delivery
- HTTP timeout
- retry with limited attempts and backoff
- fake event processor

## Design Notes
- subscriptions are in-memory only
- delivery is intentionally synchronous/sequential
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
- subscriptions disappear after restart
- no shared state across service instances
- failed delivery state exists only during processing/logging
- no durable messaging or persistence yet

## Next Step
Add PostgreSQL persistence so subscriptions and delivery state survive restarts and can later support multiple service instances.

## Agent Handoff
- read this README before making changes
- inspect code before assuming README details are still correct
- keep scope tight; do not implement future architecture
- update README only when the user asks to finalize/commit or behavior materially changes
