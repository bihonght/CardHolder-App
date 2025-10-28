package com.cardpayment;

import com.cardpayment.repo.CardRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

public class CardRepositoryUnitTest {

    @Test
    void sha256Hex_producesConsistentHash() throws Exception {
        // Access private method via reflection
        Method m = CardRepository.class.getDeclaredMethod("sha256Hex", String.class);
        m.setAccessible(true);

        String pan = "4111111111111111";
        String hash1 = (String) m.invoke(null, pan);
        String hash2 = (String) m.invoke(null, pan);

        assertEquals(hash1, hash2);
        assertEquals(64, hash1.length(), "SHA-256 hex should be 64 chars");
    }

    @Test
    void sha256Hex_matchesManualComputation() throws Exception {
        String pan = "1234567890123456";
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String expected = HexFormat.of().formatHex(md.digest(pan.getBytes()));

        Method m = CardRepository.class.getDeclaredMethod("sha256Hex", String.class);
        m.setAccessible(true);
        String actual = (String) m.invoke(null, pan);

        assertEquals(expected, actual);
    }
}
