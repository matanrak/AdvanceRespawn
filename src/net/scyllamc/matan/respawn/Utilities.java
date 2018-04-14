package net.scyllamc.matan.respawn;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

import net.scyllamc.matan.respawn.titles.Title;

public class Methods {

	public static String getDeathMessage(Player p) {

		if (Events.deathEvents.containsKey(p)) {
			try {
				PlayerDeathEvent e = Events.deathEvents.get(p);

				if (e == null || e.getEntity() == null || e.getEntity().getLastDamageCause() == null) {
					return "Died";
				}

				DamageCause dc = e.getEntity().getLastDamageCause().getCause();
				String cause = "died!";

				if (e.getEntity() instanceof Player) {
					Player player = e.getEntity();

					if (ConfigHandler.casue_dictionary.containsKey(dc.toString())) {
						return ConfigHandler.casue_dictionary.getProperty(dc.toString());
					}

					if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
						EntityDamageByEntityEvent ne = (EntityDamageByEntityEvent) e.getEntity().getLastDamageCause();
						Entity damager = ne.getDamager();

						if (!damager.getType().equals(EntityType.PLAYER) || !damager.getType().equals(EntityType.ARROW)) {
							cause = ConfigHandler.casue_dictionary.getProperty("KILLED_BY_ENTITY");
							cause = cause.replace("{killer}", ne.getDamager().getType().toString().toLowerCase());
							return cause;
						}

						String killername = " an unknown killer";

						if (damager.getType().equals(EntityType.PLAYER)) {
							killername = firstLetterCaps(ne.getDamager().getType().toString().toLowerCase());
							if (damager.getName() != null) {
								killername = damager.getName();
							}
							if (damager.getCustomName() != null) {
								killername = damager.getCustomName();
							}
						}

						if (damager.getType().equals(EntityType.ARROW) || damager.getType().equals(EntityType.SPLASH_POTION)) {
							Projectile proj = (Projectile) damager;
							Entity attacker = (Entity) proj.getShooter();
							if (attacker.getName() != null) {
								killername = attacker.getName();
							}
						} else {
							killername = firstLetterCaps(ne.getDamager().getType().toString().toLowerCase());
						}

						cause = ConfigHandler.casue_dictionary.getProperty("KILLED_BY_PLAYER");
						cause = cause.replace("{killer}", killername);
						return cause;
					}

				}

				return cause;

			} catch (NullPointerException exception) {
				exception.printStackTrace();
				return "Died";
			}
		}
		
		return "died";
	}

	public static String firstLetterCaps(String data) {
		String firstLetter = data.substring(0, 1).toUpperCase();
		String restLetters = data.substring(1).toLowerCase();
		return firstLetter + restLetters;
	}

	public static Location getRandomLocation(Location player, int Xminimum, int Xmaximum, int Zminimum, int Zmaximum) {
		World world = player.getWorld();
		int randomX = 0;
		int randomZ = 0;
		double x = 0.0D;
		double z = 0.0D;
		randomX = Xminimum + (int) (Math.random() * (Xmaximum - Xminimum + 1));
		randomZ = Zminimum + (int) (Math.random() * (Zmaximum - Zminimum + 1));
		x = Double.parseDouble(Integer.toString(randomX));
		z = Double.parseDouble(Integer.toString(randomZ));
		x = x + 0.5;
		z = z + 0.5;
		Location n = new Location(world, x, world.getHighestBlockYAt(new Location(world, x, player.getY(), z)), z);
		return n;
	}

	public static HashMap<UUID, GameMode> specs = new HashMap<UUID, GameMode>();

	public static void deathSpectate(final Player p, final Location l) {
		final GameMode gm = p.getGameMode();
		final Location fl = p.getLocation();
		specs.put(p.getUniqueId(), gm);
		p.setGameMode(GameMode.SPECTATOR);
		int c = ConfigHandler.spectateTicks - 1;
		final Title t = Main.getTitle();

		t.sendTitle(p, 14, 30, 30, ConfigHandler.spectateprogresstitle, ChatColor.RED + "" + c);
		p.teleport(fl);

		new BukkitRunnable() {
			@Override
			public void run() {

				p.teleport(l);

			}
		}.runTaskLater(Bukkit.getPluginManager().getPlugin("AdvanceRespawn"), 2);

		new BukkitRunnable() {
			int count = ConfigHandler.spectateTicks;

			@Override
			public void run() {

				if (count > 0) {
					t.sendTitle(p, 7, 15, 15, ConfigHandler.spectateprogresstitle, ChatColor.RED + "" + count);
				}

				if (count <= 0) {
					p.setGameMode(gm);
					int max = ConfigHandler.max;
					int min = ConfigHandler.min;
					Location respawn = Methods.getRandomLocation(l, l.getBlockX() - min, l.getBlockX() + max, l.getBlockZ() - min, l.getBlockZ() + max);
					String line2 = spectateTitleLine2(p, Math.round(respawn.distance(fl)));
					t.sendTitle(p, 7, 15, 15, ConfigHandler.spectatetitleLine1, line2);
					p.teleport(respawn);
					this.cancel();
					runCommands(p);
					specs.remove(p.getUniqueId());
					return;
				}

				count--;
			}
		}.runTaskTimer(Bukkit.getPluginManager().getPlugin("AdvanceRespawn"), 20, 20);

	}

	public static String buildString(String s, Player p) {
		String msg = s;

		char ch = '&';
		msg = ChatColor.translateAlternateColorCodes(ch, s);

		msg = msg.replace("{player}", p.getName());
		msg = msg.replace("{reason}", getDeathMessage(p));

		return msg;
	}

	public static String colorString(String s) {
		char ch = '&';
		return ChatColor.translateAlternateColorCodes(ch, s);
	}

	public static void runCommands(Player p) {
		if (ConfigHandler.player_respawncommand) {
			for (String s : ConfigHandler.player_respawncommands) {

				p.performCommand(buildString(s,p));
			}
		}

		if (ConfigHandler.console_respawncommand) {
			for (String s : ConfigHandler.console_respawncommands) {
				String c = s.replace("{player}", p.getName().toLowerCase());
				Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), c);
			}
		}
	}

	public static String titleLine2(Player p, int distance) {
		String line2 = buildString(ConfigHandler.titleLine2, p);
		try {
			if (line2.contains("{blocks}")) {
				line2 = line2.replace("{blocks}", distance + "");
			}
			line2 = Methods.colorString(line2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return line2;
	}

	public static String spectateTitleLine2(Player p, long l) {
		String line2 = buildString(ConfigHandler.spectatetitleLine2, p);
		try {
			if (line2.contains("{blocks}")) {
				line2 = line2.replace("{blocks}", l + "");
			}
			line2 = Methods.colorString(line2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return line2;
	}
}
