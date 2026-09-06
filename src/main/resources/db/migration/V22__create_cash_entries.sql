CREATE TABLE cash_entries (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('LOAN', 'CAPITAL', 'OTHER')),
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    received_on DATE NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_cash_entries_user_date ON cash_entries (user_id, received_on DESC, id DESC);
