CREATE TABLE IF NOT EXISTS cards (
                                     id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name         TEXT NOT NULL,
    pan_enc      BYTEA NOT NULL,
    pan_iv       BYTEA NOT NULL,
    pan_sha256   TEXT NOT NULL UNIQUE,      -- prevent duplicates without storing plaintext
    last4        CHAR(4) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
    );

-- Extensions needed for UUID generation in Postgres official images
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'pgcrypto') THEN
        CREATE EXTENSION pgcrypto;
END IF;
END$$;

CREATE INDEX IF NOT EXISTS idx_cards_last4_created ON cards(last4, created_at DESC);
