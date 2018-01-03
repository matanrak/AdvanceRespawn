package net.scyllamc.matan.respawn.holograms;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.scyllamc.matan.respawn.ConfigHandler;
import net.scyllamc.matan.respawn.Methods;

public class Holo_HolographicDisplays implements Holo {

	public void spawnHolo(Player p, String cause) {

		if (!ConfigHandler.displayHologram) {
			return;
		}

		int ticks = ConfigHandler.hologramTics;

		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwner(p.getName().toString());
		skull.setItemMeta(meta);

		Plugin plugin = Bukkit.getPluginManager().getPlugin("AdvanceRespawn");
		Location loc = p.getLocation().add(0, 2, 0);
		final com.gmail.filoghost.holographicdisplays.api.Hologram hologram = com.gmail.filoghost.holographicdisplays.api.HologramsAPI.createHologram(plugin, loc);
		hologram.appendItemLine(skull);

		hologram.appendTextLine(Methods.buildString(ConfigHandler.hololine1, p));
		hologram.appendTextLine(Methods.buildString(ConfigHandler.hololine2, p));

		new BukkitRunnable() {
			@Override
			public void run() {
				hologram.delete();
			}
		}.runTaskLater(Bukkit.getPluginManager().getPlugin("AdvanceRespawn"), ticks);

		return;

	}

}
