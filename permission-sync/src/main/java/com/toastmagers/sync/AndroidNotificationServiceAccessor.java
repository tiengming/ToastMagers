package com.toastmagers.sync;

/**
 * Production implementation of NotificationServiceAccessor running in system_server (Epic F).
 * Directly accesses and manipulates the internal NotificationManagerService states.
 */
public class AndroidNotificationServiceAccessor implements NotificationServiceAccessor {

    private final Object notificationManagerService;

    public AndroidNotificationServiceAccessor(Object notificationManagerService) {
        this.notificationManagerService = notificationManagerService;
    }

    @Override
    public boolean areNotificationsEnabled(String packageName) {
        if (packageName == null || notificationManagerService == null) {
            return true;
        }
        try {
            // Directly call NMS internal areNotificationsEnabledForPackage or similar
            java.lang.reflect.Method method = notificationManagerService.getClass()
                    .getMethod("areNotificationsEnabledForPackage", String.class, int.class);
            return (boolean) method.invoke(notificationManagerService, packageName, 10000); // uid 10000 / system
        } catch (Throwable t) {
            System.err.println("AndroidNotificationServiceAccessor: Failed to query areNotificationsEnabled: " + t.getMessage());
            return true;
        }
    }

    @Override
    public boolean setNotificationsEnabled(String packageName, boolean enabled) {
        if (packageName == null || notificationManagerService == null) {
            return false;
        }
        try {
            // Directly call NMS internal setNotificationsEnabledForPackage
            java.lang.reflect.Method method = notificationManagerService.getClass()
                    .getMethod("setNotificationsEnabledForPackage", String.class, int.class, boolean.class);
            method.invoke(notificationManagerService, packageName, 10000, enabled);
            return true;
        } catch (Throwable t) {
            System.err.println("AndroidNotificationServiceAccessor: Failed to setNotificationsEnabled: " + t.getMessage());
            return false;
        }
    }

    @Override
    public boolean isNotificationChannelEnabled(String packageName, String channelId) {
        if (packageName == null || channelId == null || notificationManagerService == null) {
            return true;
        }
        try {
            // Query NotificationChannel importance via NMS internal getNotificationChannelForPackage
            java.lang.reflect.Method method = notificationManagerService.getClass()
                    .getMethod("getNotificationChannelForPackage", String.class, int.class, String.class, boolean.class);
            Object channel = method.invoke(notificationManagerService, packageName, 10000, channelId, false);
            if (channel != null) {
                java.lang.reflect.Method getImportanceMethod = channel.getClass().getMethod("getImportance");
                int importance = (int) getImportanceMethod.invoke(channel);
                return importance != 0; // IMPORTANCE_NONE = 0
            }
        } catch (Throwable t) {
            System.err.println("AndroidNotificationServiceAccessor: Failed to query channel importance: " + t.getMessage());
        }
        return true;
    }

    @Override
    public boolean setNotificationChannelEnabled(String packageName, String channelId, boolean enabled) {
        if (packageName == null || channelId == null || notificationManagerService == null) {
            return false;
        }
        try {
            // Retrieve NotificationChannel, then update importance to IMPORTANCE_NONE (disabled/closed) or IMPORTANCE_DEFAULT
            java.lang.reflect.Method getMethod = notificationManagerService.getClass()
                    .getMethod("getNotificationChannelForPackage", String.class, int.class, String.class, boolean.class);
            Object channel = getMethod.invoke(notificationManagerService, packageName, 10000, channelId, false);
            if (channel != null) {
                java.lang.reflect.Method setImportanceMethod = channel.getClass().getMethod("setImportance", int.class);
                int newImportance = enabled ? 3 : 0; // IMPORTANCE_DEFAULT = 3, IMPORTANCE_NONE = 0
                setImportanceMethod.invoke(channel, newImportance);

                // Update NMS internal channel registry
                java.lang.reflect.Method updateMethod = notificationManagerService.getClass()
                        .getMethod("updateNotificationChannelForPackage", String.class, int.class, channel.getClass());
                updateMethod.invoke(notificationManagerService, packageName, 10000, channel);
                return true;
            }
        } catch (Throwable t) {
            System.err.println("AndroidNotificationServiceAccessor: Failed to update channel importance: " + t.getMessage());
        }
        return false;
    }
}
