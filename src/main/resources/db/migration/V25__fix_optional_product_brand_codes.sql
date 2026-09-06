-- An omitted code must not collide with another product in the same brand.
UPDATE products SET brand_code = NULL
WHERE brand_code IS NOT NULL AND brand_code ~ '^[[:space:]]*$';

ALTER TABLE products DROP CONSTRAINT uq_brand_code;

-- Match the service validation: deleted products no longer reserve a code.
CREATE UNIQUE INDEX uq_active_brand_code ON products (brand_id, brand_code)
WHERE deleted_at IS NULL AND brand_code IS NOT NULL;
