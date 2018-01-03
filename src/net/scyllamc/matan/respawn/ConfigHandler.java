package net.scyllamc.matan.respawn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class ConfigHandler {

	public static final File casue_dictionary_file = new File("plugins/AdvanceRespawn/causes.properties");
	public static final Properties casue_dictionary = new Properties();

	public static boolean autoRespawn = true;
	public static boolean useRadius = true;
	public static int max = 40;
	public static int min = 30;

	public static boolean displayHologram = true;
	public static boolean pluginHolograhpicDisplays = false;
	public static boolean pluginHolograms = false;
	public static int hologramTics = 100;
	public static String hololine1 = "&2Respawned!";
	public static String hololine2 = "&aYou have respawned &2{blocks} &ablocks away";

	public static boolean displayTitles = true;
	public static String titleLine1 = "&2Respawned!";
	public static String titleLine2 = "&aYou have respawned &2{blocks} &ablocks away";

	public static boolean spectateRespawn = false;
	public static int spectateTicks = 6;
	public static String spectateprogresstitle = "&2Respawning...";
	public static String spectatetitleLine1 = "&2Respawned!";
	public static String spectatetitleLine2 = "&aYou have respawned &2{blocks} &ablocks away";

	public static boolean player_respawncommand = false;
	public static List<String> player_respawncommands = new ArrayList<String>();

	public static boolean console_respawncommand = false;
	public static List<String> console_respawncommands = new ArrayList<String>();

	public static List<String> disabledWorlds = new ArrayList<String>();

	public static boolean useDeathMessage = true;
	public static String deathMessage = "&c{player} &7Died.";
	
	public static void loadSettingsFromConfig() {

		FileConfiguration conf = Bukkit.getPluginManager().getPlugin("AdvanceRespawn").getConfig();
		Bukkit.getPluginManager().getPlugin("AdvanceRespawn").reloadConfig();

		if (conf.contains("Use_Death_Message")) {
			useDeathMessage = conf.getBoolean("Use_Death_Message");
		}

		if (conf.contains("Death_Message")) {
			deathMessage = conf.getString("Death_Message");
			deathMessage = Methods.colorString(deathMessage);
		}

		if (conf.contains("Max_Radius")) {
			max = conf.getInt("Max_Radius");
		}
		if (conf.contains("Min_Radius")) {
			min = conf.getInt("Min_Radius");
		}
		if (conf.contains("Auto_Respawn")) {
			autoRespawn = conf.getBoolean("Auto_Respawn");
		}
		if (conf.contains("Use_Radius")) {
			useRadius = conf.getBoolean("Use_Radius");
		}

		if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			pluginHolograhpicDisplays = true;
		}
		if (Bukkit.getPluginManager().isPluginEnabled("Holograms")) {
			pluginHolograms = true;
		}
		if (conf.contains("Enable_Holograms")) {
			displayHologram = conf.getBoolean("Enable_Holograms");
		}
		if (conf.contains("Hologram_Line1")) {
			hololine1 = conf.getString("Hologram_Line1");
		}
		if (conf.contains("Hologram_Line2")) {
			hololine2 = conf.getString("Hologram_Line2");
		}
		if (conf.contains("Remove_Holograms_After_Seconds")) {
			hologramTics = conf.getInt("Remove_Holograms_After_Seconds") * 20;
		}

		if (conf.contains("Respawn_Titles")) {
			displayTitles = conf.getBoolean("Respawn_Titles");
		}
		if (conf.contains("Respawn_Title_Line1")) {
			titleLine1 = conf.getString("Respawn_Title_Line1");
			titleLine1 = Methods.colorString(titleLine1);
		}
		if (conf.contains("Respawn_Title_Line2")) {
			titleLine2 = conf.getString("Respawn_Title_Line2");
			titleLine2 = Methods.colorString(titleLine2);
		}

		if (conf.contains("Spectate_Respawn")) {
			spectateRespawn = conf.getBoolean("Spectate_Respawn");
		}
		if (conf.contains("Spectate_Respawn_Delay")) {
			spectateTicks = conf.getInt("Spectate_Respawn_Delay");
		}
		if (conf.contains("Spectate_Respawn_Progress_Title")) {
			spectateprogresstitle = conf.getString("Spectate_Respawn_Progress_Title");
			spectateprogresstitle = Methods.colorString(spectateprogresstitle);
		}
		if (conf.contains("Spectate_Respawn_Title1")) {
			spectatetitleLine1 = conf.getString("Spectate_Respawn_Title1");
			spectatetitleLine1 = Methods.colorString(spectatetitleLine1);
		}
		if (conf.contains("Spectate_Respawn_Title2")) {
			spectatetitleLine2 = conf.getString("Spectate_Respawn_Title2");
			spectatetitleLine2 = Methods.colorString(spectatetitleLine2);
		}

		disabledWorlds = new ArrayList<String>();
		if (conf.getList("Disabled_Worlds") != null) {
			for (String w : conf.getStringList("Disabled_Worlds")) {
				disabledWorlds.add(w);
			}
		}

		if (conf.contains("Player_Run_Command_On_Respawn")) {
			player_respawncommand = conf.getBoolean("Player_Run_Command_On_Respawn");
		}
		player_respawncommands = new ArrayList<String>();
		if (conf.getList("Player_Respawn_Commands") != null) {
			for (String w : conf.getStringList("Player_Respawn_Commands")) {
				player_respawncommands.add(w);
			}
		}

		if (conf.contains("Console_Run_Command_On_Respawn")) {
			console_respawncommand = conf.getBoolean("Console_Run_Command_On_Respawn");
		}
		console_respawncommands = new ArrayList<String>();
		if (conf.getList("Console_Respawn_Commands") != null) {
			for (String w : conf.getStringList("Console_Respawn_Commands")) {
				console_respawncommands.add(w);
			}
		}

		initializeProperties();
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
				casue_dictionary.setProperty("KILLED_BY_PLAYER", "was slain by {killer}");
				casue_dictionary.setProperty("KILLED_BY_ENTITY", "was slain by a {killer}");
				
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
