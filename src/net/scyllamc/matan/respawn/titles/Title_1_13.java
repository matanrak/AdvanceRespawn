package net.scyllamc.matan.respawn.titles;



import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_13_R1.IChatBaseComponent;
import net.minecraft.server.v1_13_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_13_R1.PlayerConnection;

public class Title_1_13 implements Title{

	 
	@Override
	  public void sendTitle(Player player, int fadeIn, int stay, int fadeOut, String title, String subtitle){
		  
		PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;	    
	    PacketPlayOutTitle packetPlayOutTimes = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, fadeIn, stay, fadeOut);
	    connection.sendPacket(packetPlayOutTimes);
	
	      subtitle = subtitle.replaceAll("%player%", player.getDisplayName());
	      subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
	      IChatBaseComponent titleSub = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + subtitle + "\"}");
	      PacketPlayOutTitle packetPlayOutSubTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, titleSub);
	      connection.sendPacket(packetPlayOutSubTitle);
	
	      title = title.replaceAll("%player%", player.getDisplayName());
	      title = ChatColor.translateAlternateColorCodes('&', title);
	      IChatBaseComponent titleMain = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + title + "\"}");
	      PacketPlayOutTitle packetPlayOutTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, titleMain);
	      connection.sendPacket(packetPlayOutTitle);
	    
	  }
	  
}