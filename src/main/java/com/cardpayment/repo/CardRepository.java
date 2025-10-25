package com.cardpayment.repo;

import com.cardpayment.db.Database;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cardpayment.model.CardRecord;

import java.security.MessageDigest;
import java.util.HexFormat;

public class CardRepository {

    public void insertCard(String name, byte[] panCipher, byte[] iv, String plaintextPan) throws Exception {
        String last4 = plaintextPan.substring(plaintextPan.length() - 4);
        String panSha256 = sha256Hex(plaintextPan);

        String sql = """
            INSERT INTO cards(name, pan_enc, pan_iv, pan_sha256, last4)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (pan_sha256) DO NOTHING
            """;

        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setBytes(2, panCipher);
            ps.setBytes(3, iv);
            ps.setString(4, panSha256);
            ps.setString(5, last4);
            ps.executeUpdate();
        }
    }

    public List<CardRecord> findByLast4(String last4) throws Exception {
        String sql = """
            SELECT name, last4, created_at
            FROM cards
            WHERE last4 = ?
            ORDER BY created_at DESC
            LIMIT 200
            """;
        List<CardRecord> out = new ArrayList<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, last4);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String l4 = rs.getString("last4");
                    OffsetDateTime ts = rs.getObject("created_at", OffsetDateTime.class);
                    out.add(new CardRecord(name, l4, ts));
                }
            }
        }
        return out;
    }

    private static String sha256Hex(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] dig = md.digest(s.getBytes());
        return HexFormat.of().formatHex(dig);
    }
}
