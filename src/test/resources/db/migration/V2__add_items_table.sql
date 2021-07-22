CREATE TABLE items
(
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    number     VARCHAR(8)     NOT NULL UNIQUE,
    product_id BIGINT         NOT NULL,
    price      DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT item_product FOREIGN KEY (product_id) REFERENCES products (id)
);
