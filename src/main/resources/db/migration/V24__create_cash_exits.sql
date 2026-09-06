CREATE TABLE cash_exits (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    paid_on DATE NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_cash_exits_user_date ON cash_exits (user_id, paid_on DESC, id DESC);

CREATE FUNCTION record_cash_exit_history() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO operation_history (user_id, type, title, entity_id, amount)
    VALUES (NEW.user_id, 'CASH_EXIT', NEW.title, NEW.id, NEW.amount);
    RETURN NULL;
END;
$$;

CREATE TRIGGER history_cash_exits AFTER INSERT ON cash_exits
FOR EACH ROW EXECUTE FUNCTION record_cash_exit_history();
