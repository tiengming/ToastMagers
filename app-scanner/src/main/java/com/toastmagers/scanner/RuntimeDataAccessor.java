package com.toastmagers.scanner;

import java.util.List;

/**
 * Interface representing access to Toast/notification runtime data (Epic C).
 * This abstracts reading from PackageManager or NMS runtime state on a physical device.
 */
public interface RuntimeDataAccessor {
    /**
     * Enumerates all installed application package names.
     *
     * @return List of package names
     */
    List<String> getInstalledPackages();

    /**
     * Gets a list of packages that have registered active NotificationChannels.
     *
     * @return List of package names with active channels
     */
    List<String> getPackagesWithActiveChannels();

    /**
     * Checks if the given package has granted the POST_NOTIFICATIONS permission (Android 13+).
     *
     * @param packageName The package name to check
     * @return true if granted, false otherwise
     */
    boolean hasPostNotificationPermission(String packageName);
}
