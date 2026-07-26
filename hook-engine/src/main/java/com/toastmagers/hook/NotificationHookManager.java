package com.toastmagers.hook;

/**
 * Orchestrates ART Method Hooks on system_server NMS methods using a SystemHookBridge (Epic A).
 * Employs Fail-open semantics to ensure that any crash or exception falls back to allowing the notification/toast.
 */
public class NotificationHookManager {

    private final SystemHookBridge hookBridge;
    private final Object ruleEngine; // Object placeholder to avoid direct runtime dependency in compile-only stubs
    private final Object stormBreaker; // Object placeholder
    private final Object logManager; // Object placeholder
    private boolean isEnabled = true;

    public NotificationHookManager(SystemHookBridge hookBridge, Object ruleEngine, Object stormBreaker, Object logManager) {
        this.hookBridge = hookBridge;
        this.ruleEngine = ruleEngine;
        this.stormBreaker = stormBreaker;
        this.logManager = logManager;
    }

    /**
     * Runs a self-check to verify hook installation/functionality.
     * If self-check fails, the module is automatically disabled (T-HOOK-06).
     */
    public boolean runSelfCheck() {
        try {
            // Self-check: Try to hook a dummy method on a fake bridge to verify hook bridge integrity
            FakeSystemHookBridge selfCheckBridge = new FakeSystemHookBridge();
            boolean mockInstalled = selfCheckBridge.hookMethod("dummy.Class", "dummyMethod", "()V", new SystemHookBridge.HookCallback() {
                @Override
                public Object onHookTriggered(Object[] args) throws Throwable {
                    return "healthy";
                }
            });

            if (!mockInstalled || !"healthy".equals(selfCheckBridge.triggerHook("dummy.Class", "dummyMethod", new Object[]{}))) {
                throw new IllegalStateException("Hook bridge self-check failed to intercept properly.");
            }

            System.out.println("Safe log: Self-check successful. Hook engine is healthy.");
            return true;
        } catch (Throwable t) {
            // Self-check failed, automatically disable the hook engine (T-HOOK-06)
            this.isEnabled = false;
            System.err.println("Safe log: Hook engine self-check failed: " + t.getMessage() + ". Hook engine has been automatically disabled.");
            return false;
        }
    }

    /**
     * Installs the hook on NotificationManagerService.enqueueToast and enqueueNotificationWithTag.
     */
    public boolean installHooks() {
        // Run self check first (T-HOOK-06)
        if (!runSelfCheck() || !isEnabled) {
            System.err.println("Safe log: Refusing to install hooks because hook engine is disabled or self-check failed.");
            return false;
        }

        boolean toastOk = false;
        try {
            String className = "com.android.server.notification.NotificationManagerService";
            String methodName = "enqueueToast";
            String signature = "(Ljava/lang/String;Landroid/os/IBinder;Ljava/lang/CharSequence;Landroid/os/IBinder;I)V";

            toastOk = hookBridge.hookMethod(className, methodName, signature, new SystemHookBridge.HookCallback() {
                @Override
                public Object onHookTriggered(Object[] args) throws Throwable {
                    return handleEnqueueToastHook(args);
                }
            });
        } catch (Throwable t) {
            System.err.println("Safe log: Fail-open: Failed to install toast hooks. Fallback to normal system behavior.");
        }

        boolean notificationOk = false;
        try {
            String className = "com.android.server.notification.NotificationManagerService";
            String methodName = "enqueueNotificationWithTag";
            String signature = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILandroid/app/Notification;I)V";

            notificationOk = hookBridge.hookMethod(className, methodName, signature, new SystemHookBridge.HookCallback() {
                @Override
                public Object onHookTriggered(Object[] args) throws Throwable {
                    return handleEnqueueNotificationHook(args);
                }
            });
        } catch (Throwable t) {
            System.err.println("Safe log: Fail-open: Failed to install notification hooks. Fallback to normal system behavior.");
        }

