CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR (255) NOT NULL,
    description TEXT,
    price NUMERICA(19, 2) NOT NULL,
    stock_quantity INT NOT NULL,
    image_url VARCHAR(255)
);