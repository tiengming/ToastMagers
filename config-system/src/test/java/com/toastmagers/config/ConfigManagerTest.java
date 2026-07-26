package com.toastmagers.config;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManagerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testLoadDefaultWhenFileDoesNotExist() {
        File file = new File(tempFolder.getRoot(), "config.json");
        ConfigManager manager = new ConfigManager(file);
        
        manager.load();
        
        Assert.assertTrue(file.exists());
        ConfigModel config = manager.getConfig();
        Assert.assertNotNull(config);
        Assert.assertEquals(1, config.version);
        Assert.assertTrue(config.settings.enable_toast_tracker);
    }

    @Test
    public void testFallbackWhenFileIsMalformed() throws IOException {
        File file = tempFolder.newFile("config_malformed.json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("{ \"version\": 1, \"settings\": { \"enable_toast_tracker\": "); // incomplete / malformed
        }

        ConfigManager manager = new ConfigManager(file);
        manager.load(); // should fallback to default safely and rewrite

        ConfigModel config = manager.getConfig();
        Assert.assertNotNull(config);
        Assert.assertEquals(1, config.version);
        Assert.assertTrue(config.settings.enable_toast_tracker);
    }

    @Test
    public void testSchemaValidation() {
        ConfigModel model = new ConfigModel();
        model.version = 0; // Invalid
        try {
            model.validate();
            Assert.fail("Should throw IllegalArgumentException on invalid version");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("version"));
        }
    }
}
