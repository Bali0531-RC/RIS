package org.bali.ris.utils;

import org.bali.ris.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ItemSpawnTask extends BukkitRunnable {
    private Main plugin;
    private List<Item> spawnedItems = new ArrayList<>();

    public ItemSpawnTask(Main plugin) {
        this.plugin = plugin;
    }
    public Main getPlugin() {
        return plugin;
    }
    @Override
    public void run() {
        // Despawn all previously spawned items
        for (Item item : spawnedItems) {
            item.remove();
        }
        spawnedItems.clear();

        // Spawn new items
        for (String key : plugin.getSettings().getConfig().getConfigurationSection("ItemInfos").getKeys(false)) {
            String worldName = plugin.getSettings().getConfig().getString("ItemInfos." + key + ".world");
            double x = plugin.getSettings().getConfig().getDouble("ItemInfos." + key + ".x");
            double y = plugin.getSettings().getConfig().getDouble("ItemInfos." + key + ".y");
            double z = plugin.getSettings().getConfig().getDouble("ItemInfos." + key + ".z");
            Material itemMaterial = Material.valueOf(plugin.getSettings().getConfig().getString("ItemInfos." + key + ".item"));

            Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
            Item item = location.getWorld().dropItem(location, new ItemStack(itemMaterial));

            spawnedItems.add(item);
        }
    }

    public List<Item> getSpawnedItems() {
        return spawnedItems;
    }
}