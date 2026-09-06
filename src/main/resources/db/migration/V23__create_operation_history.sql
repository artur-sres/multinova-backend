CREATE TABLE operation_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    type VARCHAR(40) NOT NULL,
    title TEXT NOT NULL,
    entity_id BIGINT NOT NULL,
    amount NUMERIC(12,2),
    occurred_at TIMESTAMP NOT NULL DEFAULT clock_timestamp(),
    recovered BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_history_user_time ON operation_history (user_id, occurred_at DESC, id DESC);

-- Recover only events supported by records that still exist. These are explicitly
-- marked as recovered; deleted records and their former operations cannot be inferred.
INSERT INTO operation_history (user_id, type, title, entity_id, amount, occurred_at, recovered)
SELECT s.user_id, 'SALE_CREATED', 'Venda #' || s.id || ' — ' || c.name, s.id, s.total_amount, s.sale_date, TRUE
FROM sales s JOIN customers c ON c.id = s.customer_id;
INSERT INTO operation_history (user_id, type, title, entity_id, amount, occurred_at, recovered)
SELECT s.user_id, 'SALE_CANCELLED', 'Venda #' || s.id || ' — ' || c.name, s.id, s.total_amount, s.updated_at, TRUE
FROM sales s JOIN customers c ON c.id = s.customer_id WHERE s.status = 'CANCELLED';
INSERT INTO operation_history (user_id, type, title, entity_id, amount, occurred_at, recovered)
SELECT user_id, 'STOCK_PURCHASE', 'Entrada de estoque #' || id, id, total_amount, purchase_date, TRUE FROM stock_purchases;
INSERT INTO operation_history (user_id, type, title, entity_id, occurred_at, recovered)
SELECT user_id, 'CUSTOMER_CREATED', name, id, created_at, TRUE FROM customers WHERE created_at IS NOT NULL;
INSERT INTO operation_history (user_id, type, title, entity_id, occurred_at, recovered)
SELECT user_id, 'PRODUCT_CREATED', name, id, created_at, TRUE FROM products WHERE created_at IS NOT NULL;
INSERT INTO operation_history (user_id, type, title, entity_id, amount, occurred_at, recovered)
SELECT user_id, 'BILL_CREATED', name, id, amount, created_at, TRUE FROM bills;
INSERT INTO operation_history (user_id, type, title, entity_id, amount, occurred_at, recovered)
SELECT user_id, 'BILL_PAID', name, id, amount, paid_at, TRUE FROM bills WHERE paid_at IS NOT NULL AND status = 'PAID';
INSERT INTO operation_history (user_id, type, title, entity_id, amount, occurred_at, recovered)
SELECT c.user_id, 'CUSTOMER_PAYMENT', c.name, p.id, p.amount, p.paid_at, TRUE
FROM customer_payments p JOIN customers c ON c.id = p.customer_id;
INSERT INTO operation_history (user_id, type, title, entity_id, amount, occurred_at, recovered)
SELECT user_id, 'CASH_ENTRY', title, id, amount, created_at, TRUE FROM cash_entries;

-- Triggers record snapshots in the same transaction as the operation. A rollback
-- also rolls back its history, and later deletion cannot remove the snapshot.
CREATE FUNCTION record_operation_history() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    data JSONB;
    owner_id BIGINT;
    event_type TEXT;
    event_title TEXT;
    event_amount NUMERIC(12,2);
BEGIN
    IF TG_OP = 'DELETE' THEN data := to_jsonb(OLD); ELSE data := to_jsonb(NEW); END IF;
    owner_id := (data->>'user_id')::BIGINT;
    event_title := COALESCE(data->>'name', data->>'title');
    event_amount := COALESCE(data->>'total_amount', data->>'amount')::NUMERIC;

    CASE TG_TABLE_NAME
    WHEN 'sales' THEN
        IF TG_OP = 'INSERT' THEN event_type := 'SALE_CREATED';
        ELSIF NEW.status = 'CANCELLED' AND OLD.status IS DISTINCT FROM NEW.status THEN event_type := 'SALE_CANCELLED';
        ELSE RETURN NULL; END IF;
        SELECT 'Venda #' || NEW.id || ' — ' || name INTO event_title FROM customers WHERE id = NEW.customer_id;
    WHEN 'stock_purchases' THEN
        event_type := 'STOCK_PURCHASE'; event_title := 'Entrada de estoque #' || NEW.id;
    WHEN 'customers' THEN
        IF TG_OP = 'INSERT' THEN event_type := 'CUSTOMER_CREATED';
        ELSIF (NEW.name, NEW.address, NEW.phone, NEW.birth_date, NEW.preferences, NEW.photo_url)
              IS DISTINCT FROM (OLD.name, OLD.address, OLD.phone, OLD.birth_date, OLD.preferences, OLD.photo_url)
        THEN event_type := 'CUSTOMER_UPDATED'; ELSE RETURN NULL; END IF;
    WHEN 'products' THEN
        IF TG_OP = 'INSERT' THEN event_type := 'PRODUCT_CREATED';
        ELSIF OLD.deleted_at IS NULL AND NEW.deleted_at IS NOT NULL THEN event_type := 'PRODUCT_DELETED';
        ELSIF (NEW.name, NEW.description, NEW.brand_id, NEW.category_id, NEW.brand_code, NEW.image_url)
              IS DISTINCT FROM (OLD.name, OLD.description, OLD.brand_id, OLD.category_id, OLD.brand_code, OLD.image_url)
        THEN event_type := 'PRODUCT_UPDATED'; ELSE RETURN NULL; END IF;
    WHEN 'bills' THEN
        IF TG_OP = 'INSERT' THEN event_type := 'BILL_CREATED';
        ELSIF TG_OP = 'DELETE' THEN event_type := 'BILL_DELETED';
        ELSIF NEW.status = 'PAID' AND OLD.status IS DISTINCT FROM NEW.status THEN event_type := 'BILL_PAID';
        ELSE RETURN NULL; END IF;
    WHEN 'customer_payments' THEN
        event_type := 'CUSTOMER_PAYMENT';
        SELECT user_id, name INTO owner_id, event_title FROM customers WHERE id = NEW.customer_id;
    WHEN 'cash_entries' THEN event_type := 'CASH_ENTRY';
    END CASE;

    INSERT INTO operation_history (user_id, type, title, entity_id, amount)
    VALUES (owner_id, event_type, event_title, (data->>'id')::BIGINT, event_amount);
    RETURN NULL;
END;
$$;

CREATE TRIGGER history_sales AFTER INSERT OR UPDATE ON sales FOR EACH ROW EXECUTE FUNCTION record_operation_history();
CREATE TRIGGER history_stock AFTER INSERT ON stock_purchases FOR EACH ROW EXECUTE FUNCTION record_operation_history();
CREATE TRIGGER history_customers AFTER INSERT OR UPDATE ON customers FOR EACH ROW EXECUTE FUNCTION record_operation_history();
CREATE TRIGGER history_products AFTER INSERT OR UPDATE ON products FOR EACH ROW EXECUTE FUNCTION record_operation_history();
CREATE TRIGGER history_bills AFTER INSERT OR UPDATE OR DELETE ON bills FOR EACH ROW EXECUTE FUNCTION record_operation_history();
CREATE TRIGGER history_payments AFTER INSERT ON customer_payments FOR EACH ROW EXECUTE FUNCTION record_operation_history();
CREATE TRIGGER history_cash AFTER INSERT ON cash_entries FOR EACH ROW EXECUTE FUNCTION record_operation_history();
