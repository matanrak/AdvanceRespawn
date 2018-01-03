package net.scyllamc.matan.respawn.holograms;

import java.util.UUID;
import net.scyllamc.matan.respawn.ConfigHandler;
import net.scyllamc.matan.respawn.Methods;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Holo_Holograms implements Holo {
	public void spawnHolo(Player p, String cause) {
		if (!ConfigHandler.displayHologram) {
			return;
		}
		int ticks = ConfigHandler.hologramTics;

		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwner(p.getName().toString());
		skull.setItemMeta(meta);

		if ((Bukkit.getPluginManager().isPluginEnabled("Holograms")) && (ConfigHandler.pluginHolograms)) {
			final com.sainttx.holograms.api.Hologram hologram = new com.sainttx.holograms.api.Hologram(UUID.randomUUID().toString(), p.getLocation().add(0.0D, 2D, 0.0D));
			((com.sainttx.holograms.api.HologramPlugin) JavaPlugin.getPlugin(com.sainttx.holograms.api.HologramPlugin.class)).getHologramManager().addActiveHologram(hologram);
			hologram.addLine(new com.sainttx.holograms.api.line.ItemLine(hologram, skull));
			hologram.addLine(new com.sainttx.holograms.api.line.TextLine(hologram, Methods.buildString(ConfigHandler.hololine1, p)));
			hologram.addLine(new com.sainttx.holograms.api.line.TextLine(hologram, Methods.buildString(ConfigHandler.hololine2, p)));
			
			new BukkitRunnable() {
				public void run() {
					hologram.despawn();
				}
			}.runTaskLater(Bukkit.getPluginManager().getPlugin("AdvanceRespawn"), ticks);
			return;
		}
	}
}