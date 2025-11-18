package com.java.TMDTPicnic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class JwtKeyProvider {

    private static final int MIN_KEY_LENGTH_BYTES = 64; // HS512 yêu cầu >= 512-bit

    private final byte[] signingKeyBytes;
    private final SecretKey secretKey;

    public JwtKeyProvider(@Value("${jwt.signerKey}") String signerKey) {
        if (signerKey == null || signerKey.isBlank()) {
            throw new IllegalStateException("jwt.signerKey must not be empty");
        }
        this.signingKeyBytes = normalizeKeyLength(signerKey);
        this.secretKey = new SecretKeySpec(this.signingKeyBytes, "HmacSHA512");
    }

    private byte[] normalizeKeyLength(String signerKey) {
        byte[] rawBytes = signerKey.getBytes(StandardCharsets.UTF_8);
        if (rawBytes.length >= MIN_KEY_LENGTH_BYTES) {
            return rawBytes;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            return digest.digest(rawBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 algorithm not available", e);
        }
    }

    public byte[] getSigningKeyBytes() {
        return signingKeyBytes.clone();
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
}

