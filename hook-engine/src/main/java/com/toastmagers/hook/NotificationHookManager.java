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

    public NotificationHookManager(SystemHookBridge hookBridge, Object ruleEngine, Object stormBreaker, Object logManager) {
        this.hookBridge = hookBridge;
        this.ruleEngine = ruleEngine;
        this.stormBreaker = stormBreaker;
        this.logManager = logManager;
    }

    /**
     * Installs the hook on NotificationManagerService.enqueueToast.
     */
    public boolean installHooks() {
        try {
            String className = "com.android.server.notification.NotificationManagerService";
            String methodName = "enqueueToast";
            String signature = "(Ljava/lang/String;Landroid/os/IBinder;Ljava/lang/CharSequence;Landroid/os/IBinder;I)V";

            return hookBridge.hookMethod(className, methodName, signature, new SystemHookBridge.HookCallback() {
                @Override
                public Object onHookTriggered(Object[] args) throws Throwable {
                    return handleEnqueueToastHook(args);
                }
            });
        } catch (Throwable t) {
            System.err.println("Safe log: Fail-open: Failed to install notification hooks. Fallback to normal system behavior.");
            return false;
        }
    }

    /**
     * Intercepts enqueueToast, performs rate-limiting/breaker checks, rule matching, and desensitized logging.
     * Enforces Fail-open: any exception defaults to allowing the toast (returning null / standard continue).
     */
    private Object handleEnqueueToastHook(Object[] args) {
        try {
            if (args == null || args.length < 3) {
                return null; // Fallback: allow standard system execution
            }

            String pkg = (String) args[0];
            CharSequence text = (CharSequence) args[2];
            String message = text != null ? text.toString() : "";

            // 1. Perform rate limiting / Toast storm breaker checks (T-PERF-02)
            if (stormBreaker != null) {
                // Use reflection or standard check
                boolean allowed = true;
                try {
                    java.lang.reflect.Method allowMethod = stormBreaker.getClass().getMethod("allowRequest");
                    allowed = (boolean) allowMethod.invoke(stormBreaker);
                } catch (Exception ignored) {}

                if (!allowed) {
                    System.err.println("Safe log: Toast storm breaker active. Blocked high-frequency toast from: " + pkg);
                    return "Blocked"; // Intercepted: return or block (custom non-null token triggers block)
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
}
