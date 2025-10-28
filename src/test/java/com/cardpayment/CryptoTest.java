package com.cardpayment;

import com.cardpayment.security.Crypto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class CryptoTest {

    @BeforeAll
    static void setupKey() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        System.setProperty("AES_KEY_B64", Base64.getEncoder().encodeToString(key));
    }

    @Test
    void encryptAndDecrypt_returnOriginal() throws Exception {
        String original = "4111111111111111";
        Crypto.Encrypted enc = Crypto.encrypt(original.getBytes());
        byte[] decrypted = Crypto.decrypt(enc.ciphertext(), enc.iv());

        assertEquals(original, new String(decrypted));
    }

    @Test
    void encrypt_usesDifferentIVEachTime() throws Exception {
        String data = "sameplaintext";
        Crypto.Encrypted a = Crypto.encrypt(data.getBytes());
        Crypto.Encrypted b = Crypto.encrypt(data.getBytes());

        assertNotEquals(Base64.getEncoder().encodeToString(a.iv()),
                Base64.getEncoder().encodeToString(b.iv()),
                "IVs must differ for each encryption");
    }
}
