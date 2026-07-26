package com.toastmagers.security;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

public class SafeLogManagerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testRedaction() {
        String msg1 = "您的验证码是 4821 或者是 123456 请勿告诉他人";
        String redacted1 = SafeLogManager.redact(msg1);
        Assert.assertTrue(redacted1.contains("[REDACTED_CODE]"));
        Assert.assertFalse(redacted1.contains("4821"));
        Assert.assertFalse(redacted1.contains("123456"));

        String msg2 = "联系电话：13912345678 或 +8613800000000";
        String redacted2 = SafeLogManager.redact(msg2);
        Assert.assertTrue(redacted2.contains("[REDACTED_PHONE]"));
        Assert.assertFalse(redacted2.contains("13912345678"));
    }

    @Test
    public void testLogRetentionPolicy() throws IOException {
        File logsDir = tempFolder.newFolder("logs");
        // Let's create some dummy old log files
        for (int i = 0; i < 5; i++) {
            File logFile = new File(logsDir, "test_log_" + i + ".log");
            logFile.createNewFile();
            // Stagger their modified timestamps
            logFile.setLastModified(System.currentTimeMillis() - (5 - i) * 10000);
        }

        SafeLogManager manager = new SafeLogManager(logsDir, 2); // max 2 files
        manager.enforceRetentionPolicy();

        File[] files = logsDir.listFiles((dir, name) -> name.endsWith(".log"));
        Assert.assertEquals(2, files.length);
    }
}
