CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(50),
    birth_date DATE,
    photo_url VARCHAR(500),
    preferences TEXT,

    debt_balance NUMERIC(10,2) DEFAULT 0.00,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


