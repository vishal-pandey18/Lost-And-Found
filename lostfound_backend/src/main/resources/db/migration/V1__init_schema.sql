CREATE TABLE users (
    id              UUID PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    email           VARCHAR(150) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    phone           VARCHAR(20),
    role            VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_verified     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE categories (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(80) UNIQUE NOT NULL,
    description     TEXT,
    icon_url        VARCHAR(255)
);

INSERT INTO categories (name, description) VALUES
    ('Electronics', 'Phones, laptops, chargers, earphones, etc.'),
    ('Stationery', 'Pens, notebooks, calculators, etc.'),
    ('Clothing', 'Jackets, bags, shoes, accessories'),
    ('Documents', 'ID cards, certificates, cards'),
    ('Keys', 'House, vehicle, locker keys'),
    ('Other', 'Anything that does not fit another category');

CREATE TABLE items (
    id              UUID PRIMARY KEY,
    title           VARCHAR(150) NOT NULL,
    description     TEXT,
    category_id     INT NOT NULL REFERENCES categories(id),
    item_type       VARCHAR(10) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    location        VARCHAR(255),
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    event_date      DATE,
    reported_by     UUID NOT NULL REFERENCES users(id),
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_items_type_status ON items(item_type, status);
CREATE INDEX idx_items_category ON items(category_id);
CREATE INDEX idx_items_created_at ON items(created_at DESC);

CREATE TABLE item_images (
    id              BIGSERIAL PRIMARY KEY,
    item_id         UUID NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    image_url       VARCHAR(500) NOT NULL,
    uploaded_at     TIMESTAMP NOT NULL DEFAULT now()
);
