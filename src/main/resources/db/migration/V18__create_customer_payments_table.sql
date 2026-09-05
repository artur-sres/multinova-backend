CREATE TABLE customer_payments (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    paid_at TIMESTAMP NOT NULL,
    amount NUMERIC(10,2) NOT NULL CHECK (amount > 0)
);

CREATE INDEX idx_customer_payments_customer_paid_at
    ON customer_payments (customer_id, paid_at DESC, id DESC);
