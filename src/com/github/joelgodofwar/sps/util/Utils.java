package com.github.joelgodofwar.sps.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Utils {
	
	public static void sendJson(CommandSender player, String string){
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw \"" + player.getName() + "\" " + string);
	}
	public static void sendJson(Player player, String string){
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw \"" + player.getName() + "\" " + string);
	}
}
