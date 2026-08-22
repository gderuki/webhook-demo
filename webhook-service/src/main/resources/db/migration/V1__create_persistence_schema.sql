CREATE TABLE subscription (
    id VARCHAR(36) PRIMARY KEY,
    callback_url VARCHAR(2048) NOT NULL
);

CREATE TABLE subscription_event_type (
    subscription_id VARCHAR(36) NOT NULL,
    sort_order INT NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    PRIMARY KEY (subscription_id, sort_order),
    CONSTRAINT fk_subscription_event_type_subscription
        FOREIGN KEY (subscription_id) REFERENCES subscription(id) ON DELETE CASCADE
);

CREATE INDEX idx_subscription_event_type_event_type
    ON subscription_event_type (event_type);

CREATE TABLE delivery (
    id VARCHAR(36) PRIMARY KEY,
    event_id VARCHAR(128) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    subscription_id VARCHAR(36) NOT NULL,
    status VARCHAR(16) NOT NULL CHECK (status IN ('SUCCESS', 'FAILED')),
    CONSTRAINT fk_delivery_subscription
        FOREIGN KEY (subscription_id) REFERENCES subscription(id) ON DELETE CASCADE
);

CREATE INDEX idx_delivery_event_subscription
    ON delivery (event_id, subscription_id);

CREATE INDEX idx_delivery_status
    ON delivery (status);

CREATE TABLE delivery_attempt (
    id VARCHAR(36) PRIMARY KEY,
    delivery_id VARCHAR(36) NOT NULL,
    attempt_number INT NOT NULL CHECK (attempt_number > 0),
    result VARCHAR(64) NOT NULL,
    duration BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_delivery_attempt_delivery
        FOREIGN KEY (delivery_id) REFERENCES delivery(id) ON DELETE CASCADE
);

CREATE INDEX idx_delivery_attempt_delivery
    ON delivery_attempt (delivery_id, attempt_number);
