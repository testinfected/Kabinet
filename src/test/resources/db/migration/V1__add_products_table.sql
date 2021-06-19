CREATE TABLE products
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    number      VARCHAR(8)   NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    PRIMARY KEY (id)
);
