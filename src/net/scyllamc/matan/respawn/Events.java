package net.scyllamc.matan.respawn;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.md_5.bungee.api.ChatColor;

public class Events implements Listener {

	public static HashMap<UUID, PlayerDeathEvent> deathEvents = new HashMap<UUID, PlayerDeathEvent>();
	public static HashMap<UUID, Location> deathLocations = new HashMap<UUID, Location>();


	@EventHandler
	public void Death(PlayerDeathEvent event) {

		final Player player = event.getEntity();
		
		if (Main.deathCauseCache.containsKey(player.getUniqueId()))
			Main.deathCauseCache.remove(player.getUniqueId());

		Location deathLocation = player.getLocation();
		
		if(deathLocation.getBlockY() <= 0)
			deathLocation = deathLocation.getWorld().getHighestBlockAt(deathLocation).getLocation();
		
		deathLocations.put(player.getUniqueId(), deathLocation);
		deathEvents.put(player.getUniqueId(), event);

		if (Config.AUTO_RESPAWN.getBoolenValue()) 
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
				public void run() {
					player.spigot().respawn();
				}
			}, 1L);
		
		if (Config.USE_DEATH_MESSAGE.getBoolenValue())
			event.setDeathMessage(Config.DEATH_MESSAGE.getFormattedValue(player, 0));	
		
		if (Config.SHOW_HOLOGRAMS.getBoolenValue() && Main.usingHolograms) 
			Main.holo.spawnHolo(player);
	}

	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		
		Player player = event.getPlayer();
		
		Location respawnLocation = player.getWorld().getSpawnLocation();
		Location deathLocation = deathLocations.getOrDefault(player.getUniqueId(), player.getLocation());
		
		if (Config.DISABLED_WORLDS.getArrayValue().contains(deathLocation.getWorld().getName())) 
			return;
		
		if (Config.USE_RADIUS.getBoolenValue()) 
			respawnLocation = Utilities.getRandomSpawnLocation(deathLocation);

		int distance = (int) deathLocation.distance(respawnLocation);

		if (Config.SHOW_RESPAWN_TITLES.getBoolenValue() && Main.title != null) 
			Main.getTitle().sendTitle(player, 7, 15, 15, Config.RESPAWN_TITLE_LINE_1.getFormattedValue(player, distance), Config.RESPAWN_TITLE_LINE_2.getFormattedValue(player, distance));
				
		if (Config.SPECTATE_RESPAWN.getBoolenValue()) {
			event.setRespawnLocation(deathLocation);
			Utilities.deathSpectate(player, deathLocation, respawnLocation);
			return;
		}
		
		Utilities.runCommands(player);
		
		if (Config.USE_RADIUS.getBoolenValue()) 
			event.setRespawnLocation(respawnLocation);
	}

	
	@EventHandler
	public void join(PlayerJoinEvent event) {
		
		Player player = event.getPlayer();
		
		if(Config.SPECTATE_RESPAWN_CONTINUE_TIMER_AFTER_LOGOUT.getBoolenValue() && Main.cachedRespawnLocation.containsKey(player.getUniqueId()) && Main.spectatorsCountdown.containsKey(player.getUniqueId())) {
			
			Location respawnLocation = Main.cachedRespawnLocation.get(player.getUniqueId());
			int time_left = Main.spectatorsCountdown.get(player.getUniqueId());
			
			Utilities.deathSpectate(player, player.getLocation(), respawnLocation, time_left);
		}

	}
	
	@EventHandler
	public void leave(PlayerQuitEvent event) {
		
		Player player = event.getPlayer();
		
		if (Main.spectatorsGamemode.containsKey(player.getUniqueId())) {
			
			player.setGameMode(Main.spectatorsGamemode.get(player.getUniqueId()));
			Location respawn = Utilities.getRandomSpawnLocation(player.getLocation());
			
			Main.cachedRespawnLocation.put(player.getUniqueId(), respawn);
			
			if(Config.SPECTATE_RESPAWN_CONTINUE_TIMER_AFTER_LOGOUT.getBoolenValue())
				return;
			
			player.teleport(respawn);
			
			Main.spectatorsGamemode.remove(player.getUniqueId());
		}
	}
	

	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {

		if (event.getCause().equals(TeleportCause.SPECTATE) && Main.spectatorsGamemode.containsKey(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "Advance Respawn" + ChatColor.GRAY + " | " + ChatColor.RED + "You cannot teleport while respawning!");
		}
	}

}
