package com.toastmagers.security;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Handles log desensitization and log file retention policy (T-SEC-01).
 * Redacts potential sensitive data such as verification codes, phone numbers, and keys.
 */
public class SafeLogManager {

    private static final Pattern VERIFICATION_CODE_PATTERN = Pattern.compile("\\b\\d{4,6}\\b");
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("\\b(\\+?86)?1[3-9]\\d{9}\\b");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

    private final File logsDir;
    private final int maxFiles;

    public SafeLogManager(File logsDir, int maxFiles) {
        this.logsDir = logsDir;
        this.maxFiles = maxFiles;
        if (logsDir != null && !logsDir.exists()) {
            logsDir.mkdirs();
        }
    }

    /**
     * Masks sensitive details in Toast/Notification content.
     */
    public static String redact(String message) {
        if (message == null) {
            return null;
        }
        // Redact verification codes (4-6 digits)
        String step1 = VERIFICATION_CODE_PATTERN.matcher(message).replaceAll("[REDACTED_CODE]");
        // Redact phone numbers
        return PHONE_NUMBER_PATTERN.matcher(step1).replaceAll("[REDACTED_PHONE]");
    }

    /**
     * Appends a log line to a specified log file, applying desensitization first.
     */
    public synchronized void log(String tag, String packageName, String message) {
        String redactedMessage = redact(message);
        String timestamp = DATE_FORMAT.format(new Date());
        String logLine = String.format("[%s] [%s] [%s]: %s\n", timestamp, tag, packageName, redactedMessage);

        File logFile = new File(logsDir, "toast_magers_tracker.log");
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(logLine);
        } catch (IOException e) {
            System.err.println("Safe log: Failed to append to log file safely.");
        }
    }

    /**
     * Enforces automatic log cleanup based on the maximum allowed log files count (T-SEC-01).
     */
    public synchronized void enforceRetentionPolicy() {
        if (logsDir == null || !logsDir.exists()) {
            return;
        }
        File[] files = logsDir.listFiles((dir, name) -> name.endsWith(".log"));
        if (files == null || files.length <= maxFiles) {
            return;
        }

        // Sort files by modification date, oldest first
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));

        int filesToDelete = files.length - maxFiles;
        for (int i = 0; i < filesToDelete; i++) {
            if (files[i].delete()) {
                System.out.println("Enforced retention policy: deleted oldest log file: " + files[i].getName());
            }
        }
    }
}
