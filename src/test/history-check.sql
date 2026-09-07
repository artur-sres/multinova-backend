DO $$
DECLARE
    before_count INTEGER;
    entry_id BIGINT;
    bill_id BIGINT;
    sale_id BIGINT;
BEGIN
    IF (SELECT COUNT(*) FROM operation_history WHERE recovered) <> 6 THEN
        RAISE EXCEPTION 'Backfill did not recover the expected six events';
    END IF;
    SELECT COUNT(*) INTO before_count FROM operation_history;
    -- A failed operation must leave no event behind.
    BEGIN
        INSERT INTO cash_entries (user_id, title, kind, amount, received_on) VALUES (1, 'Rollback', 'LOAN', 50, CURRENT_DATE);
        RAISE EXCEPTION 'Simulated failure';
    EXCEPTION WHEN raise_exception THEN NULL;
    END;
    IF (SELECT COUNT(*) FROM operation_history) <> before_count THEN RAISE EXCEPTION 'History survived rollback'; END IF;
    IF EXISTS (SELECT 1 FROM cash_entries WHERE title = 'Rollback') THEN RAISE EXCEPTION 'Cash survived rollback'; END IF;

    INSERT INTO cash_entries (user_id, title, kind, amount, received_on) VALUES (1, 'Empréstimo', 'LOAN', 1000.50, CURRENT_DATE) RETURNING id INTO entry_id;
    INSERT INTO cash_entries (user_id, title, kind, amount, received_on) VALUES (2, 'Outro usuário', 'OTHER', 500, CURRENT_DATE);
    INSERT INTO cash_entries (user_id, title, kind, amount, received_on) VALUES (1, 'Outro período', 'CAPITAL', 200, CURRENT_DATE - 60);
    IF (SELECT SUM(amount) FROM cash_entries WHERE user_id = 1 AND received_on = CURRENT_DATE) <> 1000.50 THEN RAISE EXCEPTION 'Wrong cash sum or user scope'; END IF;
    IF NOT EXISTS (SELECT 1 FROM operation_history WHERE type = 'CASH_ENTRY' AND entity_id = entry_id AND user_id = 1 AND amount = 1000.50 AND NOT recovered) THEN RAISE EXCEPTION 'Cash event missing'; END IF;

    INSERT INTO sales (user_id, customer_id, total_amount) VALUES (1, 1, 25) RETURNING id INTO sale_id;
    UPDATE sales SET status = 'CANCELLED' WHERE id = sale_id;
    UPDATE sales SET status = 'CANCELLED' WHERE id = sale_id;
    IF (SELECT COUNT(*) FROM operation_history WHERE type = 'SALE_CANCELLED' AND entity_id = sale_id) <> 1 THEN RAISE EXCEPTION 'Cancellation missing or duplicated'; END IF;

    INSERT INTO bills (user_id, name, amount, due_date) VALUES (1, 'Conta teste', 30, CURRENT_DATE) RETURNING id INTO bill_id;
    UPDATE bills SET status = 'PAID', paid_at = CURRENT_TIMESTAMP WHERE id = bill_id;
    UPDATE bills SET status = 'PAID' WHERE id = bill_id;
    DELETE FROM bills WHERE id = bill_id;
    IF (SELECT COUNT(*) FROM operation_history WHERE entity_id = bill_id AND type IN ('BILL_CREATED','BILL_PAID','BILL_DELETED') AND NOT recovered) <> 3 THEN RAISE EXCEPTION 'Bill events lost or duplicated'; END IF;

    INSERT INTO stock_purchases (user_id, total_amount) VALUES (1, 123.45);
    INSERT INTO customer_payments (customer_id, paid_at, amount) VALUES (1, CURRENT_TIMESTAMP, 12.50);
    UPDATE customers SET debt_balance = 100 WHERE id = 1;
    IF EXISTS (SELECT 1 FROM operation_history WHERE type = 'CUSTOMER_UPDATED') THEN RAISE EXCEPTION 'Debt updates produced duplicate events'; END IF;
    UPDATE customers SET name = 'Cliente atualizado' WHERE id = 1;
    UPDATE products SET stock = 10 WHERE id = 1;
    IF EXISTS (SELECT 1 FROM operation_history WHERE type = 'PRODUCT_UPDATED') THEN RAISE EXCEPTION 'Stock updates produced duplicate events'; END IF;
    UPDATE products SET name = 'Produto atualizado' WHERE id = 1;
    UPDATE products SET deleted_at = CURRENT_TIMESTAMP WHERE id = 1;
    IF NOT EXISTS (SELECT 1 FROM operation_history WHERE type = 'STOCK_ENTRY' AND amount = 123.45) THEN RAISE EXCEPTION 'Unpaid stock event missing'; END IF;
    INSERT INTO stock_purchases (user_id, total_amount, entry_type) VALUES (1, 40, 'PAID'), (1, 80, 'PENDING'), (2, 500, 'PAID');
    IF (SELECT SUM(total_amount) FROM stock_purchases WHERE user_id = 1 AND entry_type = 'PAID' AND purchase_date::date = CURRENT_DATE) <> 40 THEN RAISE EXCEPTION 'Unpaid stock affected cash balance'; END IF;
    IF NOT EXISTS (SELECT 1 FROM operation_history WHERE user_id = 1 AND type = 'STOCK_PURCHASE' AND amount = 40) THEN RAISE EXCEPTION 'Paid purchase event missing'; END IF;
    IF NOT EXISTS (SELECT 1 FROM operation_history WHERE user_id = 1 AND type = 'STOCK_ENTRY' AND amount = 80) THEN RAISE EXCEPTION 'Pending purchase classified as payment'; END IF;
    IF NOT EXISTS (SELECT 1 FROM operation_history WHERE type = 'CUSTOMER_PAYMENT' AND user_id = 1 AND amount = 12.50) THEN RAISE EXCEPTION 'Payment event missing'; END IF;
    IF NOT EXISTS (SELECT 1 FROM operation_history WHERE type = 'PRODUCT_DELETED') THEN RAISE EXCEPTION 'Product deletion missing'; END IF;
    IF NOT EXISTS (SELECT 1 FROM operation_history WHERE type = 'CUSTOMER_UPDATED') THEN RAISE EXCEPTION 'Customer edit missing'; END IF;
    IF NOT EXISTS (SELECT 1 FROM operation_history WHERE recovered AND title = 'Cliente anterior') THEN RAISE EXCEPTION 'Historical name was overwritten'; END IF;

    INSERT INTO cash_entries (user_id, title, kind, amount, received_on)
    SELECT 1, 'Entrada ' || n, 'OTHER', 1, CURRENT_DATE FROM generate_series(1, 110) n;
    IF (SELECT COUNT(*) FROM (SELECT id FROM operation_history WHERE user_id = 1 ORDER BY occurred_at DESC, id DESC LIMIT 100) recent) <> 100 THEN RAISE EXCEPTION 'Recent limit failed'; END IF;
END;
$$;
SELECT 'History and cash SQL checks passed' AS result;
