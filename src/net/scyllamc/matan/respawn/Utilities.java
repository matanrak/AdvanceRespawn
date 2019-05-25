package net.scyllamc.matan.respawn;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

import net.scyllamc.matan.respawn.titles.Title;

public class Utilities {

	
	public static String getDeathMessage(Player player) {
				
		if (Main.deathCauseCache.containsKey(player.getUniqueId()))
			return Main.deathCauseCache.get(player.getUniqueId());
		
		String defaultCause = format(Main.casue_dictionary.getProperty("UNKNOWN"), player);
		PlayerDeathEvent event = Events.deathEvents.get(player.getUniqueId());
		String cause = defaultCause;
		
		if (!Events.deathEvents.containsKey(player.getUniqueId()) || event == null || event.getEntity() == null || event.getEntity().getLastDamageCause() == null)
			cause = defaultCause;
		
		if (!(event.getEntity() instanceof Player))
			cause = "";
		
		DamageCause damageCause = event.getEntity().getLastDamageCause().getCause();
		
		if (Main.casue_dictionary.containsKey(damageCause.toString())) 
			cause = format(Main.casue_dictionary.getProperty(damageCause.toString()), player);

		if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
			
			EntityDamageByEntityEvent combatEvent = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
			Entity damager = combatEvent.getDamager();
			
			String property = "KILLED_BY_ENTITY";
			String killerName = damager.getType().toString().toLowerCase();
			
			if (damager instanceof Projectile) 
								
				if (((Projectile) damager).getShooter() instanceof Player)
					if (damager instanceof Arrow)
						property = "KILLED_BY_PLAYER_ARROW";
					else 
						property = "KILLED_BY_PLAYER_PROJECTILE";
				else
					if (damager instanceof Arrow)
						cause = "KILLED_BY_ENTITY_ARROW"; 
					else 
						cause = "KILLED_BY_ENTITY_PROJECTILE"; 
			
			else if (damager instanceof Player)
				property = "KILLED_BY_PLAYER";
			
			if (property.contains("PLAYER"))
				if (property.contains("ARROW") || property.contains("PROJECTILE"))
					killerName = ((Entity) ((Projectile) damager).getShooter()).getName();
				else 
					killerName = damager.getName();
			
			cause = format(Main.casue_dictionary.getProperty(property), player).replace("{killer}", killerName);
		}

