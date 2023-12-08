package com.maidgroup.maidgroup.util.square;

// SignatureGenerator.java

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SignatureGenerator {

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public static String generateSignature(String url, String payload, String key) {
        try {
            // Concatenate the URL and the payload
            String stringToSign = url + payload;

            // Create a new HMAC object with our key and the SHA1 algorithm
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);

            // Generate the HMAC
            byte[] rawHmac = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));

            // Base64 encode the HMAC
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC : " + e.getMessage());
        }
    }
}

