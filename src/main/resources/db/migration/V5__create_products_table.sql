CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL REFERENCES users(id),
    brand_id BIGINT NOT NULL REFERENCES brands(id),
    category_id BIGINT NOT NULL REFERENCES categories(id),

    brand_code VARCHAR(50) NOT NULL,

    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),

    stock INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_brand_code UNIQUE (brand_id, brand_code)
);