package com.cardpayment.sql;

/**
 * Centralized SQL statements for the CardPayment app.
 * Helps keep repositories clean and consistent.
 */
public final class CardSQL {

    private CardSQL() {} // prevent instantiation

    public static final String INSERT_CARD = """
        INSERT INTO cards(name, pan_enc, pan_iv, pan_sha256, last4)
        VALUES (?, ?, ?, ?, ?)
        ON CONFLICT (pan_sha256) DO NOTHING
        """;

    public static final String FIND_BY_LAST4 = """
        SELECT name, last4, created_at
        FROM cards
        WHERE last4 = ?
        ORDER BY created_at DESC
        LIMIT 200
        """;

    public static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS cards (
            id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            name         TEXT NOT NULL,
            pan_enc      BYTEA NOT NULL,
            pan_iv       BYTEA NOT NULL,
            pan_sha256   TEXT NOT NULL UNIQUE,
            last4        CHAR(4) NOT NULL,
            created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
        );
        """;
}
