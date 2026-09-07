ALTER TABLE sale_items ADD COLUMN product_name VARCHAR(255);
ALTER TABLE sale_items ADD COLUMN name_recovered BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE sale_items i SET product_name = p.name, name_recovered = TRUE FROM products p WHERE p.id = i.product_id;
ALTER TABLE sale_items ALTER COLUMN product_name SET NOT NULL;

CREATE FUNCTION snapshot_sale_item_name() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.product_name := (SELECT name FROM products WHERE id = NEW.product_id);
    NEW.name_recovered := FALSE;
    RETURN NEW;
END;
$$;
CREATE TRIGGER sale_item_name_snapshot BEFORE INSERT ON sale_items
FOR EACH ROW EXECUTE FUNCTION snapshot_sale_item_name();

ALTER TABLE customer_ledger ADD COLUMN customer_name VARCHAR(255);
ALTER TABLE customer_ledger ADD COLUMN name_recovered BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE customer_ledger l SET customer_name = c.name, name_recovered = TRUE FROM customers c WHERE c.id = l.customer_id;
ALTER TABLE customer_ledger ALTER COLUMN customer_name SET NOT NULL;

CREATE FUNCTION snapshot_ledger_customer_name() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.customer_name := (SELECT name FROM customers WHERE id = NEW.customer_id);
    NEW.name_recovered := FALSE;
    RETURN NEW;
END;
$$;
CREATE TRIGGER ledger_customer_name_snapshot BEFORE INSERT ON customer_ledger
FOR EACH ROW EXECUTE FUNCTION snapshot_ledger_customer_name();
