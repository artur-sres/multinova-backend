ALTER TABLE customers ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE TABLE customer_ledger (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    type VARCHAR(40) NOT NULL,
    source_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    delta NUMERIC(12,2) NOT NULL,
    effective_at TIMESTAMP NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT clock_timestamp(),
    recovered BOOLEAN NOT NULL DEFAULT FALSE,
    reversed_entry_id BIGINT UNIQUE REFERENCES customer_ledger(id),
    UNIQUE (type, source_id)
);
CREATE INDEX idx_ledger_customer_time ON customer_ledger(customer_id, recorded_at, id);

INSERT INTO customer_ledger(customer_id, type, source_id, description, delta, effective_at, recorded_at, recovered)
SELECT customer_id, 'SALE', id, 'Venda #' || id, total_amount, sale_date, sale_date, TRUE FROM sales;
INSERT INTO customer_ledger(customer_id, type, source_id, description, delta, effective_at, recorded_at, recovered)
SELECT customer_id, 'SALE_CANCELLED', id, 'Cancelamento da venda #' || id, -total_amount, updated_at, updated_at, TRUE FROM sales WHERE status = 'CANCELLED';
INSERT INTO customer_ledger(customer_id, type, source_id, description, delta, effective_at, recorded_at, recovered)
SELECT customer_id, 'PAYMENT', id, 'Pagamento #' || id, -amount, paid_at, paid_at, TRUE FROM customer_payments;
INSERT INTO customer_ledger(customer_id, type, source_id, description, delta, effective_at, recorded_at, recovered)
SELECT customer_id, 'DEBT', id, description, amount, incurred_on, created_at, TRUE FROM customer_debts;
-- Historical opening balances cannot be reconstructed from today's balance alone.
-- Reconcile the known movements and explicitly identify the remainder as recovered.
INSERT INTO customer_ledger(customer_id, type, source_id, description, delta, effective_at, recorded_at, recovered)
SELECT c.id, 'INITIAL', c.id, 'Saldo anterior reconciliado', COALESCE(c.debt_balance, 0) - COALESCE(SUM(l.delta), 0),
       COALESCE(c.created_at, CURRENT_TIMESTAMP), COALESCE(c.created_at, CURRENT_TIMESTAMP), TRUE
FROM customers c LEFT JOIN customer_ledger l ON l.customer_id = c.id GROUP BY c.id;

CREATE FUNCTION record_customer_ledger() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    CASE TG_TABLE_NAME
    WHEN 'customers' THEN
        INSERT INTO customer_ledger(customer_id, type, source_id, description, delta, effective_at)
        VALUES (NEW.id, 'INITIAL', NEW.id, 'Saldo devedor inicial', COALESCE(NEW.debt_balance, 0), clock_timestamp());
    WHEN 'sales' THEN
        IF TG_OP = 'INSERT' THEN
            INSERT INTO customer_ledger(customer_id, type, source_id, description, delta, effective_at)
            VALUES (NEW.customer_id, 'SALE', NEW.id, 'Venda #' || NEW.id, NEW.total_amount, NEW.sale_date);
        ELSIF NEW.status = 'CANCELLED' AND OLD.status IS DISTINCT FROM NEW.status THEN
            INSERT INTO customer_ledger(customer_id, type, source_id, description, delta, effective_at)
            VALUES (NEW.customer_id, 'SALE_CANCELLED', NEW.id, 'Cancelamento da venda #' || NEW.id, -NEW.total_amount, clock_timestamp());
        END IF;
    WHEN 'customer_payments' THEN
        INSERT INTO customer_ledger(customer_id, type, source_id, description, delta, effective_at)
        VALUES (NEW.customer_id, 'PAYMENT', NEW.id, 'Pagamento #' || NEW.id, -NEW.amount, NEW.paid_at);
    WHEN 'customer_debts' THEN
        INSERT INTO customer_ledger(customer_id, type, source_id, description, delta, effective_at)
        VALUES (NEW.customer_id, 'DEBT', NEW.id, NEW.description, NEW.amount, NEW.incurred_on);
    END CASE;
    RETURN NULL;
END;
$$;
CREATE TRIGGER ledger_initial AFTER INSERT ON customers FOR EACH ROW EXECUTE FUNCTION record_customer_ledger();
CREATE TRIGGER ledger_sales AFTER INSERT OR UPDATE ON sales FOR EACH ROW EXECUTE FUNCTION record_customer_ledger();
CREATE TRIGGER ledger_payments AFTER INSERT ON customer_payments FOR EACH ROW EXECUTE FUNCTION record_customer_ledger();
CREATE TRIGGER ledger_debts AFTER INSERT ON customer_debts FOR EACH ROW EXECUTE FUNCTION record_customer_ledger();

CREATE OR REPLACE FUNCTION record_customer_debt() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    owner_id BIGINT;
    customer_name TEXT;
BEGIN
    UPDATE customers
    SET debt_balance = COALESCE(debt_balance, 0) + NEW.amount, version = version + 1, updated_at = clock_timestamp()
    WHERE id = NEW.customer_id
    RETURNING user_id, name INTO owner_id, customer_name;

    INSERT INTO operation_history (user_id, type, title, entity_id, amount)
    VALUES (owner_id, 'CUSTOMER_DEBT', customer_name || ' — ' || NEW.description
        || ' (data do débito: ' || to_char(NEW.incurred_on, 'DD/MM/YYYY') || ')', NEW.customer_id, NEW.amount);
    RETURN NULL;
END;
$$;
