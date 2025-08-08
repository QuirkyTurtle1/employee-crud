-- 1) case-insensitive уникальность имени продукта
-- (сервис уже проверяет, но дублируем на уровне БД)
CREATE UNIQUE INDEX IF NOT EXISTS ux_products_name_ci
    ON products (lower(name));

-- 2) уникальность товара в заказе (одна позиция на товар)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM   pg_constraint c
        JOIN   pg_class t  ON t.oid = c.conrelid
        WHERE  c.conname = 'uk_order_products_order_product'
        AND    t.relname = 'order_products'
    ) THEN
ALTER TABLE order_products
    ADD CONSTRAINT uk_order_products_order_product
        UNIQUE (order_id, product_id);
END IF;
END $$;

-- 3) количество > 0
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM   pg_constraint c
        JOIN   pg_class t  ON t.oid = c.conrelid
        WHERE  c.conname = 'ck_order_products_qty_positive'
        AND    t.relname = 'order_products'
    ) THEN
ALTER TABLE order_products
    ADD CONSTRAINT ck_order_products_qty_positive
        CHECK (quantity > 0);
END IF;
END $$;

-- 4) quantity NOT NULL (на всякий случай)
ALTER TABLE order_products
    ALTER COLUMN quantity SET NOT NULL;

-- 5) индексы под фильтры/джоины
CREATE INDEX IF NOT EXISTS idx_orders_created_at    ON orders (created_at);
CREATE INDEX IF NOT EXISTS idx_orders_status        ON orders (status);
CREATE INDEX IF NOT EXISTS idx_op_product_id        ON order_products (product_id);
CREATE INDEX IF NOT EXISTS idx_op_order_id          ON order_products (order_id);