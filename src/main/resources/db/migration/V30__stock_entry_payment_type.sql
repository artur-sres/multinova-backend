-- Existing entries keep their historical cash effect; new entries require explicit payment.
ALTER TABLE stock_purchases ADD COLUMN entry_type VARCHAR(20) NOT NULL DEFAULT 'PAID';
ALTER TABLE stock_purchases ALTER COLUMN entry_type SET DEFAULT 'ADJUSTMENT';
ALTER TABLE stock_purchases ADD CONSTRAINT stock_purchases_entry_type_check
    CHECK (entry_type IN ('ADJUSTMENT', 'PAID', 'PENDING'));

-- Keep inventory entries without payment out of the cash-flow history filters.
CREATE FUNCTION classify_stock_entry_history() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.type = 'STOCK_PURCHASE' AND EXISTS (
        SELECT 1 FROM stock_purchases p
        WHERE p.id = NEW.entity_id AND p.user_id = NEW.user_id AND p.entry_type <> 'PAID'
    ) THEN
        NEW.type := 'STOCK_ENTRY';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER classify_stock_entry_history BEFORE INSERT ON operation_history
    FOR EACH ROW EXECUTE FUNCTION classify_stock_entry_history();
