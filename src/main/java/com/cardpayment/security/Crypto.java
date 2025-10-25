package com.cardpayment.security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public final class Crypto {
    private static final SecureRandom RNG = new SecureRandom();
    private static final byte[] KEY = keyFromEnv();

    public record Encrypted(byte[] ciphertext, byte[] iv) {}

    public static Encrypted encrypt(byte[] plaintext) throws Exception {
        byte[] iv = new byte[16];
        RNG.nextBytes(iv);

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, "AES"), new IvParameterSpec(iv));
        byte[] ct = c.doFinal(plaintext);
        return new Encrypted(ct, iv);
    }

    public static byte[] decrypt(byte[] ciphertext, byte[] iv) throws Exception {
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY, "AES"), new IvParameterSpec(iv));
        return c.doFinal(ciphertext);
    }

    private static byte[] keyFromEnv() {
        String b64 = System.getenv("AES_KEY_B64");
        if (b64 == null || b64.isEmpty()) {
            throw new IllegalStateException("AES_KEY_B64 not set (base64-encoded 32 bytes required).");
        }
        byte[] k = Base64.getDecoder().decode(b64);
        if (k.length != 32) throw new IllegalStateException("AES key must be 32 bytes (256-bit).");
        return k;
    }
}
