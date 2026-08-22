WITH duplicate_subscription AS (
    SELECT id
    FROM (
        SELECT id,
               ROW_NUMBER() OVER (PARTITION BY callback_url ORDER BY id) AS row_num
        FROM subscription
    ) ranked
    WHERE row_num > 1
)
DELETE FROM subscription
WHERE id IN (SELECT id FROM duplicate_subscription);

CREATE UNIQUE INDEX IF NOT EXISTS uq_subscription_callback_url
    ON subscription (callback_url);

ALTER TABLE delivery DROP CONSTRAINT IF EXISTS delivery_status_check;
ALTER TABLE delivery
    ADD CONSTRAINT chk_delivery_status
    CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED'));
