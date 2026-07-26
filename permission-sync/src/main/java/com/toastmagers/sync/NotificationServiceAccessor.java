package com.toastmagers.sync;

/**
 * Interface representing system_server internal method calls for notifications (Epic F).
 * This abstracts accessing NotificationManagerService directly without binder permission checks.
 */
public interface NotificationServiceAccessor {
    /**
     * Checks if notification is enabled for a given package.
     *
     * @param packageName The package name to query
     * @return true if enabled, false otherwise
     */
    boolean areNotificationsEnabled(String packageName);

    /**
     * Sets whether notifications are enabled for a package.
     *
     * @param packageName The package name
     * @param enabled     Whether to enable or disable
     * @return true if set successfully, false otherwise
     */
    boolean setNotificationsEnabled(String packageName, boolean enabled);
}
