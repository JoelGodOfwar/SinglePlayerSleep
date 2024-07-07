package com.github.joelgodofwar.sps.api;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.github.joelgodofwar.sps.SinglePlayerSleep;


public class SendJsonMessages {
	public static void SendJsonMessage(Player player, String string){
		/** Some code taken from AutoMessage by ELCHILEN0 */
		String v = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		try {
			// Parse the message
			Object parsedMessage = Class.forName("net.minecraft.server." + v + ".IChatBaseComponent$ChatSerializer").getMethod("a", String.class).invoke(null, ChatColor.translateAlternateColorCodes("&".charAt(0), string));
			Object packetPlayOutChat = Class.forName("net.minecraft.server." + v + ".PacketPlayOutChat").getConstructor(Class.forName("net.minecraft.server." + v + ".IChatBaseComponent")).newInstance(parsedMessage);

			// Drill down to the playerConnection which calls the sendPacket method
			Object craftPlayer = Class.forName("org.bukkit.craftbukkit." + v + ".entity.CraftPlayer").cast(player);
			Object craftHandle = Class.forName("org.bukkit.craftbukkit." + v + ".entity.CraftPlayer").getMethod("getHandle").invoke(craftPlayer);
			Object playerConnection = Class.forName("net.minecraft.server." + v + ".EntityPlayer").getField("playerConnection").get(craftHandle);

			// Send the message packet
			Class.forName("net.minecraft.server." + v + ".PlayerConnection").getMethod("sendPacket", Class.forName("net.minecraft.server." + v + ".Packet")).invoke(playerConnection, packetPlayOutChat);
		} catch (Exception ignore) {
			ignore.printStackTrace();
		}
	}
	public static void SendAllJsonMessage(String s, String oldS, World world){
		String s2;
		for (Player player: Bukkit.getOnlinePlayers()){
			if(player.getWorld().equals(world)){
				if(player.hasPermission("sps.cancel")&&SinglePlayerSleep.displaycancel){
					SendJsonMessage(player, s);
					if(SinglePlayerSleep.debug){SinglePlayerSleep.log("SinglePlayerSleep - SAJM - perm & display");}
				}else{
					s2 = s;
					s2 = s2.replace(oldS, "").replace(" [\"", " \"").replace("]\"", "\"").replace(",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"Click to cancel sleep\"}", "");
					SendJsonMessage(player, s2);
					//PluginBase.logger.info("S=" + s);
					//PluginBase.logger.info("s2=" + s2);
					//PluginBase.logger.info("no perm");
				}
			}
		}
	}
}
