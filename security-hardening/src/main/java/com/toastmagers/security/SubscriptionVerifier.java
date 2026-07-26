package com.toastmagers.security;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Validates external Cloud rule subscriptions (T-SEC-02).
 * Enforces HTTPS-only protocols and performs cryptographic signature validation of rule contents.
 */
public class SubscriptionVerifier {

    private final PublicKey publicKey;

    public SubscriptionVerifier(String base64PublicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.publicKey = keyFactory.generatePublic(spec);
    }

    /**
     * Checks if the subscription url is secure (HTTPS-only) (T-SEC-02).
     */
    public static boolean isSecureUrl(String url) {
        if (url == null) {
            return false;
        }
        return url.toLowerCase().startsWith("https://");
    }

    /**
     * Crytographically verifies rule content signature.
     *
     * @param content        The raw JSON content of subscription rules
     * @param base64Signature Base64 RSA SHA256 signature
     * @return true if valid, false otherwise
     */
    public boolean verifySubscription(String content, String base64Signature) {
        if (content == null || base64Signature == null) {
            return false;
        }
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(content.getBytes("UTF-8"));
            byte[] signatureBytes = Base64.getDecoder().decode(base64Signature);
            return sig.verify(signatureBytes);
        } catch (Exception e) {
            System.err.println("Safe log: Cloud subscription cryptographic verification failed.");
            return false;
        }
    }
}
