package net.scyllamc.matan.respawn.holograms;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.scyllamc.matan.respawn.Config;

public class Holo_HolographicDisplays implements Holo {

	public void spawnHolo(Player p) {

		if (!Config.SHOW_HOLOGRAMS.getBoolenValue()) 
			return;
		
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwner(p.getName().toString());
		skull.setItemMeta(meta);

		Plugin plugin = Bukkit.getPluginManager().getPlugin("AdvanceRespawn");
		Location loc = p.getLocation().add(0, 2, 0);
		final com.gmail.filoghost.holographicdisplays.api.Hologram hologram = com.gmail.filoghost.holographicdisplays.api.HologramsAPI.createHologram(plugin, loc);
		hologram.appendItemLine(skull);

		hologram.appendTextLine(Config.HOLOGRAM_LINE_1.getFormattedValue(p, 0));
		hologram.appendTextLine(Config.HOLOGRAM_LINE_2.getFormattedValue(p, 0));

		new BukkitRunnable() {
			@Override
			public void run() {
				hologram.delete();
			}
		}.runTaskLater(Bukkit.getPluginManager().getPlugin("AdvanceRespawn"), Config.HOLOGRAM_DURATION.getIntValue() * 20);

		return;

	}

}
