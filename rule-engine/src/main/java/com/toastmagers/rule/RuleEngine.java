package com.toastmagers.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core Rule Engine for ToastMagers (Epic B).
 * Supports whitelist packages, app-specific rules, and global rules.
 */
public class RuleEngine {

    private final List<String> whitelistPackages = new ArrayList<>();
    private final List<Rule> globalRules = new ArrayList<>();
    private final Map<String, AppRuleConfig> appRules = new HashMap<>();

    public static class AppRuleConfig {
        private boolean blockAllToasts;
        private List<String> blockNotificationChannels;
        private boolean forceSilent;
        private int autoDismissDelayMs;
        private boolean syncSystemState;
        private List<Rule> rules = new ArrayList<>();

        public AppRuleConfig(boolean blockAllToasts, List<String> blockNotificationChannels, boolean forceSilent) {
            this.blockAllToasts = blockAllToasts;
            this.blockNotificationChannels = blockNotificationChannels == null ? new ArrayList<>() : blockNotificationChannels;
            this.forceSilent = forceSilent;
        }

        public boolean isBlockAllToasts() {
            return blockAllToasts;
        }

        public List<String> getBlockNotificationChannels() {
            return blockNotificationChannels;
        }

        public boolean isForceSilent() {
            return forceSilent;
        }

        public List<Rule> getRules() {
            return rules;
        }

        public void addRule(Rule rule) {
            this.rules.add(rule);
        }
    }

    public void addWhitelistPackage(String packageName) {
        if (packageName != null) {
            whitelistPackages.add(packageName);
        }
    }

    public void addGlobalRule(Rule rule) {
        if (rule != null) {
            globalRules.add(rule);
        }
    }

    public void setAppRule(String packageName, AppRuleConfig config) {
        if (packageName != null && config != null) {
            appRules.put(packageName, config);
        }
    }

    /**
     * Determines whether to allow, block, or silences a Toast or notification.
     *
     * @param packageName The originating application's package name
     * @param content     The text content of the Toast or notification
     * @param channelId   The notification channel ID (nullable)
     * @return Rule.Action indicating the decision
     */
    public Rule.Action evaluate(String packageName, String content, String channelId) {
        // 1. Whitelist Bypass (T-RULE-03)
        if (whitelistPackages.contains(packageName)) {
            return Rule.Action.ALLOW;
        }

        // 2. App-specific rules (T-RULE-02)
        AppRuleConfig appConfig = appRules.get(packageName);
        if (appConfig != null) {
            if (appConfig.isBlockAllToasts()) {
                return Rule.Action.BLOCK;
            }
            if (channelId != null && appConfig.getBlockNotificationChannels().contains(channelId)) {
                return Rule.Action.BLOCK;
            }
            if (appConfig.isForceSilent()) {
                return Rule.Action.SILENT;
            }

            // Evaluate specific app rules
            for (Rule rule : appConfig.getRules()) {
                if (rule.matches(content)) {
                    return rule.getAction();
                }
            }
        }

        // 3. Global rules (T-RULE-02)
        for (Rule rule : globalRules) {
            if (rule.matches(content)) {
                return rule.getAction();
            }
        }

        return Rule.Action.ALLOW;
    }

    public void clear() {
        whitelistPackages.clear();
        globalRules.clear();
        appRules.clear();
    }
}
