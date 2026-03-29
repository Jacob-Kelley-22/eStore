-- Create tables
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role VARCHAR(255),
    CONSTRAINT users_role_check
    CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    price NUMERIC(19,2) NOT NULL,
    stock_quantity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    image_url VARCHAR(255)
);

CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_carts_user
    FOREIGN KEY (user_id)
    REFERENCES users (id)
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_price NUMERIC(19,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(255) NOT NULL,
    CONSTRAINT fk_orders_user
    FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT orders_status_check
        CHECK (status IN ('PENDING_PAYMENT', 'PAID', 'PAYMENT_FAILED', 'CANCELLED', 'FULFILLED'))
);

CREATE TABLE cart_item (
    id BIGSERIAL PRIMARY KEY,
    quantity INT NOT NULL CHECK (quantity >= 1),
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    CONSTRAINT fk_cart_item_cart
        FOREIGN KEY (cart_id)
    REFERENCES carts (id),
        CONSTRAINT fk_cart_item_product
    FOREIGN KEY (product_id)
        REFERENCES products (id),
    CONSTRAINT uk_cart_product UNIQUE (cart_id, product_id)
);

CREATE TABLE order_item (
id BIGSERIAL PRIMARY KEY,
price_at_purchase NUMERIC(19,2) NOT NULL,
quantity INT NOT NULL CHECK (quantity >= 1),
order_id BIGINT NOT NULL,
product_id BIGINT NOT NULL,
CONSTRAINT fk_order_item_order
    FOREIGN KEY (order_id)
    REFERENCES orders (id),
CONSTRAINT fk_order_item_product
    FOREIGN KEY (product_id)
    REFERENCES products (id),
CONSTRAINT uk_order_item_order_product
    UNIQUE (order_id, product_id)
);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(19,2) NOT NULL,
    card_holder_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    failure_reason VARCHAR(255),
    last4 VARCHAR(4) NOT NULL,
    processed_at TIMESTAMP,
    public_id UUID NOT NULL UNIQUE,
    status VARCHAR(255) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id),
    CONSTRAINT payments_status_check
        CHECK (status IN ('PENDING', 'SUCCEEDED', 'FAILED'))
);

CREATE TABLE checkout_requests (
id BIGSERIAL PRIMARY KEY,
completed_at TIMESTAMP,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
failure_reason VARCHAR(255),
idempotency_key VARCHAR(100) NOT NULL,
status VARCHAR(255) NOT NULL,
order_id BIGINT UNIQUE,
user_id BIGINT NOT NULL,
CONSTRAINT fk_checkout_requests_user
    FOREIGN KEY (user_id)
    REFERENCES users (id),
CONSTRAINT fk_checkout_requests_order
    FOREIGN KEY (order_id)
    REFERENCES orders (id),
CONSTRAINT uk_checkout_request_user_idempotency
    UNIQUE (user_id, idempotency_key),
CONSTRAINT checkout_requests_status_check
    CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);
