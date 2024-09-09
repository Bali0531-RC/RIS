package org.bali.ris.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bali.ris.Main;

import java.util.ArrayList;
import java.util.List;

public class RISCommand implements CommandExecutor, TabCompleter {
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
            switch (args[0].toLowerCase()) {
                case "reload":
                    if (sender.hasPermission("RIS.reload")) {
                        plugin.getSettings().saveConfig();
                        sender.sendMessage(plugin.getMessages().getMessage("reload"));
                    } else {
                        sender.sendMessage(plugin.getMessages().getMessage("no_permission"));
                    }
                    break;
                case "respawn":
                    if (sender.hasPermission("RIS.respawn")) {
                        itemSpawnTask.run(); // Immediately run the task
                        sender.sendMessage(plugin.getMessages().getMessage("respawn"));
                    } else {
                        sender.sendMessage(plugin.getMessages().getMessage("no_permission"));
                    }
                    break;
                case "add":
                    if (sender.hasPermission("RIS.add")) {
                        if (args.length == 5 && sender instanceof Player) {
                            String id = args[1];
                            String itemName = args[2].toUpperCase();
                            int value;
                            try {
                                value = Integer.parseInt(args[3]);
                            } catch (NumberFormatException e) {
                                sender.sendMessage(ChatColor.RED + "Value must be a number.");
                                return true;
                            }
                            String commandStr = args[4];
                            Player player = (Player) sender;
                            plugin.getSettings().addSpawnInfo(id, player.getWorld().getName(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), itemName, value, commandStr);
                            sender.sendMessage(plugin.getMessages().getMessage("item_spawn_added").replace("$ID", id));
                        } else {
                            sender.sendMessage(plugin.getMessages().getMessage("usage_add"));
                        }
                    } else {
                        sender.sendMessage(plugin.getMessages().getMessage("no_permission"));
                    }
                    break;
                case "delete":
                    if (sender.hasPermission("RIS.delete")) {
                        if (args.length == 2) {
                            String id = args[1];
                            if (plugin.getSettings().removeSpawnInfo(id)) {
                                sender.sendMessage(plugin.getMessages().getMessage("item_spawn_deleted").replace("$ID", id));
                            } else {
                                sender.sendMessage(plugin.getMessages().getMessage("item_spawn_not_found").replace("$ID", id));
                            }
                        } else {
                            sender.sendMessage(plugin.getMessages().getMessage("usage_delete"));
                        }
                    } else {
                        sender.sendMessage(plugin.getMessages().getMessage("no_permission"));
                    }
                    break;
                case "help":
                    sender.sendMessage(plugin.getMessages().getMessage("help_header"));
                    sender.sendMessage(plugin.getMessages().getMessage("help_reload"));
                    sender.sendMessage(plugin.getMessages().getMessage("help_respawn"));
                    sender.sendMessage(plugin.getMessages().getMessage("help_add"));
                    sender.sendMessage(plugin.getMessages().getMessage("help_delete"));
                    break;
                default:
                    sender.sendMessage(plugin.getMessages().getMessage("unknown_command"));
            }
        } else {
            sender.sendMessage(plugin.getMessages().getMessage("unknown_command"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) { // First argument completion
            if ("ris".equalsIgnoreCase(command.getName())) {
                if (sender.hasPermission("RIS.reload")) {
                    completions.add("reload");
                }
                if (sender.hasPermission("RIS.respawn")) {
                    completions.add("respawn");
                }
                if (sender.hasPermission("RIS.add")) {
                    completions.add("add");
                }
                if (sender.hasPermission("RIS.delete")) {
                    completions.add("delete");
                }
                completions.add("help");
            }
        }
        return completions;
    }
}