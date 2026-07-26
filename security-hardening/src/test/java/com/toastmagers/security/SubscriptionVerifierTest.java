package com.toastmagers.security;

import org.junit.Assert;
import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;

public class SubscriptionVerifierTest {

    @Test
    public void testIsSecureUrl() {
        Assert.assertTrue(SubscriptionVerifier.isSecureUrl("https://example.com/rules.json"));
        Assert.assertFalse(SubscriptionVerifier.isSecureUrl("http://example.com/rules.json"));
        Assert.assertFalse(SubscriptionVerifier.isSecureUrl("ftp://example.com/rules.json"));
    }

    @Test
    public void testCryptoSignatureVerification() throws Exception {
        // Generate temporary test RSA key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();

        String base64PublicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
        SubscriptionVerifier verifier = new SubscriptionVerifier(base64PublicKey);

        String rulesContent = "{ \"version\": 1, \"rules\": [] }";

        // Create a valid signature
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(pair.getPrivate());
        privateSignature.update(rulesContent.getBytes("UTF-8"));
        byte[] signatureBytes = privateSignature.sign();
        String base64Signature = Base64.getEncoder().encodeToString(signatureBytes);

        // Verify valid signature
        Assert.assertTrue(verifier.verifySubscription(rulesContent, base64Signature));

        // Verify invalid signature or modified content
        Assert.assertFalse(verifier.verifySubscription(rulesContent + " ", base64Signature));
        Assert.assertFalse(verifier.verifySubscription(rulesContent, "invalid_sig"));
    }
}
