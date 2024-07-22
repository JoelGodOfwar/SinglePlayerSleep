package com.github.joelgodofwar.sps.api;

import org.bukkit.ChatColor;

import com.github.joelgodofwar.sps.util.FormatUtil;



public class ChatColorUtils
{
	public static String setNametoRGB(String s){
		String prefix = "#";
		return s.replace("<BLACK>", FormatUtil.parseHexColor(prefix + "000000")).replace("<DARK_BLUE>", FormatUtil.parseHexColor(prefix + "0000AA"))
				.replace("<DARK_GREEN>", FormatUtil.parseHexColor(prefix + "00AA00")).replace("<DARK_AQUA>", FormatUtil.parseHexColor(prefix + "00AAAA"))
				.replace("<DARK_RED>", FormatUtil.parseHexColor(prefix + "AA0000")).replace("<DARK_PURPLE>", FormatUtil.parseHexColor(prefix + "AA00AA"))
				.replace("<GOLD>", FormatUtil.parseHexColor(prefix + "FFAA00")).replace("<GRAY>", FormatUtil.parseHexColor(prefix + "AAAAAA"))
				.replace("<DARK_GRAY>", FormatUtil.parseHexColor(prefix + "555555")).replace("<BLUE>", FormatUtil.parseHexColor(prefix + "5555FF"))
				.replace("<GREEN>", FormatUtil.parseHexColor(prefix + "55FF55")).replace("<AQUA>", FormatUtil.parseHexColor(prefix + "55FFFF"))
				.replace("<RED>", FormatUtil.parseHexColor(prefix + "FF5555")).replace("<LIGHT_PURPLE>", FormatUtil.parseHexColor(prefix + "FF55FF"))
				.replace("<YELLOW>", FormatUtil.parseHexColor(prefix + "FFFF55")).replace("<WHITE>", FormatUtil.parseHexColor(prefix + "FFFFFF"));
	}

