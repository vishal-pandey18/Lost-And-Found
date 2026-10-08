CREATE TABLE claims (
    id                  UUID PRIMARY KEY,
    item_id             UUID NOT NULL REFERENCES items(id),
    claimed_by          UUID NOT NULL REFERENCES users(id),
    proof_description   TEXT,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by         UUID REFERENCES users(id),
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_claims_item ON claims(item_id);
CREATE INDEX idx_claims_status ON claims(status);
CREATE INDEX idx_claims_claimed_by ON claims(claimed_by);
