package org.bali.ris;

import org.bali.ris.utils.Messages;
import org.bali.ris.utils.RISCommand;
import org.bali.ris.utils.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private Settings settings;
    private Messages messages;

    @Override
    public void onEnable() {
        settings = new Settings("Settings.yml", this);
        messages = new Messages(new Settings("Messages.yml", this));
        RISCommand risCommand = new RISCommand(this);
        this.getCommand("ris").setExecutor(risCommand);
        this.getCommand("ris").setTabCompleter(risCommand);
        risCommand.getItemSpawnTask().run();
        // Register the PlayerPickupItemEvent
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerPickupItem(PlayerPickupItemEvent event) {
                Item item = event.getItem();
                Player player = event.getPlayer();

                // Check if the item is one of the spawned items
                if (risCommand.getItemSpawnTask().getSpawnedItems().contains(item)) {
                    // Cancel the pickup event so the item doesn't get added to the player's inventory
                    event.setCancelled(true);

                    // Despawn the item
                    item.remove();

                    // Get the value from the Settings.yml file
                    for (String key : Main.this.getSettings().getConfig().getConfigurationSection("ItemInfos").getKeys(false)) {
                        Material itemMaterial = Material.valueOf(Main.this.getSettings().getConfig().getString("ItemInfos." + key + ".item"));
                        if (item.getItemStack().getType() == itemMaterial) {
                            int value = Main.this.getSettings().getConfig().getInt("ItemInfos." + key + ".value");

                            // Run the command
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " " + value);
                            // Send the received_value message to the player
                            String message = Main.this.getMessages().getMessage("received_value").replace("$VALUE", String.valueOf(value));
                            player.sendMessage(message);
                            // Schedule the item to respawn after 4 minutes
                            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.this, new Runnable() {
                                @Override
                                public void run() {
                                    String worldName = Main.this.getSettings().getConfig().getString("ItemInfos." + key + ".world");
                                    double x = Main.this.getSettings().getConfig().getDouble("ItemInfos." + key + ".x");
                                    double y = Main.this.getSettings().getConfig().getDouble("ItemInfos." + key + ".y");
                                    double z = Main.this.getSettings().getConfig().getDouble("ItemInfos." + key + ".z");

                                    Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                                    Item item = location.getWorld().dropItem(location, new ItemStack(itemMaterial));

                                    risCommand.getItemSpawnTask().getSpawnedItems().add(item);
                                }
                            }, 4800);
                            break;
                        }
                    }
                }
            }
        }, this);
    }

    @Override
    public void onDisable() {
        for (String key : this.getSettings().getConfig().getConfigurationSection("ItemInfos").getKeys(false)) {
            String worldName = this.getSettings().getConfig().getString("ItemInfos." + key + ".world");
            for (Entity entity : Bukkit.getWorld(worldName).getEntities()) {
                if (entity instanceof Item) {
                    Item item = (Item) entity;
                    Material itemMaterial = Material.valueOf(this.getSettings().getConfig().getString("ItemInfos." + key + ".item"));
                    if (item.getItemStack().getType() == itemMaterial) {
                        entity.remove();
                    }
                }
            }
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public Messages getMessages() {
        return messages;
    }
}