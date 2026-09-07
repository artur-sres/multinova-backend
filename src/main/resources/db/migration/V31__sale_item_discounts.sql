ALTER TABLE sale_items
    ADD COLUMN discount_type VARCHAR(20) NOT NULL DEFAULT 'NONE',
    ADD COLUMN discount_value NUMERIC(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE sale_items ADD CONSTRAINT sale_item_discount_valid CHECK (
    discount_type IN ('NONE', 'PERCENTAGE', 'AMOUNT') AND
    discount_value >= 0 AND discount_amount >= 0 AND
    discount_amount <= unit_price * quantity AND
    (discount_type <> 'PERCENTAGE' OR discount_value <= 100) AND
    (discount_type <> 'NONE' OR (discount_value = 0 AND discount_amount = 0))
);
