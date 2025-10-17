package com.github.joelgodofwar.sps.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.joelgodofwar.sps.util.Ansi;

public class UpdateChecker {
	private static int project;
	private URL checkURL;
	private String newVersion;
	private String newMinVers;
	private String oldVersion;
	private String oldMinVers;
	@SuppressWarnings("unused")
	private JavaPlugin plugin;
	String[] strVersionNew; // [0]=1.14 [1]=1.0.0.?
	String[] strVersionCurrent; // [0]=1.14 [1]=1.0.0.?
	String URLString = "https://github.com/JoelGodOfwar/SinglePlayerSleep/raw/master/versioncheck/";
	String URLFile = "/version.txt";


	public UpdateChecker(JavaPlugin plugin, int projectID) {
		this.plugin = plugin;
		project = projectID;
		oldVersion = plugin.getDescription().getVersion();
		try {
			checkURL = new URL(URLString + oldVersion.substring(0, 4) + URLFile);
		}catch(MalformedURLException e) {
			Bukkit.getLogger().warning(Ansi.RED + "Could not connect to update server.");
			//Bukkit.getPluginManager().disablePlugin(plugin);
		}
	}
	public UpdateChecker(String plugin, int projectID){
		//this.plugin = plugin;
		project = projectID;
		oldVersion = plugin;
		try {
			checkURL = new URL(URLString + oldVersion.substring(0, 4) + URLFile);
		}catch(MalformedURLException e) {
			Bukkit.getLogger().warning(Ansi.RED + "Could not connect to update server.");
			//Bukkit.getPluginManager().disablePlugin(plugin);
		}
	}
	public static String getResourceUrl() {return "https://spigotmc.org/resources/" + project;}
	public boolean checkForUpdates() throws Exception {
		boolean isOutdated = false;
		URLConnection con = checkURL.openConnection();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
			newVersion = reader.readLine();
		}
		strVersionNew = newVersion.split("_"); // Split into minimum MC version and version

		newMinVers = strVersionNew[0];
		newVersion = strVersionNew[1];
		//System.out.println("newVersion=" + newVersion);
		Version newVers = new Version(newVersion);

		strVersionCurrent = oldVersion.split("_");

		oldMinVers = strVersionCurrent[0];
		oldVersion = strVersionCurrent[1];
		//System.out.println("oldVersion=" + oldVersion);
		Version oldVers = new Version(oldVersion);

		if(oldVers.isDev()){ /** If currently on Dev version, ignore build -1 from Patch */
			if(newVers.Major() > oldVers.Major()){isOutdated = true;}
			if(newVers.Major() == oldVers.Major()){/** same do nothing */}
			if(newVers.Minor() > oldVers.Minor()){isOutdated = true;}
			else if(newVers.Minor() == oldVers.Minor()){/** same do nothing */}
			if(newVers.Patch() > (oldVers.Patch() - 1)){isOutdated = true;}
			else if(newVers.Patch() == (oldVers.Patch() - 1)){/** same do nothing */}
			/**if(newVers.Build() > oldVers.Build()){isOutdated = true;}
        	else if(newVers.Build() == oldVers.Build()){/** same do nothing }*/
		}else {
			if(newVers.Major() > oldVers.Major()){isOutdated = true;}
			if(newVers.Major() == oldVers.Major()){/** same do nothing */}
			if(newVers.Minor() > oldVers.Minor()){isOutdated = true;}
			else if(newVers.Minor() == oldVers.Minor()){/** same do nothing */}
			if(newVers.Patch() > oldVers.Patch()){isOutdated = true;}
			else if(newVers.Patch() == oldVers.Patch()){/** same do nothing */}
			if(newVers.Build() > oldVers.Build()){isOutdated = true;}
			else if(newVers.Build() == oldVers.Build()){/** same do nothing */}
		}
		return isOutdated; //plugin.getDescription().getVersion().equals(newVersion); TODO:
	}
	public String newVersion(){
		return newMinVers + "_" + newVersion;
	}
	public String oldVersion(){
		return oldMinVers + "_" + oldVersion;
	}

	public class Version{
		private int Major; // 1
		private int Minor; // 16
		private int Patch; // 1
		private int Build; // ?
		private boolean isDev = false;
		private String[] string2 = {"0","0","0","0"};
		public Version(String string){
			//System.out.println("string=" + string);
			string2 = string.split("\\.");
			//for (String a : string2)
			//System.out.println(a);
			//System.out.println("string2=" + string2.toString());
			//System.out.println("string2.length=" + string2.length);
			this.Major = NumberUtils.toInt(string2[0]);
			this.Minor = NumberUtils.toInt(string2[1]);
			this.Patch = NumberUtils.toInt(string2[2]);
			if(string2.length >= 4){
				if(string2[3].toUpperCase().contains("D")){
					isDev =  true;
					string2[3] = string2[3].toUpperCase().replace("D", "");
				}
				this.Build = NumberUtils.toInt(string2[3]);
			}else{
				this.Build = 0;
			}
		}
		public int Major(){return Major;}
		public int Minor(){return Minor;}
		public int Patch(){return Patch;}
		public int Build(){return Build;}
		public boolean isDev(){return isDev;}
	}
	/**
	public static boolean UpdateCheck;
	public String UColdVers;
	public String UCnewVers;
	public String thisName = this.getName();
	public String thisVersion = this.getDescription().getVersion();

for onEnable use
	if(UpdateCheck){
			try {
				Bukkit.getConsoleSender().sendMessage("Checking for updates...");
				UpdateChecker updater = new UpdateChecker(this, 68139);
				if(updater.checkForUpdates()) {
					UpdateAvailable = true; // TODO: Update Checker
					UColdVers = updater.oldVersion();
					UCnewVers = updater.newVersion();
					Bukkit.getConsoleSender().sendMessage(this.getName() + Ansi.RED + " v" + UColdVers + Ansi.RESET +" " + lang.get("newvers") + Ansi.GREEN + " v" + UCnewVers + Ansi.RESET);
					Bukkit.getConsoleSender().sendMessage(UpdateChecker.getResourceUrl());
				}else{
					UpdateAvailable = false;
				}
			}catch(Exception e) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not process update check");
				e.printStackTrace();
			}
		}

for update command use
	@NotNull
				BukkitTask updateTask = this.getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {

					public void run() {
						try {
							Bukkit.getConsoleSender().sendMessage("Checking for updates...");
							UpdateChecker updater = new UpdateChecker(thisVersion, 68139);
							if(updater.checkForUpdates()) {
								UpdateAvailable = true;
								UColdVers = updater.oldVersion();
								UCnewVers = updater.newVersion();
								sender.sendMessage(ChatColor.YELLOW + thisName + ChatColor.RED + " v" + UColdVers + ChatColor.RESET + " " + lang.get("newvers") + ChatColor.GREEN + " v" + UCnewVers + ChatColor.RESET);
								sender.sendMessage(UpdateChecker.getResourceUrl());
							}else{
								sender.sendMessage(ChatColor.YELLOW + thisName + ChatColor.RED + " v" + thisVersion + ChatColor.RESET + " Up to date." + ChatColor.RESET);
								UpdateAvailable = false;
							}
						}catch(Exception e) {
							sender.sendMessage(ChatColor.RED + "Could not process update check");
							Bukkit.getConsoleSender().sendMessage(Ansi.RED + "Could not process update check");
							e.printStackTrace();
						}
					}

				});
	 */
}