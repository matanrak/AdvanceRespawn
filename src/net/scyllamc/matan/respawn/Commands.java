package net.scyllamc.matan.respawn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

	
	public String prefix = ChatColor.DARK_GREEN + "Advance Respawn" + ChatColor.GRAY.toString() + " | ";

	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {

		if (!(sender instanceof Player)) {
			return true;
		}
			
		if (command.getName().equalsIgnoreCase("AdvanceRespawn")) {
			Player player = (Player) sender;

			if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
				player.sendMessage(prefix + ChatColor.GREEN + "Commands:");
				player.sendMessage(ChatColor.GREEN + "  /AR Help " + ChatColor.GRAY + " | Shows this help page");
				player.sendMessage(ChatColor.GREEN + "  /AR Info " + ChatColor.GRAY + " | Shows info about the plugin");
				player.sendMessage(ChatColor.GREEN + "  /AR Reload" + ChatColor.GRAY + " | Reloads the config");
				return true;
			}
			
			
			if (args[0].equalsIgnoreCase("settings")) {
			
				player.sendMessage(ChatColor.GREEN + "  /AR Settings List " + ChatColor.GRAY + " | Lists all editable settings");
				player.sendMessage(ChatColor.GREEN + "  /AR Settings Edit {setting} {new_value}" + ChatColor.GRAY + " | Edit a specified setting");
			}
			
				
			if (args[0].equalsIgnoreCase("reload")) {
				if (player.hasPermission("AdvanceRespawn.reload")) {
					
					Bukkit.getPluginManager().getPlugin("AdvanceRespawn").reloadConfig();

					player.sendMessage(prefix + ChatColor.GREEN + "Config reloaded!");
					return true;
				} else {
					player.sendMessage(prefix + ChatColor.RED + "You do not have the premission needed to use this command.");
					return true;
				}
			}

			
			if (args[0].equalsIgnoreCase("info")) {

				String version = Bukkit.getPluginManager().getPlugin("AdvanceRespawn").getDescription().getVersion();

				player.sendMessage(prefix + ChatColor.GREEN + "Info:");
				player.sendMessage(ChatColor.GREEN + "  Made by " + ChatColor.GRAY + "Matan");
				player.sendMessage(ChatColor.GREEN + "  Version: " + ChatColor.GRAY + version);
				player.sendMessage(ChatColor.GREEN + "  Link: " + ChatColor.GRAY + "http://bit.ly/1UrBeYp");
				return true;
			}

			player.sendMessage(prefix + ChatColor.RED + " " + args[0] + " is not a known sub command!");

		}
		return false;
	}

}
