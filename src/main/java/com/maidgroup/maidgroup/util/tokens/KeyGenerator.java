package com.maidgroup.maidgroup.util.tokens;

import java.security.SecureRandom;
import java.util.Base64;

public class KeyGenerator {
    public static void main(String[] args) {
        // Create a new SecureRandom instance
        SecureRandom secureRandom = new SecureRandom();

        // Generate a 256-bit secret key
        byte[] secretKey = new byte[32];
        secureRandom.nextBytes(secretKey);

        // Encode the secret key using Base64
        String encodedSecretKey = Base64.getEncoder().encodeToString(secretKey);

        // Print the encoded secret key
        System.out.println("Secret key: " + encodedSecretKey);
    }
}

