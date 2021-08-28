CREATE TABLE orders
(
    id        BIGINT     NOT NULL AUTO_INCREMENT,
    number    VARCHAR(8) NOT NULL UNIQUE,
    placed_at TIMESTAMP  NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE line_items
(
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    item_number     VARCHAR(8)     NOT NULL UNIQUE,
    item_unit_price DECIMAL(10, 2) NOT NULL,
    quantity        SMALLINT DEFAULT NULL,
    order_id        BIGINT         NOT NULL,
    order_line      SMALLINT DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT line_item_order FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE TABLE payments
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT,
    date     DATE,
    amount   DECIMAL(12, 2),
    PRIMARY KEY (id),
    CONSTRAINT payment_order FOREIGN KEY (order_id) REFERENCES orders (id)
);
