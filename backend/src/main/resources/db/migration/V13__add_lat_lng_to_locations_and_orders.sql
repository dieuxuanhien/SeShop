-- Add latitude and longitude to locations and orders

ALTER TABLE locations 
ADD COLUMN latitude DOUBLE PRECISION,
ADD COLUMN longitude DOUBLE PRECISION,
ADD COLUMN address_text VARCHAR(255);

ALTER TABLE orders
ADD COLUMN shipping_latitude DOUBLE PRECISION,
ADD COLUMN shipping_longitude DOUBLE PRECISION;
