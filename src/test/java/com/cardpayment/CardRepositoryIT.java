package com.cardpayment;

import com.cardpayment.db.Database;
import com.cardpayment.model.CardRecord;
import com.cardpayment.repo.CardRepository;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.security.SecureRandom;
import java.sql.Connection;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CardRepositoryIT {

    @Container
    static final PostgreSQLContainer<?> pg =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("carddb")
                    .withUsername("carduser")
                    .withPassword("cardpass");

    static CardRepository repo;

    @BeforeAll
    static void setup() throws Exception {
        // Provide AES key via system property (Crypto reads this)
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        System.setProperty("AES_KEY_B64", Base64.getEncoder().encodeToString(key));

        // Point Database to Testcontainers Postgres
        Database.init(pg.getJdbcUrl(), pg.getUsername(), pg.getPassword());
        try (Connection c = Database.get()) {
            Database.ensureSchema(c);
        }
        repo = new CardRepository();
    }

    @Test
    @Order(1)
    void insertAndFindByLast4() throws Exception {
        String name = "Alice";
        String pan  = "4111111111114321"; // 16 digits
        repo.insertCard(name, new byte[]{1}, new byte[]{2}, pan); // Crypto is used inside insertCard

        List<CardRecord> result = repo.findByLast4("4321");
        assertFalse(result.isEmpty(), "Should find at least one record");
        assertEquals("4321", result.get(0).last4());
        assertEquals("Alice", result.get(0).name());
    }

    @Test
    @Order(2)
    void duplicateInsertIsIgnoredByUniqueHash() throws Exception {
        String name = "Bob";
        String pan  = "4000000000009999";
        repo.insertCard(name, new byte[]{3}, new byte[]{4}, pan);
        // same PAN again → ON CONFLICT DO NOTHING
        repo.insertCard(name, new byte[]{5}, new byte[]{6}, pan);

        List<CardRecord> result = repo.findByLast4("9999");
        // Should be exactly one logical card (by PAN hash uniqueness);
        // findByLast4 shows rows; we expect 1 because duplicates are ignored.
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).name());
    }
}
