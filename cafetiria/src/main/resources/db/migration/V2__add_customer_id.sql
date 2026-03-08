ALTER TABLE orders ADD COLUMN customer_id UUID NOT NULL;
ALTER TABLE orders DROP COLUMN customer_name;