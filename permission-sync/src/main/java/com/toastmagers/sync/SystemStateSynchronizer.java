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
}
