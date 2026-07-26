package com.toastmagers.sync;

/**
 * Synchronizes rule switch states with the system's NotificationManagerService notification status (Epic F).
 * Uses direct system_server address space method invocation stubs abstracted by NotificationServiceAccessor.
 */
public class SystemStateSynchronizer {

    private final NotificationServiceAccessor accessor;

    public SystemStateSynchronizer(NotificationServiceAccessor accessor) {
        this.accessor = accessor;
    }

    /**
     * Aligns the system level notification state for a package with the specified rule block status (T-SYNC-02).
     *
     * @param packageName The package name to align
     * @param blockAll    If true, disables notification in system settings; if false, enables them.
     * @return true if synchronization succeeded, false otherwise
     */
    public boolean synchronizeState(String packageName, boolean blockAll) {
        if (packageName == null || accessor == null) {
            return false;
        }
        try {
            boolean systemEnabled = accessor.areNotificationsEnabled(packageName);
            boolean targetEnabled = !blockAll;

            if (systemEnabled != targetEnabled) {
                System.out.println("Sync State: Aligning system notifications for " + packageName + " to: " + targetEnabled);
                return accessor.setNotificationsEnabled(packageName, targetEnabled);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Safe log: Failed to synchronize system notification state safely: " + e.getMessage());
            return false;
        }
    }

    /**
     * Synchronizes and closes specific system notification channels for a package (T-SYNC-02).
     *
     * @param packageName               The target package name
     * @param blockNotificationChannels List of notification channel IDs to disable/close
     * @return true if all channel synchronizations succeeded
     */
    public boolean synchronizeChannelState(String packageName, java.util.List<String> blockNotificationChannels) {
        if (packageName == null || blockNotificationChannels == null || accessor == null) {
            return false;
        }
        boolean allSuccess = true;
        for (String channelId : blockNotificationChannels) {
            try {
                if (channelId != null && !channelId.isEmpty()) {
                    boolean enabled = accessor.isNotificationChannelEnabled(packageName, channelId);
                    if (enabled) {
                        System.out.println("Sync State: Closing system notification channel [" + channelId + "] for: " + packageName);
                        boolean ok = accessor.setNotificationChannelEnabled(packageName, channelId, false);
                        if (!ok) {
                            allSuccess = false;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Safe log: Failed to close channel " + channelId + " for " + packageName + ": " + e.getMessage());
                allSuccess = false;
            }
        }
        return allSuccess;
    }
}
