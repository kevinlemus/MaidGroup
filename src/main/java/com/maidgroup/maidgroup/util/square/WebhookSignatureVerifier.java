package com.maidgroup.maidgroup.util.square;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class WebhookSignatureVerifier {

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    @Value("${square.webhookUrl}")
    private String webhookUrl;

    public boolean verifySignature (String payload, String signature, String signatureKey) throws NoSuchAlgorithmException, InvalidKeyException {

        // Include the webhook URL in the string to sign
        String stringToSign = webhookUrl + payload;

        SecretKeySpec signingKey = new SecretKeySpec(signatureKey.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        String checkSignature = Base64.getEncoder().encodeToString(rawHmac);
        return checkSignature.equals(signature);

    }
}
