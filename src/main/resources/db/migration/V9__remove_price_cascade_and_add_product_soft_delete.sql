ALTER TABLE prices DROP CONSTRAINT prices_product_id_fkey;
ALTER TABLE prices ADD CONSTRAINT prices_product_id_fkey
    FOREIGN KEY (product_id) REFERENCES products(id);

ALTER TABLE products ADD COLUMN deleted_at TIMESTAMP;