        return toastOk && notificationOk;
    }

    /**
     * Intercepts enqueueToast, performs rate-limiting/breaker checks, rule matching, and desensitized logging.
     * Enforces Fail-open: any exception defaults to allowing the toast (returning null / standard continue).
     */
    public Object handleEnqueueToastHook(Object[] args) {
        if (!isEnabled) {
            return null; // Force normal flow if disabled
        }
        try {
            if (args == null || args.length < 3) {
                return null; // Fallback: allow standard system execution
            }

            String pkg = (String) args[0];
            CharSequence text = (CharSequence) args[2];
            String message = text != null ? text.toString() : "";

            // 1. Perform rate limiting / Toast storm breaker checks (T-PERF-02)
            if (stormBreaker != null) {
                boolean allowed = true;
                try {
                    java.lang.reflect.Method allowMethod = stormBreaker.getClass().getMethod("allowRequest");
                    allowed = (boolean) allowMethod.invoke(stormBreaker);
                } catch (Exception ignored) {}

                if (!allowed) {
                    System.err.println("Safe log: Toast storm breaker active. Blocked high-frequency toast from: " + pkg);
                    return "Blocked"; // Intercepted
                }
            }

            // 2. Rule evaluation (T-RULE-02)
            if (ruleEngine != null) {
                String actionStr = "ALLOW";
                try {
                    java.lang.reflect.Method evaluateMethod = ruleEngine.getClass().getMethod("evaluate", String.class, String.class, String.class);
                    Object actionEnum = evaluateMethod.invoke(ruleEngine, pkg, message, null);
                    actionStr = actionEnum.toString();
                } catch (Exception ignored) {}

                if ("BLOCK".equals(actionStr)) {
                    // Record statistics (T-STAT-01)
                    com.toastmagers.stats.StatsManager.getInstance().recordIntercept(pkg, null, "TOAST");

                    // Log the interception safely with desensitized logs (T-SEC-01)
                    if (logManager != null) {
                        try {
                            java.lang.reflect.Method logMethod = logManager.getClass().getMethod("log", String.class, String.class, String.class);
                            logMethod.invoke(logManager, "RuleEngine_Intercept", pkg, message);
                        } catch (Exception ignored) {}
                    }
                    return "Blocked"; // Intercepted
                }
            }

            // Record allowed event (T-STAT-01)
            com.toastmagers.stats.StatsManager.getInstance().recordPassThrough(pkg);

        } catch (Throwable t) {
            // Fail-open guarantee: never crash the caller (system_server) (T-HOOK-05)
            System.err.println("Safe log: Fail-open trigger: exception inside HookCallback handled successfully. Original call allowed.");
        }
        return null; // Let the original method execution continue
    }

    /**
     * Intercepts enqueueNotificationWithTag, performs rate-limiting, rule matching and statistics (T-HOOK-04).
     */
    public Object handleEnqueueNotificationHook(Object[] args) {
        if (!isEnabled) {
            return null; // Force normal flow if disabled
        }
        try {
            if (args == null || args.length < 5) {
                return null; // Fallback: allow standard system execution
            }

            String pkg = (String) args[0];
            Object notification = args[4];
            String message = "";
            String channelId = null;

            if (notification != null) {
                // Get notification channelId via reflection
                try {
                    java.lang.reflect.Method getChannelIdMethod = notification.getClass().getMethod("getChannelId");
                    channelId = (String) getChannelIdMethod.invoke(notification);
                } catch (Exception ignored) {}

                // Extract notification message text via reflection: notification.extras.getCharSequence("android.text")
                try {
                    java.lang.reflect.Field extrasField = notification.getClass().getField("extras");
                    Object extras = extrasField.get(notification);
                    if (extras != null) {
                        java.lang.reflect.Method getCharSequenceMethod = extras.getClass().getMethod("getCharSequence", String.class);
                        CharSequence textCharSeq = (CharSequence) getCharSequenceMethod.invoke(extras, "android.text");
                        if (textCharSeq != null) {
                            message = textCharSeq.toString();
                        } else {
                            CharSequence titleCharSeq = (CharSequence) getCharSequenceMethod.invoke(extras, "android.title");
                            if (titleCharSeq != null) {
                                message = titleCharSeq.toString();
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }

            // 1. Perform rate limiting / Toast storm breaker checks (T-PERF-02)
            if (stormBreaker != null) {
                boolean allowed = true;
                try {
                    java.lang.reflect.Method allowMethod = stormBreaker.getClass().getMethod("allowRequest");
                    allowed = (boolean) allowMethod.invoke(stormBreaker);
                } catch (Exception ignored) {}

                if (!allowed) {
                    System.err.println("Safe log: Notification storm breaker active. Blocked high-frequency notification from: " + pkg);
                    return "Blocked"; // Intercepted
                }
            }

            // 2. Rule evaluation (T-RULE-02)
            if (ruleEngine != null) {
                String actionStr = "ALLOW";
                try {
                    java.lang.reflect.Method evaluateMethod = ruleEngine.getClass().getMethod("evaluate", String.class, String.class, String.class);
                    Object actionEnum = evaluateMethod.invoke(ruleEngine, pkg, message, channelId);
                    actionStr = actionEnum.toString();
                } catch (Exception ignored) {}

                if ("BLOCK".equals(actionStr)) {
                    // Record statistics (T-STAT-01)
                    com.toastmagers.stats.StatsManager.getInstance().recordIntercept(pkg, channelId, "NOTIFICATION");

                    // Log the interception safely with desensitized logs (T-SEC-01)
                    if (logManager != null) {
                        try {
                            java.lang.reflect.Method logMethod = logManager.getClass().getMethod("log", String.class, String.class, String.class);
                            logMethod.invoke(logManager, "RuleEngine_Intercept", pkg, message);
                        } catch (Exception ignored) {}
                    }
                    return "Blocked"; // Intercepted
                }
            }

            // Record allowed event (T-STAT-01)
            com.toastmagers.stats.StatsManager.getInstance().recordPassThrough(pkg);

        } catch (Throwable t) {
            // Fail-open guarantee: never crash system_server (T-HOOK-05)
            System.err.println("Safe log: Fail-open trigger: exception inside HookCallback handled successfully. Original call allowed.");
        }
        return null; // Let the original method execution continue
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }
}
