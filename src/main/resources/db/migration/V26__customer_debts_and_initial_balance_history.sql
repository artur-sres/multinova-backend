CREATE TABLE customer_debts (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    amount NUMERIC(10,2) NOT NULL CHECK (amount > 0),
    incurred_on DATE NOT NULL,
    description VARCHAR(1000) NOT NULL CHECK (length(trim(description)) > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_customer_debts_customer ON customer_debts (customer_id, created_at DESC);

CREATE FUNCTION record_customer_debt() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    owner_id BIGINT;
    customer_name TEXT;
BEGIN
    UPDATE customers
    SET debt_balance = COALESCE(debt_balance, 0) + NEW.amount, updated_at = clock_timestamp()
    WHERE id = NEW.customer_id
    RETURNING user_id, name INTO owner_id, customer_name;

    INSERT INTO operation_history (user_id, type, title, entity_id, amount)
    VALUES (owner_id, 'CUSTOMER_DEBT', customer_name || ' — ' || NEW.description
        || ' (data do débito: ' || to_char(NEW.incurred_on, 'DD/MM/YYYY') || ')', NEW.customer_id, NEW.amount);
    RETURN NULL;
END;
$$;
CREATE TRIGGER customer_debt_recorded AFTER INSERT ON customer_debts
FOR EACH ROW EXECUTE FUNCTION record_customer_debt();

CREATE FUNCTION record_customer_initial_debt() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.debt_balance > 0 THEN
        INSERT INTO operation_history (user_id, type, title, entity_id, amount)
        VALUES (NEW.user_id, 'CUSTOMER_INITIAL_DEBT', NEW.name || ' — Saldo devedor inicial / migração', NEW.id, NEW.debt_balance);
    END IF;
    RETURN NULL;
END;
$$;
CREATE TRIGGER customer_initial_debt_recorded AFTER INSERT ON customers
FOR EACH ROW EXECUTE FUNCTION record_customer_initial_debt();
