package net.scyllamc.matan.respawn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.Plugin;
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

	public static Plugin plugin;
	public static Logger logger = Bukkit.getLogger();
	
	public static boolean usingHolograms = false;
	public static Title title;
	public static Holo holo;

	public static Properties casue_dictionary = new Properties();
	public static File casue_dictionary_file;

	public static HashMap<UUID, GameMode> spectatorsGamemode = new HashMap<UUID, GameMode>();
	public static HashMap<UUID, String> deathCauseCache = new HashMap<UUID, String>();

	
	@Override
	public void onEnable() {
	
		plugin = Bukkit.getPluginManager().getPlugin("AdvanceRespawn");
		casue_dictionary_file = new File(plugin.getDataFolder().toString(), "deathCauses.properties");

		this.saveDefaultConfig();
	
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(new Events(), this);
		
		getCommand("AdvanceRespawn").setExecutor(new Commands());
		
		initializeProperties();
		
		if (!loadTitle()) 
			logger.info("Unsupported version for titles, feature disabled");
		
		new BukkitRunnable() {
			@Override
			public void run() {
				usingHolograms = getHoloHandler();
			}
		}.runTaskLater(Bukkit.getPluginManager().getPlugin("AdvanceRespawn"), 35);

	}

	
	@Override
	public void onDisable() {

		if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) 
			for (com.gmail.filoghost.holographicdisplays.api.Hologram hologram : com.gmail.filoghost.holographicdisplays.api.HologramsAPI.getHolograms(Bukkit.getPluginManager().getPlugin("AdvanceRespawn"))) 
				hologram.delete();
			
		for (Entry<UUID, GameMode> entry : spectatorsGamemode.entrySet()) {
			((HumanEntity) Bukkit.getOfflinePlayer(entry.getKey())).setGameMode(entry.getValue());
			((HumanEntity) Bukkit.getOfflinePlayer(entry.getKey())).teleport(Events.deathLocations.get(entry.getKey()));
		}
		
		getServer().getScheduler().cancelAllTasks();
	}

	
	public static Title getTitle() {
		return title;
	}

	
	public static Holo getHolo() {
		return holo;
	}
	
	
	private static boolean loadTitle() {
		String version;

		try {
			version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		} catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) {
			return false;
		}

		switch (version) {
		
		case "v1_12_R1":
			title = new Title_1_12();
			break;
		case "v1_11_R1":
			title = new Title_1_11();
			break;
		case "v1_10_R1":
			title = new Title_1_10();
			break;
		case "v1_9_R2":
			title = new Title_1_9_4();
			break;
		case "v1_9_R1":
			title = new Title_1_9();
			break;
		case "v1_8_R3":
			title = new Title_1_8();
			break;
		}

		return title != null;
	}

	
	public static boolean getHoloHandler() {

		if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			holo = new Holo_HolographicDisplays();
			logger.info("Using 'HolographicDisplays' as hologram handler");
			return true;
		}

		if (Bukkit.getPluginManager().isPluginEnabled("Holograms")) {
			holo = new Holo_Holograms();
			logger.info("Using 'Holograms' as the hologram handler");
			return true;
		}

		logger.info("No hologram handler found. Feature disabled.");
		return false;
	}


	
	public static void initializeProperties() {
			
		try {

			if (!casue_dictionary_file.exists()) {
				
				casue_dictionary_file.createNewFile();
				
				casue_dictionary.setProperty(DamageCause.BLOCK_EXPLOSION.toString(), "was exploded to bits");
				casue_dictionary.setProperty(DamageCause.DROWNING.toString(), "has drowned");
				casue_dictionary.setProperty(DamageCause.FIRE_TICK.toString(), "was burnt");
				casue_dictionary.setProperty(DamageCause.FALL.toString(), "missed a step");
				casue_dictionary.setProperty(DamageCause.MAGIC.toString(), "was zapped by magic");
				casue_dictionary.setProperty(DamageCause.LAVA.toString(), "took a swim in lava");
				casue_dictionary.setProperty(DamageCause.WITHER.toString(), "fought a wither and lost");
				casue_dictionary.setProperty(DamageCause.LIGHTNING.toString(), "was zapped by lightning");
				casue_dictionary.setProperty(DamageCause.SUICIDE.toString(), "commited suicide");
				casue_dictionary.setProperty(DamageCause.VOID.toString(), "fell into the void");
				
				casue_dictionary.setProperty("UNKNOWN", "Nobody knowss why he died");

				casue_dictionary.setProperty("KILLED_BY_PLAYER", "was slain by {killer}");
				casue_dictionary.setProperty("KILLED_BY_PLAYER_ARROW", "was shot by {killer}");
				casue_dictionary.setProperty("KILLED_BY_PLAYER_PROJECTILE", "was shot by {killer}");

				casue_dictionary.setProperty("KILLED_BY_ENTITY", "was slain by a {killer}");
				casue_dictionary.setProperty("KILLED_BY_ENTITY_ARROW", "was shot by a {killer}");
				casue_dictionary.setProperty("KILLED_BY_ENTITY_PROJECTILE", "was shot by a {killer}");

				FileOutputStream output = new FileOutputStream(casue_dictionary_file);
				casue_dictionary.store(output, null);
				output.close();
			}

			FileInputStream input = new FileInputStream(casue_dictionary_file);
			casue_dictionary.load(input);
			input.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
