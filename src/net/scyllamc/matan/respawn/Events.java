package net.scyllamc.matan.respawn;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
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
			event.setDeathMessage(Config.DEATH_MESSAGE.getFormattedValue(player, 0).replace("{reason}", Utilities.getDeathMessage(player)));
	}

	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		
		Player player = event.getPlayer();
		
		Location respawnLocation = player.getWorld().getSpawnLocation();
		Location deathLocation = player.getLocation();
		
		if (deathLocations.containsKey(player.getUniqueId()))
			deathLocation = deathLocations.get(player.getUniqueId());
		

		if (Config.DISABLED_WORLDS.getArrayValue().contains(deathLocation.getWorld().getName())) 
			return;
		
		if (Config.USE_RADIUS.getBoolenValue()) 
			respawnLocation = Utilities.getRandomSpawnLocation(deathLocation);

		int distance = (int) deathLocation.distance(respawnLocation);
	
		if (Config.SHOW_HOLOGRAMS.getBoolenValue() && Main.usingHolograms) 
			Main.holo.spawnHolo(player);

		if (Config.SHOW_RESPAWN_TITLES.getBoolenValue() && Main.title != null) 
			Main.getTitle().sendTitle(player, 7, 15, 15, Config.RESPAWN_TITLE_LINE_1.getFormattedValue(player, distance), Config.RESPAWN_TITLE_LINE_2.getFormattedValue(player, distance));
				
		if (Config.SPECTATE_RESPAWN.getBoolenValue()) {
			event.setRespawnLocation(deathLocation);
			Utilities.deathSpectate(player, deathLocation, respawnLocation);
			return;
		}
		
		Utilities.runCommands(player);
		event.setRespawnLocation(respawnLocation);
	}

	
	@EventHandler
	public void leave(PlayerQuitEvent e) {
		
		Player p = e.getPlayer();
		
		if (Main.spectatorsGamemode.containsKey(p.getUniqueId())) {
			
			p.setGameMode(Main.spectatorsGamemode.get(p.getUniqueId()));
			Location l = p.getLocation();
		
			Location respawn = Utilities.getRandomSpawnLocation(l);
			p.teleport(respawn);
			
			Main.spectatorsGamemode.remove(p.getUniqueId());
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
