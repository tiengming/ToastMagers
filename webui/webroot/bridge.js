/**
 * ToastMagers WebUI Bridge (T-WEB-02 ~ T-WEB-04).
 * Encapsulates unified JS bridge calls. If a native bridge exists in the Root Manager
 * WebView environment, it will delegate to it; otherwise, it falls back to rich mock data.
 */
(function(global) {
    const MOCK_PASSWORD = "root";

    // Initialize mock store in localStorage if not present
    if (!localStorage.getItem("toastmagers_auth")) {
        localStorage.setItem("toastmagers_auth", "false");
    }

    const defaultApps = [
        { packageName: "com.android.systemui", name: "System UI", isWhitelisted: true, blockAllToasts: false, silent: false, channelsBlocked: [] },
        { packageName: "com.tencent.mm", name: "WeChat (微信)", isWhitelisted: false, blockAllToasts: false, silent: false, channelsBlocked: [] },
        { packageName: "com.eg.android.AlipayGphone", name: "Alipay (支付宝)", isWhitelisted: false, blockAllToasts: false, silent: false, channelsBlocked: [] },
        { packageName: "com.rogue.spam.app", name: "Spammy Rogue App", isWhitelisted: false, blockAllToasts: true, silent: true, channelsBlocked: ["ad_channel", "marketing"] },
        { packageName: "com.marketing.notification", name: "Marketing Service", isWhitelisted: false, blockAllToasts: false, silent: false, channelsBlocked: ["push_channel"] }
    ];

    if (!localStorage.getItem("toastmagers_apps")) {
        localStorage.setItem("toastmagers_apps", JSON.stringify(defaultApps));
    }

    const defaultLogs = [
        { id: 1, timestamp: "2026-07-26 15:30:12", packageName: "com.rogue.spam.app", action: "BLOCKED", originBody: "领红包啦！快点击领取100元现金！" },
        { id: 2, timestamp: "2026-07-26 15:32:45", packageName: "com.marketing.notification", action: "BLOCKED", originBody: "【限时特惠】您的账户有1个专属优惠券未领取！" },
        { id: 3, timestamp: "2026-07-26 15:35:01", packageName: "com.tencent.mm", action: "ALLOWED", originBody: "您收到一条新消息" },
        { id: 4, timestamp: "2026-07-26 15:40:22", packageName: "com.eg.android.AlipayGphone", action: "ALLOWED", originBody: "支付成功 5.00元" }
    ];

    if (!localStorage.getItem("toastmagers_logs")) {
        localStorage.setItem("toastmagers_logs", JSON.stringify(defaultLogs));
    }

    const ToastMagersBridge = {
        /**
         * Checks if authentication is required.
         */
        isAuthRequired: function() {
            return true;
        },

        /**
         * Checks if the user is currently authenticated.
         */
        isAuthenticated: function() {
            return localStorage.getItem("toastmagers_auth") === "true";
        },

        /**
         * Verifies the password (T-WEB-04).
         */
        verifyAuth: function(password) {
            if (password === MOCK_PASSWORD) {
                localStorage.setItem("toastmagers_auth", "true");
                return { success: true };
            }
            return { success: false, message: "Invalid password" };
        },

        /**
         * Logs out the user.
         */
        logout: function() {
            localStorage.setItem("toastmagers_auth", "false");
        },

        /**
         * Retrieves the application list (T-WEB-02).
         */
        getAppList: function() {
            return JSON.parse(localStorage.getItem("toastmagers_apps"));
        },

        /**
         * Toggles the block rule for a given package (T-WEB-02).
         */
        toggleAppBlock: function(packageName, blockAllToasts) {
            const apps = this.getAppList();
            const app = apps.find(a => a.packageName === packageName);
            if (app) {
                app.blockAllToasts = blockAllToasts;
                localStorage.setItem("toastmagers_apps", JSON.stringify(apps));
                return { success: true };
            }
            return { success: false, message: "App not found" };
        },

        /**
         * Retrieves Toast/Notification tracker logs (T-WEB-03).
         * Privacy Engineering constraints: Raw bodies are returned, but by default 
         * the UI layer masks or hides them.
         */
        getToastLogs: function() {
            return JSON.parse(localStorage.getItem("toastmagers_logs"));
        },

        /**
         * Clear all tracker logs.
         */
        clearLogs: function() {
            localStorage.setItem("toastmagers_logs", JSON.stringify([]));
            return { success: true };
        },

        /**
         * Retrieves notification intercept statistics (Epic K / T-STAT-03).
         */
        getStatistics: function() {
            const defaultStats = {
                totalIntercepts: 154,
                totalAllowed: 420,
                totalToastIntercepts: 98,
                totalNotificationIntercepts: 56,
                topBlockedApps: [
                    { packageName: "com.rogue.spam.app", name: "Spammy Rogue App", count: 86 },
                    { packageName: "com.marketing.notification", name: "Marketing Service", count: 42 },
                    { packageName: "com.ad.push.service", name: "Ad Push SDK", count: 18 },
                    { packageName: "com.game.banner", name: "Mini Game Ads", count: 8 }
                ],
                dailyStats: [
                    { date: "2026-07-20", count: 12 },
                    { date: "2026-07-21", count: 18 },
                    { date: "2026-07-22", count: 25 },
                    { date: "2026-07-23", count: 19 },
                    { date: "2026-07-24", count: 32 },
                    { date: "2026-07-25", count: 28 },
                    { date: "2026-07-26", count: 20 }
                ]
            };
            return JSON.parse(localStorage.getItem("toastmagers_stats") || JSON.stringify(defaultStats));
        },

        /**
         * Resets statistics data.
         */
        resetStatistics: function() {
            const emptyStats = {
                totalIntercepts: 0,
                totalAllowed: 0,
                totalToastIntercepts: 0,
                totalNotificationIntercepts: 0,
                topBlockedApps: [],
                dailyStats: []
            };
            localStorage.setItem("toastmagers_stats", JSON.stringify(emptyStats));
            return { success: true };
        }
    };

    global.ToastMagersBridge = ToastMagersBridge;
})(window);
