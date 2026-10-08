CREATE TABLE notifications (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id),
    item_id         UUID REFERENCES items(id),
    title           VARCHAR(150),
    message         TEXT,
    type            VARCHAR(30) NOT NULL,
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

-- Lets users opt into alerts for a category and/or keyword, so notifications
-- are targeted instead of broadcast to everyone.
CREATE TABLE user_subscriptions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id),
    category_id     INT REFERENCES categories(id),
    keyword         VARCHAR(120),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_subscriptions_user ON user_subscriptions(user_id);
CREATE INDEX idx_subscriptions_category ON user_subscriptions(category_id);
