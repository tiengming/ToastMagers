package com.toastmagers.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Handles configuration file loading, validation, and safe error fallback (T-CFG-01).
 * Logs only sanitized errors to prevent leaks of sensitive configurations.
 */
public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File configFile;
    private ConfigModel currentConfig;

    public ConfigManager(File configFile) {
        this.configFile = configFile;
        this.currentConfig = new ConfigModel();
    }

    /**
     * Loads config.json. If file does not exist, is invalid or malformed,
     * it falls back to a clean default config and persists it.
     */
    public synchronized void load() {
        if (!configFile.exists()) {
            System.out.println("Config file not found, creating default configuration.");
            currentConfig = new ConfigModel();
            save();
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            ConfigModel loaded = GSON.fromJson(reader, ConfigModel.class);
            if (loaded == null) {
                throw new IllegalArgumentException("Config is empty or null.");
            }
            loaded.validate();
            this.currentConfig = loaded;
        } catch (JsonSyntaxException e) {
            // T-CFG-01 fallback and safe logging
            System.err.println("Safe log: Configuration file contains malformed JSON syntax. Falling back to default configuration safely.");
            this.currentConfig = new ConfigModel();
            save();
        } catch (Exception e) {
            System.err.println("Safe log: Failed to load config safely due to unexpected error. Falling back to default.");
            this.currentConfig = new ConfigModel();
            save();
        }
    }

    public synchronized void save() {
        try {
            File parent = configFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(currentConfig, writer);
            }
        } catch (IOException e) {
            System.err.println("Safe log: Failed to save config safely.");
        }
    }

    public synchronized ConfigModel getConfig() {
        return currentConfig;
    }

    public synchronized void setConfig(ConfigModel config) {
        if (config != null) {
            config.validate();
            this.currentConfig = config;
        }
    }
}
