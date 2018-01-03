package net.scyllamc.matan.respawn;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.scyllamc.matan.respawn.holograms.Holo;
import net.scyllamc.matan.respawn.holograms.Holo_Holograms;
import net.scyllamc.matan.respawn.holograms.Holo_HolographicDisplays;
import net.scyllamc.matan.respawn.titles.Title;
import net.scyllamc.matan.respawn.titles.Title_1_10;
import net.scyllamc.matan.respawn.titles.Title_1_11;
import net.scyllamc.matan.respawn.titles.Title_1_12;
import net.scyllamc.matan.respawn.titles.Title_1_8;
import net.scyllamc.matan.respawn.titles.Title_1_9;
import net.scyllamc.matan.respawn.titles.Title_1_9_4;

public class Main extends JavaPlugin implements Listener {

	public static Title title;
	public static Holo holo;
	public static Logger logger = Bukkit.getLogger();
	public static boolean usingHolograms = false;

	@Override
	public void onEnable() {
		this.saveDefaultConfig();

		new BukkitRunnable() {
			@Override
			public void run() {
				ConfigHandler.loadSettingsFromConfig();
			}
		}.runTaskLater(Bukkit.getPluginManager().getPlugin("AdvanceRespawn"), 25);

		new BukkitRunnable() {
			@Override
			public void run() {

				usingHolograms = getHoloHandler();

			}
		}.runTaskLater(Bukkit.getPluginManager().getPlugin("AdvanceRespawn"), 35);

		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(new Events(), this);
		getCommand("AdvanceRespawn").setExecutor(new PublicCommands());

		if (!loadTitle()) {
			logger.info("Unsupported version for titles, feature disabled");
		}

		new Metrics(this);
	}

	@Override
	public void onDisable() {

		if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			for (com.gmail.filoghost.holographicdisplays.api.Hologram hologram : com.gmail.filoghost.holographicdisplays.api.HologramsAPI
					.getHolograms(Bukkit.getPluginManager().getPlugin("AdvanceRespawn"))) {
				hologram.delete();
			}
		}

		getServer().getScheduler().cancelAllTasks();

	}

	private boolean loadTitle() {
		String version;

		try {
			version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

		} catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) {
			return false;
		}

		getLogger().info("Your server is running version " + version);

		if (version.equals("v1_12_R1")) {
			title = new Title_1_12();

		}else if (version.equals("v1_11_R1")) {
			title = new Title_1_11();

		} else if (version.equals("v1_10_R1")) {
			title = new Title_1_10();

		} else if (version.equals("v1_9_R2")) {
			title = new Title_1_9_4();

		} else if (version.equals("v1_9_R1")) {
			title = new Title_1_9();

		} else if (version.equals("v1_8_R3")) {
			title = new Title_1_8();
		}

		return title != null;
	}

	public boolean getHoloHandler() {
		if (ConfigHandler.pluginHolograhpicDisplays) {
			holo = new Holo_HolographicDisplays();
			getLogger().info("Using 'HolographicDisplays' as hologram handler");
			return true;
		}

		if (ConfigHandler.pluginHolograms) {
			holo = new Holo_Holograms();
			getLogger().info("Using 'Holograms' as the hologram handler");
			return true;
		}

		getLogger().info("No hologram handler found. Feature disabled.");
		return false;
	}

	public static Title getTitle() {
		return title;
	}

	public static Holo getHolo() {
		return holo;
	}

}
