package com.github.joelgodofwar.sps.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class Utils {
	public static final char COLOR_CHAR = '\u00A7';

	public static void sendJson(CommandSender player, String string){
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw \"" + player.getName() + "\" " + string);
	}
	public static void sendJson(Player player, String string){
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw \"" + player.getName() + "\" " + string);
	}
	public static String of(String string)
	{
		Preconditions.checkArgument( string != null, "string cannot be null" );
		if ( string.startsWith( "#" ) && (string.length() == 7) )
		{
			@SuppressWarnings("unused") int rgb;
			try
			{
				rgb = Integer.parseInt( string.substring( 1 ), 16 );
			} catch ( NumberFormatException ex )
			{
				throw new IllegalArgumentException( "Illegal hex string " + string );
			}

			StringBuilder magic = new StringBuilder( COLOR_CHAR + "x" );
			for ( char c : string.substring( 1 ).toCharArray() )
			{
				magic.append( COLOR_CHAR ).append( c );
			}

			return magic.toString();
		}


		throw new IllegalArgumentException( "Could not parse ChatColor " + string );
	}

	public void getComponent(String string) {
		ComponentBuilder component  = new  ComponentBuilder("Hello ").color(ChatColor.RED);
		component.append(string);
	}
}
