package org.bali.ris.utils;

import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

public class Messages {
    private Map<String, String> messages = new HashMap<>();

    public Messages(Settings settings) {
        for (String key : settings.getConfig().getKeys(false)) {
            messages.put(key, ChatColor.translateAlternateColorCodes('&', settings.getConfig().getString(key)));
        }
    }

    public String getMessage(String key) {
        return messages.get(key);
    }
}