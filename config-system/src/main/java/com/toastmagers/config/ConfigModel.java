package com.toastmagers.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigModel {
    public static class Settings {
        public boolean enable_toast_tracker = true;
        public boolean show_toast_source_overlay = false;
        public String log_level = "INFO";
    }

    public static class WebUi {
        public boolean enabled = true;
        public boolean require_auth = false;
    }

    public static class AppRule {
        public boolean block_all_toasts = false;
        public List<String> block_notification_channels = new ArrayList<>();
        public boolean force_silent = false;
        public int auto_dismiss_delay_ms = 0;
        public boolean sync_system_state = false;
    }

    public int version = 1;
    public Settings settings = new Settings();
    public WebUi webui = new WebUi();
    public List<String> whitelist_packages = new ArrayList<>();
    public List<Map<String, String>> global_rules = new ArrayList<>();
    public Map<String, AppRule> app_rules = new HashMap<>();

    public void validate() {
        if (version < 1) {
            throw new IllegalArgumentException("Config version must be at least 1");
        }
        if (settings == null) {
            settings = new Settings();
        }
        if (webui == null) {
            webui = new WebUi();
        }
        if (whitelist_packages == null) {
            whitelist_packages = new ArrayList<>();
        }
        if (global_rules == null) {
            global_rules = new ArrayList<>();
        }
        if (app_rules == null) {
            app_rules = new HashMap<>();
        }
        
        // Settings validation
        if (settings.log_level == null || (!settings.log_level.equals("DEBUG") && !settings.log_level.equals("INFO") && !settings.log_level.equals("WARN") && !settings.log_level.equals("ERROR"))) {
            settings.log_level = "INFO";
        }
    }
}
