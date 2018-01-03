package net.scyllamc.matan.respawn;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
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

	public static HashMap<Player, PlayerDeathEvent> deathEvents = new HashMap<Player, PlayerDeathEvent>();
	public HashMap<OfflinePlayer, String> deathCauses = new HashMap<OfflinePlayer, String>();
	public static HashMap<Player, Location> deathLocation = new HashMap<Player, Location>();

	@EventHandler
	public void Death(PlayerDeathEvent e) {
		deathLocation.put(e.getEntity(), e.getEntity().getLocation());
		final Player player = e.getEntity();
		deathEvents.put(player, e);

		String cause = Methods.getDeathMessage(player);
		deathCauses.put(player, cause);

		if (ConfigHandler.autoRespawn) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("AdvanceRespawn"), new Runnable() {
				public void run() {
					player.spigot().respawn();
				}
			}, 1L);
		}

		if (ConfigHandler.useDeathMessage) {
			e.setDeathMessage(Methods.buildString(ConfigHandler.deathMessage, player));
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		
		Player p = e.getPlayer();
		Location l = p.getWorld().getSpawnLocation();
		Location respawn = l;
		
		if (ConfigHandler.useRadius) {

			if (deathLocation.containsKey(p)) {
				l = deathLocation.get(p);
			}
			int max = ConfigHandler.max;
			int min = ConfigHandler.min;
			respawn = Methods.getRandomLocation(l, l.getBlockX() - min, l.getBlockX() + max, l.getBlockZ() - min, l.getBlockZ() + max);

			e.setRespawnLocation(respawn);
		}

		if (ConfigHandler.disabledWorlds.contains(p.getLocation().getWorld().getName())) {
			return;
		}

		if (ConfigHandler.displayHologram) {
			String cause = "";
			if (deathCauses.containsKey(p)) {
				cause = deathCauses.get(p);
			}

			if (Main.usingHolograms) {
				Main.holo.spawnHolo(p, cause);
			}

		}

		if (ConfigHandler.spectateRespawn && deathLocation.containsKey(p) && deathLocation.get(p).getBlockY() > 0) {
			Methods.deathSpectate(p, l);
			e.setRespawnLocation(deathLocation.get(p));
			return;
		}

		Methods.runCommands(p);

		if (ConfigHandler.displayTitles) {
			int i = (int) l.distance(respawn);
			if (Main.title != null) {
				Main.getTitle().sendTitle(p, 7, 15, 15, ConfigHandler.titleLine1, Methods.titleLine2(p, i) + "");
			}
		}

	}

	@EventHandler
	public void leave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if (Methods.specs.containsKey(p.getUniqueId())) {
			p.setGameMode(Methods.specs.get(p.getUniqueId()));
			Location l = p.getLocation();
			int max = ConfigHandler.max;
			int min = ConfigHandler.min;
			Location respawn = Methods.getRandomLocation(l, l.getBlockX() - min, l.getBlockX() + max, l.getBlockZ() - min, l.getBlockZ() + max);
			p.teleport(respawn);
			Methods.specs.remove(p.getUniqueId());
		}
	}

	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {

		if (event.getCause().equals(TeleportCause.SPECTATE) && Methods.specs.containsKey(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "Advance Respawn" + ChatColor.GRAY + " | " + ChatColor.RED + "You cannot teleport while respawning!");

		}
	}

}