	public static String setColorsByCode(String s)
	{
		return s.replace("&0", "" + ChatColor.BLACK).replace("&1", "" + ChatColor.DARK_BLUE)
				.replace("&2", "" + ChatColor.DARK_GREEN).replace("&3", "" + ChatColor.DARK_AQUA)
				.replace("&4", "" + ChatColor.DARK_RED).replace("&5", "" + ChatColor.DARK_PURPLE)
				.replace("&6", "" + ChatColor.GOLD).replace("&7", "" + ChatColor.GRAY)
				.replace("&8", "" + ChatColor.DARK_GRAY).replace("&9", "" + ChatColor.BLUE)
				.replace("&a", "" + ChatColor.GREEN).replace("&b", "" + ChatColor.AQUA)
				.replace("&c", "" + ChatColor.RED).replace("&d", "" + ChatColor.LIGHT_PURPLE)
				.replace("&e", "" + ChatColor.YELLOW).replace("&f", "" + ChatColor.WHITE)
				.replace("&k", "" + ChatColor.MAGIC).replace("&l", "" + ChatColor.BOLD)
				.replace("&m", "" + ChatColor.STRIKETHROUGH).replace("&n", "" + ChatColor.UNDERLINE)
				.replace("&o", "" + ChatColor.ITALIC).replace("&r", "" + ChatColor.RESET)
				.replace("&A", "" + ChatColor.GREEN).replace("&B", "" + ChatColor.AQUA)
				.replace("&C", "" + ChatColor.RED).replace("&D", "" + ChatColor.LIGHT_PURPLE)
				.replace("&E", "" + ChatColor.YELLOW).replace("&F", "" + ChatColor.WHITE)
				.replace("&K", "" + ChatColor.MAGIC).replace("&L", "" + ChatColor.BOLD)
				.replace("&M", "" + ChatColor.STRIKETHROUGH).replace("&N", "" + ChatColor.UNDERLINE)
				.replace("&O", "" + ChatColor.ITALIC).replace("&R", "" + ChatColor.RESET);
	}
	public static String setColorsByName(String s){
		return s.replace("BLACK", "" + ChatColor.BLACK).replace("DARK_BLUE", "" + ChatColor.DARK_BLUE)
				.replace("DARK_GREEN", "" + ChatColor.DARK_GREEN).replace("DARK_AQUA", "" + ChatColor.DARK_AQUA)
				.replace("DARK_RED", "" + ChatColor.DARK_RED).replace("DARK_PURPLE", "" + ChatColor.DARK_PURPLE)
				.replace("GOLD", "" + ChatColor.GOLD).replace("GRAY", "" + ChatColor.GRAY)
				.replace("DARK_GRAY", "" + ChatColor.DARK_GRAY).replace("BLUE", "" + ChatColor.BLUE)
				.replace("GREEN", "" + ChatColor.GREEN).replace("AQUA", "" + ChatColor.AQUA)
				.replace("RED", "" + ChatColor.RED).replace("LIGHT_PURPLE", "" + ChatColor.LIGHT_PURPLE)
				.replace("YELLOW", "" + ChatColor.YELLOW).replace("WHITE", "" + ChatColor.WHITE)
				.replace("MAGIC", "" + ChatColor.MAGIC).replace("BOLD", "" + ChatColor.BOLD)
				.replace("STRIKETHROUGH", "" + ChatColor.STRIKETHROUGH).replace("UNDERLINE", "" + ChatColor.UNDERLINE)
				.replace("ITALIC", "" + ChatColor.ITALIC).replace("RESET", "" + ChatColor.RESET);
	}
	public static String setColors(String s)
	{
		return s.replace("&0", "" + ChatColor.BLACK).replace("&1", "" + ChatColor.DARK_BLUE)
				.replace("&2", "" + ChatColor.DARK_GREEN).replace("&3", "" + ChatColor.DARK_AQUA)
				.replace("&4", "" + ChatColor.DARK_RED).replace("&5", "" + ChatColor.DARK_PURPLE)
				.replace("&6", "" + ChatColor.GOLD).replace("&7", "" + ChatColor.GRAY)
				.replace("&8", "" + ChatColor.DARK_GRAY).replace("&9", "" + ChatColor.BLUE)
				.replace("&a", "" + ChatColor.GREEN).replace("&b", "" + ChatColor.AQUA)
				.replace("&c", "" + ChatColor.RED).replace("&d", "" + ChatColor.LIGHT_PURPLE)
				.replace("&e", "" + ChatColor.YELLOW).replace("&f", "" + ChatColor.WHITE)
				.replace("&k", "" + ChatColor.MAGIC).replace("&l", "" + ChatColor.BOLD)
				.replace("&m", "" + ChatColor.STRIKETHROUGH).replace("&n", "" + ChatColor.UNDERLINE)
				.replace("&o", "" + ChatColor.ITALIC).replace("&r", "" + ChatColor.RESET)
				.replace("&A", "" + ChatColor.GREEN).replace("&B", "" + ChatColor.AQUA)
				.replace("&C", "" + ChatColor.RED).replace("&D", "" + ChatColor.LIGHT_PURPLE)
				.replace("&E", "" + ChatColor.YELLOW).replace("&F", "" + ChatColor.WHITE)
				.replace("&K", "" + ChatColor.MAGIC).replace("&L", "" + ChatColor.BOLD)
				.replace("&M", "" + ChatColor.STRIKETHROUGH).replace("&N", "" + ChatColor.UNDERLINE)
				.replace("&O", "" + ChatColor.ITALIC).replace("&R", "" + ChatColor.RESET)
				.replace("<BLACK>", "" + ChatColor.BLACK).replace("<DARK_BLUE>", "" + ChatColor.DARK_BLUE)
				.replace("<DARK_GREEN>", "" + ChatColor.DARK_GREEN).replace("<DARK_AQUA>", "" + ChatColor.DARK_AQUA)
				.replace("<DARK_RED>", "" + ChatColor.DARK_RED).replace("<DARK_PURPLE>", "" + ChatColor.DARK_PURPLE)
				.replace("<GOLD>", "" + ChatColor.GOLD).replace("<GRAY>", "" + ChatColor.GRAY)
				.replace("<DARK_GRAY>", "" + ChatColor.DARK_GRAY).replace("<BLUE>", "" + ChatColor.BLUE)
				.replace("<GREEN>", "" + ChatColor.GREEN).replace("<AQUA>", "" + ChatColor.AQUA)
				.replace("<RED>", "" + ChatColor.RED).replace("<LIGHT_PURPLE>", "" + ChatColor.LIGHT_PURPLE)
				.replace("<YELLOW>", "" + ChatColor.YELLOW).replace("<WHITE>", "" + ChatColor.WHITE)
				.replace("<MAGIC>", "" + ChatColor.MAGIC).replace("<BOLD>", "" + ChatColor.BOLD)
				.replace("<STRIKETHROUGH>", "" + ChatColor.STRIKETHROUGH).replace("<UNDERLINE>", "" + ChatColor.UNDERLINE)
				.replace("<ITALIC>", "" + ChatColor.ITALIC).replace("<RESET>", "" + ChatColor.RESET);
	}
}