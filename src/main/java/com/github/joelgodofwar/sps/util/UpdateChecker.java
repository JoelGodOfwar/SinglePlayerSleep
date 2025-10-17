package com.github.joelgodofwar.sps.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class UpdateChecker {
	private static int project;
	private URL checkURL;
	private String newVersion;
	private String async_newVersion;
	private String newMinVers;
	private String oldVersion;
	private String oldMinVers;
	@SuppressWarnings("unused")
	private JavaPlugin plugin;
	String[] strVersionNew; // [0]=1.14 [1]=1.0.0.?
	String[] strVersionCurrent; // [0]=1.14 [1]=1.0.0.?


	public UpdateChecker(JavaPlugin plugin, int projectID, String verURL) {
		this.plugin = plugin;
		project = projectID;
		async_newVersion = plugin.getDescription().getVersion();
		try {
			checkURL = new URL(verURL.replace("{vers}", async_newVersion.substring(0, 4)));
		}catch(MalformedURLException e) {
			Bukkit.getLogger().warning(Ansi.RED + "Could not connect to update server.");
			//Bukkit.getPluginManager().disablePlugin(plugin);
		}
	}
	public UpdateChecker(String plugin, int projectID, String verURL) {
		async_newVersion = plugin;
		try {
			checkURL = new URL(verURL.replace("{vers}", async_newVersion.substring(0, 4)));
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

		strVersionCurrent = async_newVersion.split("_");

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
}