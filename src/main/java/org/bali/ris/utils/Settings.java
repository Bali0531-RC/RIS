package org.bali.ris.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Settings {
    private File file;
    private FileConfiguration config;

    public Settings(String fileName, JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void addSpawnInfo(String id, String world, double x, double y, double z, String item, int value) {
        this.config.set("ItemInfos." + id + ".world", world);
        this.config.set("ItemInfos." + id + ".x", x);
        this.config.set("ItemInfos." + id + ".y", y);
        this.config.set("ItemInfos." + id + ".z", z);
        this.config.set("ItemInfos." + id + ".item", item);
        this.config.set("ItemInfos." + id + ".value", value);
        saveConfig();
    }

    public boolean removeSpawnInfo(String id) {
        if (this.config.contains("ItemInfos." + id)) {
            this.config.set("ItemInfos." + id, null);
            saveConfig();
            return true;
        }
        return false;
    }
    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}