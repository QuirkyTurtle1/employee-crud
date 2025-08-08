-- Таблица клиентов
CREATE TABLE IF NOT EXISTS clients
(
    id         UUID PRIMARY KEY,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    email      VARCHAR(255),
    phone      VARCHAR(255)
    );

-- Таблица сотрудников
CREATE TABLE IF NOT EXISTS employee
(
    id         UUID PRIMARY KEY,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    email      VARCHAR(255),
    password   VARCHAR(255),
    role       VARCHAR(255)
    );

-- Таблица продуктов
CREATE TABLE IF NOT EXISTS products
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(255),
    description VARCHAR(255),
    price       DECIMAL(19,2)
    );

-- Таблица заказов
CREATE TABLE IF NOT EXISTS orders
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    status     VARCHAR(255),
    client_id  UUID

    );

-- Связующая таблица заказов и товаров
CREATE TABLE IF NOT EXISTS order_products
(
    id         UUID PRIMARY KEY,
    order_id   UUID,
    product_id UUID,
    quantity   INTEGER NOT NULL
);

-- Связи (FK).

DO $$
    BEGIN
        IF to_regclass('public.orders') IS NOT NULL
            AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_orders_on_client') THEN
            ALTER TABLE public.orders
                ADD CONSTRAINT fk_orders_on_client
                    FOREIGN KEY (client_id) REFERENCES public.clients(id) ON DELETE RESTRICT;
        END IF;
    END $$;


DO $$
    BEGIN
        IF to_regclass('public.order_products') IS NOT NULL
            AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_order_products_on_order') THEN
            ALTER TABLE public.order_products
                ADD CONSTRAINT fk_order_products_on_order
                    FOREIGN KEY (order_id) REFERENCES public.orders(id) ON DELETE RESTRICT;
        END IF;
    END $$;


DO $$
    BEGIN
        IF to_regclass('public.order_products') IS NOT NULL
            AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_order_products_on_product') THEN
            ALTER TABLE public.order_products
                ADD CONSTRAINT fk_order_products_on_product
                    FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE RESTRICT;
        END IF;
    END $$;

-- Уникальность пары (order_id, product_id) — чтобы один товар был одной строкой в заказе.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_order_products_order_product'
    ) THEN
ALTER TABLE order_products
    ADD CONSTRAINT uk_order_products_order_product UNIQUE (order_id, product_id);
END IF;
END $$;

-- Уникальность email у сотрудников
DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uc_employee_email') THEN
            ALTER TABLE public.employee
                ADD CONSTRAINT uc_employee_email UNIQUE (email);
        END IF;
    END $$;