		Main.deathCauseCache.put(player.getUniqueId(), cause);
		return cause;
	}

	
	public static String firstLetterCaps(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
	}

	
	public static String format(String string, Player p, int numericValue) {
		
		string = string.replace("{damage}", String.valueOf(numericValue));
		string = string.replace("{blocks}", String.valueOf(numericValue));
		string = string.replace("{countdown}", String.valueOf(numericValue));
		string = string.replace("{player}", p.getName());

		if (numericValue != Integer.MAX_VALUE)
			string = string.replace("{reason}", Utilities.getDeathMessage(p));
		
		
		return ChatColor.translateAlternateColorCodes('&', string);
	}
	
	
	public static String format(String string, Player p) {
		return format(string, p, Integer.MAX_VALUE);
	}
	
	
	@SuppressWarnings("deprecation")
	public static Location getTopBlock(Location loc) {

		List<Integer> unspawnable = Arrays.asList(0, 18, 161);
		Location current = loc;

		if (unspawnable.contains(loc.getBlock().getTypeId())) {

			while (unspawnable.contains(current.getBlock().getTypeId()) && current.getBlockY() > 0) 
				current = current.add(0, -1, 0);
			

			if (loc.add(0, 1, 0).getBlock().getType() == Material.AIR)
				return current;
			
		}

		return loc;
	}

	
	public static boolean insideWorldBorder(Location loc) {
		
		WorldBorder border = loc.getWorld().getWorldBorder();

		double maxX = border.getCenter().getX() + (border.getSize() / 2);
		double minX = border.getCenter().getX() - (border.getSize() / 2);

		double maxZ = border.getCenter().getZ() + (border.getSize() / 2);
		double minZ = border.getCenter().getZ() - (border.getSize() / 2);

		return (loc.getX() < maxX && loc.getX() > minX) &&  (loc.getZ() < maxZ && loc.getZ() > minZ);
	}
	
	
	@SuppressWarnings("deprecation")
	public static boolean isSpawnable(Location loc) {
				
		if (!insideWorldBorder(loc))
			return false;
		
		if (loc.getWorld().getHighestBlockAt(loc).getY() == 0)
			return false;

		Location current = loc, current_tree = loc;

		while (current.getBlock().getType() == Material.AIR && current.getBlock().getY() >= 0)
			current = current.add(0, -1, 0);

		if (current.getBlock().isLiquid())
			return false;

		while (Arrays.asList(18, 161).contains(current_tree.getBlock().getTypeId()) && current_tree.getBlock().getY() >= 0)
			current_tree = current_tree.add(0, -1, 0);

		if (Arrays.asList(17, 162).contains(current_tree.getBlock().getTypeId()))
			return false;

		return true;
	}

	
	public static Location getRandomLocationAround(Location loc) {

		int max = Config.MAX_RADIUS.getIntValue(), min = Config.MIN_RADIUS.getIntValue();
		int Xmax = loc.getBlockX() + max, Xmin = loc.getBlockX() - min;
		int Zmax = loc.getBlockZ() + max, Zmin = loc.getBlockZ() - min;

		int randomX = Xmin + (int) (Math.random() * (Xmax - Xmin + 1));
		int randomZ = Zmin + (int) (Math.random() * (Zmax - Zmin + 1));

		Double x = Double.parseDouble(Integer.toString(randomX));
		Double z = Double.parseDouble(Integer.toString(randomZ));

		return getTopBlock(new Location(loc.getWorld(), x, loc.getWorld().getHighestBlockYAt(x.intValue(), z.intValue()), z));
	}

	
	public static Location getRandomSpawnLocation(Location loc) {
		
		Location selected = getRandomLocationAround(loc);
		int count = 0;

		while ((selected == null || !isSpawnable(selected)) && count < 50) {

			selected = getRandomLocationAround(loc);
			count++;
		}

		if (!isSpawnable(selected))
			return loc.getWorld().getSpawnLocation();

		return selected;
	}

	
	public static void deathSpectate(Player player, Location deathLocation, Location respawnLocation) {
		
		int duration = Config.SPECTATE_RESPAWN_LENGTH.getIntValue();

		deathSpectate(player, deathLocation, respawnLocation, duration);
	}

	public static void deathSpectate(Player player, Location deathLocation, Location respawnLocation, int duration) {
		
		if (Main.toggledDeathSpectate.contains(player.getUniqueId()))
			return;
		
		final Title title = Main.getTitle();
		final GameMode gameMode = player.getGameMode();

		if ((gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR) && !Config.SPECTATE_RESPAWN_FOR_PLAYERS_IN_CREATIVE.getBoolenValue())
			return;
		
		title.sendTitle(player, 14, 30, 30, Config.SPECTATE_RESPAWN_PROGRESS_TITLE.getFormattedValue(player, 0), ChatColor.RED + "" + duration);
		Main.spectatorsGamemode.put(player.getUniqueId(), gameMode);
		
		player.setGameMode(GameMode.SPECTATOR);
		
		new BukkitRunnable() {
			
			int counter = duration;

			@Override
			public void run() {
				
				if (Main.spectatorsCountdown.getOrDefault(player.getUniqueId(), 0) == -1)
					counter = 0;
				
				Main.spectatorsCountdown.put(player.getUniqueId(), counter);
				
				if (!player.isOnline()) {
					this.cancel();
					return;
				}
				
				if (counter > 0) {
					
					if(player.getLocation().getBlockY() < 0 || player.getWorld() != deathLocation.getWorld() || player.getLocation().distance(deathLocation) > Config.SPECTATE_RESPAWN_MAX_FLY_DISTANCE.getIntValue())
						player.teleport(deathLocation);
		
					title.sendTitle(player, 7, 15, 15, Config.SPECTATE_RESPAWN_PROGRESS_TITLE.getFormattedValue(player, 0), Config.SPECTATE_RESPAWN_PROGRESS_TITLE_LINE2.getFormattedValue(player, counter));
					
					counter--;
					
					return;		
				}
				
				player.setGameMode(gameMode);
				player.teleport(respawnLocation);
				
				if (Main.spectatorsGamemode.containsKey(player.getUniqueId()))
					Main.spectatorsGamemode.remove(player.getUniqueId());
				
				if (Main.spectatorsCountdown.containsKey(player.getUniqueId()))
					Main.spectatorsCountdown.remove(player.getUniqueId());

				
				int distance = (int) Math.round(deathLocation.distance(respawnLocation));
				title.sendTitle(player, 7, 15, 15, Config.SPECTATE_RESPAWN_TITLE_LINE_1.getFormattedValue(player, distance), Config.SPECTATE_RESPAWN_TITLE_LINE_2.getFormattedValue(player, distance));

				runCommands(player);
				
				this.cancel();
			}
		}.runTaskTimer(Bukkit.getPluginManager().getPlugin("AdvanceRespawn"), 2, 20);

	}

	public static void runCommands(Player p) {

		if (Config.PLAYER_RUN_COMMAND_ON_RESPAWN.getBoolenValue())
			for (String s : Config.PLAYER_RESPAWN_COMMANDS.getArrayValue())
				p.performCommand(s.replace("{player}", p.getName().toLowerCase()));

		if (Config.CONSOLE_RESPAWN_COMMANDS.getBoolenValue())
			for (String s : Config.CONSOLE_RESPAWN_COMMANDS.getArrayValue())
				Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), s.replace("{player}", p.getName().toLowerCase()));
	}

}

