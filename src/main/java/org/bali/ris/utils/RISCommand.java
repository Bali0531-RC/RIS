package org.bali.ris.utils;

import org.bali.ris.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RISCommand implements CommandExecutor {
    private Main plugin;
    private ItemSpawnTask itemSpawnTask;

    public RISCommand(Main plugin) {
        this.plugin = plugin;
        this.itemSpawnTask = new ItemSpawnTask(plugin);
        this.itemSpawnTask.runTaskTimer(plugin, 0L, 20L * 60 * 4); // Run the task every 4 minutes
    }
    public ItemSpawnTask getItemSpawnTask() {
        return itemSpawnTask;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("RIS.reload")) {
                    plugin.getSettings().saveConfig();
                    sender.sendMessage(plugin.getMessages().getMessage("reload"));
                } else {
                    sender.sendMessage("You do not have permission to use this command.");
                }
            } else if (args[0].equalsIgnoreCase("respawn")) {
                if (sender.hasPermission("RIS.respawn")) {
                    itemSpawnTask.run(); // Immediately run the task
                    sender.sendMessage(plugin.getMessages().getMessage("respawn"));
                } else {
                    sender.sendMessage("You do not have permission to use this command.");
                }
            }
        }
        return true;
    }
}