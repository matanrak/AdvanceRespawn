package net.scyllamc.matan.respawn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


public enum Config {

	AUTO_RESPAWN(true),
	USE_RADIUS(true),
	MAX_RADIUS(40),
	MIN_RADIUS(30),

	USE_DEATH_MESSAGE(true),
	DEATH_MESSAGE("&7{player} &c{reason}"),

	DISABLED_WORLDS(new ArrayList<String>()),

	SPECTATE_RESPAWN(false),
	SPECTATE_RESPAWN_FOR_PLAYERS_IN_CREATIVE(false),
	SPECTATE_RESPAWN_LENGTH(4),
	SPECTATE_RESPAWN_PROGRESS_TITLE("&2Respawning...!"),
	SPECTATE_RESPAWN_TITLE_LINE_1("&cRespawned!"),
	SPECTATE_RESPAWN_TITLE_LINE_2("&aYou have respawned &2%blocks% &ablocks away"),

	SHOW_RESPAWN_TITLES(true),
	RESPAWN_TITLE_LINE_1("&cRespawned!"),
	RESPAWN_TITLE_LINE_2("&aYou have respawned &2%blocks% &ablocks away"),

	SHOW_HOLOGRAMS(true),
	HOLOGRAM_DURATION(10),
	HOLOGRAM_LINE_1("&c{player} &7died."),
	HOLOGRAM_LINE_2("&c{reason}"),

	PLAYER_RUN_COMMAND_ON_RESPAWN(false),
	PLAYER_RESPAWN_COMMANDS(new ArrayList<String>()),

	CONSOLE_RUN_COMMAND_ON_RESPAWN(false),
	CONSOLE_RESPAWN_COMMANDS(new ArrayList<String>());
	
	public static Plugin plugin = Main.plugin;
	public static FileConfiguration config = plugin.getConfig();


	@SuppressWarnings({ "resource", "unchecked" })
	public static void updateConfigVersion() {
				
		Bukkit.broadcastMessage("Updating config values");
		
		plugin.reloadConfig();
		
		final Map<String, Object> configValues = new HashMap<String, Object>(plugin.getConfig().getValues(true));
		
		File configFile = new File(plugin.getDataFolder(), "config.yml");
		configFile.delete();
		
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
				
		try {

			BufferedReader reader = new BufferedReader(new FileReader(plugin.getDataFolder() + File.separator + "config.yml"));

			String newConfig = reader.lines().map((line) -> {
				
				Object[] filtered = configValues.entrySet().stream().filter(entry -> line.contains(entry.getKey() + ":")).toArray();
				
				if (line.length() > 0 && line.toCharArray()[0] == '-')
					return "";
							
				if (filtered.length == 0) 
					return line;
	
				Entry<String, Object> savedEntry = (Entry<String, Object>) filtered[0];
		
				String updatedLine = savedEntry.getKey() + ": ";
				
				if (savedEntry.getValue() instanceof ArrayList) 
					return updatedLine + ((ArrayList<String>) savedEntry.getValue()).stream().map(value -> "\n- " + value.toString()).collect(Collectors.joining(""));
				else if (savedEntry.getValue() instanceof String) 
					return updatedLine + "'" + savedEntry.getValue().toString() + "'";
				
                return updatedLine += savedEntry.getValue().toString();
                
            }).collect(Collectors.joining("\n"));
			
			FileWriter writer = new FileWriter(configFile);
			writer.write(newConfig);
			writer.close();
			
			plugin.reloadConfig();      

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private Object value;

	
	Config(Object t) {
		this.value = t;
	}

	
	public String toConfigString() {		
		return Stream.of(this.toString().split("_")).map(entry -> Utilities.firstLetterCaps(entry)).collect(Collectors.joining("_"));
	}
	
	
	public String getStringValue() {
		
		if (config.contains(this.toConfigString())) 
			return config.getString(this.toConfigString());
		
		Bukkit.broadcastMessage("BOOM : " + this.toConfigString());
		updateConfigVersion();
		return value.toString();
	}
	
	
	public int getIntValue() {
		
		if (config.contains(this.toConfigString())) 
			return config.getInt(this.toConfigString());
		
		Bukkit.broadcastMessage("BOOM : " + this.toConfigString());
		updateConfigVersion();
		return (int) value;
	}
	
	
	public boolean getBoolenValue() {
		
		if (config.contains(this.toConfigString())) 
			return config.getBoolean(this.toConfigString());
		
		Bukkit.broadcastMessage("BOOM : " + this.toConfigString());
		updateConfigVersion();
		return (boolean) value;
	}
		
	
	@SuppressWarnings("unchecked")
	
	public ArrayList<String> getArrayValue() {
		
		if (config.contains(this.toConfigString())) 
			return (ArrayList<String>) config.getList(this.toConfigString());
		
		Bukkit.broadcastMessage("BOOM : " + this.toConfigString());
		updateConfigVersion();
		return (ArrayList<String>) value;
	}
	
	
	public String getFormattedValue(Player p, int distance) {
		return Utilities.format(getStringValue(), p, distance);
	}
	
	
}