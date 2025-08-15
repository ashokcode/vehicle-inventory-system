-- Requires pgcrypto for gen_random_uuid() — already enabled on every Supabase
-- project by default.

CREATE TABLE admin_users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(60) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    enabled       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE vehicles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    brand           VARCHAR(60) NOT NULL,
    model           VARCHAR(60) NOT NULL,
    model_year      INTEGER NOT NULL,
    engine          VARCHAR(120),
    vin             VARCHAR(17) NOT NULL UNIQUE,
    mileage         INTEGER NOT NULL,
    purchase_price  NUMERIC(12, 2) NOT NULL,
    selling_price   NUMERIC(12, 2),
    status          VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    condition       VARCHAR(30) NOT NULL,
    notes           TEXT,
    date_added      TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_updated    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_vehicles_status ON vehicles (status);
CREATE INDEX idx_vehicles_brand_model ON vehicles (brand, model);
CREATE INDEX idx_vehicles_date_added ON vehicles (date_added DESC);

CREATE TABLE vehicle_photos (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id     UUID NOT NULL REFERENCES vehicles (id) ON DELETE CASCADE,
    url            VARCHAR(500) NOT NULL,
    storage_path   VARCHAR(500) NOT NULL,
    is_primary     BOOLEAN NOT NULL DEFAULT FALSE,
    display_order  INTEGER NOT NULL DEFAULT 0,
    uploaded_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_vehicle_photos_vehicle_id ON vehicle_photos (vehicle_id);
