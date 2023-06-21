package com.github.joelgodofwar.sps;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.spectralmemories.bloodmoon.BloodmoonActuator;

import com.earth2me.essentials.Essentials;
import com.github.joelgodofwar.sps.api.ChatColorUtils;
import com.github.joelgodofwar.sps.api.Metrics;
import com.github.joelgodofwar.sps.api.StrUtils;
import com.github.joelgodofwar.sps.api.YmlConfiguration;
import com.github.joelgodofwar.sps.i18n.Translator;
import com.github.joelgodofwar.sps.util.Ansi;
import com.github.joelgodofwar.sps.util.Format;
import com.github.joelgodofwar.sps.util.UpdateChecker;
import com.github.joelgodofwar.sps.util.Utils;
import com.github.joelgodofwar.sps.util.VersionChecker;

import dev.majek.hexnicks.Nicks;
import dev.majek.relocations.net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import me.mrgeneralq.bloodmoon.api.BloodmoonAPI;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
/**
 * @author JoelGodOfWar(JoelYahwehOfWar)
 * some code added by ColdCode(coldcode69)
 */

@SuppressWarnings("unused")
public class SinglePlayerSleep extends JavaPlugin implements Listener{
	/** Languages: čeština (cs_CZ), Deutsch (de_DE), English (en_US), Español (es_ES), Español (es_MX), Français (fr_FR), Italiano (it_IT), Magyar (hu_HU), 日本語 (ja_JP), 한국어 (ko_KR), Lolcat (lol_US), Melayu (my_MY), Nederlands (nl_NL), Polski (pl_PL), Português (pt_BR), Русский (ru_RU), Svenska (sv_SV), Türkçe (tr_TR), 中文(简体) (zh_CN), 中文(繁體) (zh_TW) */
	public final static Logger logger = Logger.getLogger("Minecraft");
	static String THIS_NAME;
	static String THIS_VERSION;
	/** update checker variables */
	public int projectID = 68139; // https://spigotmc.org/resources/71236
	public String githubURL = "https://github.com/JoelGodOfwar/SinglePlayerSleep/raw/master/versioncheck/1.13/versions.xml";
	boolean UpdateAvailable =  false;
	public String UColdVers;
	public String UCnewVers;
	public static boolean UpdateCheck;
    public String DownloadLink = "https://www.spigotmc.org/resources/singleplayersleep.68139";
	/** end update checker variables */
	
	public static boolean cancelbroadcast;
	public static boolean debug;
	public static String daLang;
	//private boolean UpdateAviable = false;

	public boolean isCanceled = false;
	public boolean isDSCanceled = false;
	public int transitionTask = 0;
	public int dayskipTask = 0;
	public int transitionTaskUnrestricted = 1;
	public long pTime = 0;
	public Map<String, Long> playersCancelled = new HashMap<String, Long>();
	private URL url;
	private static long mobSpawningStartTime = 12541;//12600;
	//mobs stop spawning at: 22813
	//mobs start to burn at: 23600
	private static long mobSpawningStopTime = 23600;
	File langFile;
	FileConfiguration lang;
	Translator lang2;
	public static boolean displaycancel;
	public HashMap<UUID, Long> sleeplimit =  new HashMap<UUID, Long>();
	public HashMap<UUID, Long> cancellimit =  new HashMap<UUID, Long>();
	YmlConfiguration config = new YmlConfiguration();
	YamlConfiguration oldconfig = new YamlConfiguration();
	public boolean isBloodMoon = false;
	public String jsonColorString = "\"},{\"text\":\"<text>\",\"color\":\"<color>\"},{\"text\":\"";
	public boolean is116 = false;
	String blacklist_sleep;
	String blacklist_dayskip;
	boolean colorful_console;
	String configVersion = "1.0.7";
	String pluginName = THIS_NAME;
	private Set<String> triggeredPlayers = new HashSet<>();
	
	@Override // TODO: onEnable
	public void onEnable(){
		long startTime = System.currentTimeMillis();
		UpdateCheck = getConfig().getBoolean("auto_update_check", true);
		debug = getConfig().getBoolean("debug", false);
		daLang = getConfig().getString("lang", "en_US");
		displaycancel = getConfig().getBoolean("display_cancel", true);
		//log("displaycancel=" + displaycancel);
		config = new YmlConfiguration();
		oldconfig = new YamlConfiguration();
		blacklist_sleep = config.getString("blacklist.sleep", "");
		blacklist_dayskip = config.getString("blacklist.dayskip", "");
		colorful_console = getConfig().getBoolean("colorful_console", true);
		THIS_NAME = this.getDescription().getName();
		THIS_VERSION = this.getDescription().getVersion();
		if(!getConfig().getBoolean("console.longpluginname", true)) {
			pluginName = "SPS";
		}else {
			pluginName = THIS_NAME;
		}
		lang2 = new Translator(daLang, getDataFolder().toString());
		
		
		
		SinglePlayerSleep.logger.info(Ansi.GREEN +  "**************************************" + Ansi.RESET);
		SinglePlayerSleep.logger.info(Ansi.YELLOW + THIS_NAME + " v" + THIS_VERSION + Ansi.RESET + " Loading...");
		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			logDebug("Jar file contains -DEV, debug set to true");
			//log("jarfile contains dev, debug set to true.");
		}
		if(debug){logDebug("datafolder=" + getDataFolder());}
		/**langFile = new File(getDataFolder() + "" + File.separatorChar + "lang" + File.separatorChar, daLang + ".yml");//\
		if(debug){logDebug("langFilePath=" + langFile.getPath());}
		if(!langFile.exists()){									// checks if the yaml does not exist
			langFile.getParentFile().mkdirs();					// creates the /plugins/<pluginName>/ directory if not found
			saveResource("lang" + File.separatorChar + "cs_CZ.yml", true);
			saveResource("lang" + File.separatorChar + "de_DE.yml", true);
			saveResource("lang" + File.separatorChar + "en_US.yml", true);
			saveResource("lang" + File.separatorChar + "es_MX.yml", true);
			saveResource("lang" + File.separatorChar + "fr_FR.yml", true);
			saveResource("lang" + File.separatorChar + "ja_JP.yml", true);
			saveResource("lang" + File.separatorChar + "lol_US.yml", true);
			saveResource("lang" + File.separatorChar + "nl_NL.yml", true);
			saveResource("lang" + File.separatorChar + "pl_PL.yml", true);
			saveResource("lang" + File.separatorChar + "pt_BR.yml", true);
			saveResource("lang" + File.separatorChar + "sv_SV.yml", true);
			saveResource("lang" + File.separatorChar + "tr_TR.yml", true);
			saveResource("lang" + File.separatorChar + "zh_CN.yml", true);
			saveResource("lang" + File.separatorChar + "zh_TW.yml", true);
			log("lang file not found! copied cs_CZ.yml, de_DE.yml, en_US.yml, es_MX.yml, fr_FR.yml, ja_JP, lol_US.yml, nl_NL.yml, pl_PL.yml, pt_BR.yml, sv_SV.yml,tr_TR.yml, zh_TW.yml, and zh_CN.yml to " + getDataFolder() + "" + File.separatorChar + "lang");
			//ConfigAPI.copy(getResource("lang.yml"), langFile); // copies the yaml from your jar to the folder /plugin/<pluginName>
		}
		lang = new YamlConfiguration();
		try {
			lang.load(langFile);
		} catch (IOException | InvalidConfigurationException e1) {
			e1.printStackTrace();
		}
		String checklangversion = getString("langversion");
		if(checklangversion != null&&checklangversion.contains("2.13.48")){
			//Up to date do nothing
		}else{
			// outdated, update them then
			if(debug){logDebug("checklangversion='" + checklangversion + "'");}
			saveResource("lang" + File.separatorChar + "cs_CZ.yml", true);
			saveResource("lang" + File.separatorChar + "de_DE.yml", true);
			saveResource("lang" + File.separatorChar + "en_US.yml", true);
			saveResource("lang" + File.separatorChar + "es_MX.yml", true);
			saveResource("lang" + File.separatorChar + "fr_FR.yml", true);
			saveResource("lang" + File.separatorChar + "ja_JP.yml", true);
			saveResource("lang" + File.separatorChar + "lol_US.yml", true);
			saveResource("lang" + File.separatorChar + "nl_NL.yml", true);
			saveResource("lang" + File.separatorChar + "pl_PL.yml", true);
			saveResource("lang" + File.separatorChar + "pt_BR.yml", true);
			saveResource("lang" + File.separatorChar + "sv_SV.yml", true);
			saveResource("lang" + File.separatorChar + "tr_TR.yml", true);
			saveResource("lang" + File.separatorChar + "zh_CN.yml", true);
			saveResource("lang" + File.separatorChar + "zh_TW.yml", true);
			log("Updating lang files! copied cs_CZ.yml, de_DE.yml, en_US.yml, es_MX.yml, fr_FR.yml, ja_JP, lol_US.yml, nl_NL.yml, pl_PL.yml, pt_BR.yml, sv_SV.yml,tr_TR.yml, zh_TW.yml, and zh_CN.yml to " + getDataFolder() + "" + File.separatorChar + "lang");
		}
		File oldlangFile = new File(getDataFolder() + "" + File.separatorChar + "lang.yml");
		if(oldlangFile.exists()){
			oldlangFile.delete();
			log("Old lang.yml file deleted.");
		}//*/
		
		/** update checker variable */
		//newVerMsg = ChatColor.YELLOW + thisName + ChatColor.RED + " v{oVer}"  + ChatColor.RESET + " " + get("newvers") + ChatColor.GREEN + " v{nVer}" + ChatColor.RESET;
		/** end update checker variable */
		
		/**  Check for config */
		try{
			if(!getDataFolder().exists()){
				log("Data Folder doesn't exist");
				log("Creating Data Folder");
				getDataFolder().mkdirs();
				log("Data Folder Created at " + getDataFolder());
			}
			File  file = new File(getDataFolder(), "config.yml");
			log("" + file);
			if(!file.exists()){
				log("config.yml not found, creating!");
				saveResource("config.yml", true);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		/** end config check */
		/** Check if config.yml is up to date.*/
		boolean needConfigUpdate = false;
		String oldConfig = new File(getDataFolder(), "config.yml").getPath().toString();
		try {
			oldconfig.load(new File(getDataFolder() + "" + File.separatorChar + "config.yml"));
		} catch (Exception e2) {
			logWarn("Could not load config.yml");
			e2.printStackTrace();
		}
		String checkconfigversion = oldconfig.getString("version", "1.0.0");
		if(checkconfigversion != null){
			if(!checkconfigversion.equalsIgnoreCase(configVersion)){
				needConfigUpdate = true;
			}
		}
		if(needConfigUpdate){
			try {
				copyFile_Java7(getDataFolder() + "" + File.separatorChar + "config.yml",getDataFolder() + "" + File.separatorChar + "old_config.yml");
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				oldconfig.load(new File(getDataFolder(), "config.yml"));
			} catch (IOException | InvalidConfigurationException e2) {
				logWarn("Could not load config.yml");
				e2.printStackTrace();
			}
			saveResource("config.yml", true);
			try {
				config.load(new File(getDataFolder(), "config.yml"));
			} catch (IOException | InvalidConfigurationException e1) {
				logWarn("Could not load config.yml");
				e1.printStackTrace();
			}
			try {
				oldconfig.load(new File(getDataFolder(), "old_config.yml"));
			} catch (IOException | InvalidConfigurationException e1) {
				e1.printStackTrace();
			}
			config.set("auto_update_check", oldconfig.get("auto_update_check", true));
			config.set("debug", oldconfig.get("debug", false));
			config.set("lang", oldconfig.get("lang", "en_US"));
			config.set("blacklist.sleep", oldconfig.get("blacklist.sleep", "world_nether, world_the_end"));
			config.set("blacklist.dayskip", oldconfig.get("blacklist.dayskip", "world_nether, world_the_end"));
			config.set("broadcast_per_world", oldconfig.get("broadcast_per_world", true));
			config.set("reset_insomnia", oldconfig.get("reset_insomnia", false));
			config.set("colorful_console", oldconfig.get("colorful_console", true));
			config.set("clearrain_enabled", oldconfig.get("clearrain_enabled", false));
			config.set("unrestrictedsleep", oldconfig.get("unrestrictedsleep", false));
			config.set("waketime", oldconfig.get("waketime", "NORMAL"));
			config.set("sleepdelay", oldconfig.get("sleepdelay", 10));
			config.set("enabledayskipper", oldconfig.get("enabledayskipper", false));
			config.set("dayskipdelay", oldconfig.get("dayskipdelay", 10));
			config.set("unrestricteddayskipper", oldconfig.get("unrestricteddayskipper", false));
			config.set("dayskipperitemrequired", oldconfig.get("dayskipperitemrequired", true));
			config.set("cancelcolor", oldconfig.get("cancelcolor", "RED"));
			config.set("cancelbracketcolor", oldconfig.get("cancelbracketcolor", "YELLOW"));
			config.set("sleepmsgcolor", oldconfig.get("sleepmsgcolor", "STRIKETHROUGHYELLOW"));
			config.set("playernamecolor", oldconfig.get("playernamecolor", "WHITE"));
			config.set("exitbedcancel", oldconfig.get("exitbedcancel", false));
			config.set("display_cancel", oldconfig.get("display_cancel", true));
			config.set("cancelbroadcast", oldconfig.get("cancelbroadcast", true));
			config.set("sleeplimit", oldconfig.get("sleeplimit", 60));
			config.set("cancellimit", oldconfig.get("cancellimit", 60));
			config.set("notifymustbenight", oldconfig.get("notifymustbenight", false));
			config.set("nickname.usedisplayname", oldconfig.get("nickname.usedisplayname", true));
			config.set("randomsleepmsgs", oldconfig.get("randomsleepmsgs", true));
			config.set("numberofsleepmsgs", oldconfig.get("numberofsleepmsgs", 4));
			for (int i = 1; i < (getConfig().getInt("numberofsleepmsgs") + 1); i++) {
				config.set("sleepmsg" + i, oldconfig.get("sleepmsg" + i, "<player> is sleeping"));
			}
			try {
				config.save(new File(getDataFolder(), "config.yml"));
			} catch (IOException e) {
				logWarn("Could not save old settings to config.yml");
				e.printStackTrace();
			}
			log("config.yml has been updated");
		}else{
			//log("" + "not found");
		}
		/** End Config update check */
		// End config.yml check.

		/** Update Checker */
		if(UpdateCheck){
			try {
				Bukkit.getConsoleSender().sendMessage("Checking for updates...");
				VersionChecker updater = new VersionChecker(this, projectID, githubURL);
				if(updater.checkForUpdates()) {
					/** Update available */
					UpdateAvailable = true; // TODO: Update Checker
					UColdVers = updater.oldVersion();
					UCnewVers = updater.newVersion();
					
					logWarn("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					logWarn("* " + get("sps.version.message").toString().replace("<MyPlugin>", THIS_NAME) );
					logWarn("* " + get("sps.version.old_vers") + ChatColor.RED + UColdVers );
					logWarn("* " + get("sps.version.new_vers") + ChatColor.GREEN + UCnewVers );
					logWarn("*");
					logWarn("* " + get("sps.version.please_update") );
					logWarn("*");
					logWarn("* " + get("sps.version.download") + ": " + DownloadLink + "/history");
					logWarn("* " + get("sps.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
					logWarn("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
				}else{
					/** Up to date */
					log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					log("* " + get("sps.version.curvers"));
					log("* " + get("sps.version.donate") + ": https://ko-fi.com/joelgodofwar");
					log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					UpdateAvailable = false;
				}
			}catch(Exception e) {
				/** Error */
				logWarn( get("sps.version.update.error"));
				e.printStackTrace();
			}
		}else {
			/** auto_update_check is false so nag. */
			logWarn("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
			logWarn( "* " + get("sps.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
			logWarn("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
		}
		/** end update checker */
		
		File  file = new File(getDataFolder(), "permissions.yml");
		log("" + file);
		if(!file.exists()){
			log("permissions.yml not found, creating! This is a sample only!");
			saveResource("permissions.yml", true);
		}
		getServer().getPluginManager().registerEvents(this, this);
		
		log("This server is running " + Bukkit.getName() + " version " + Bukkit.getVersion() + " (Implementing API version " + Bukkit.getBukkitVersion() + ")");
		log("vardebug=" + debug + " debug=" + getConfig().get("debug","error") + " in " + this.getDataFolder() + "/config.yml");
		log("jarfilename=" + this.getFile().getAbsoluteFile());
		
		if(getConfig().getBoolean("debug")==true&&!(jarfile.toString().contains("-DEV"))){
			logDebug("Config.yml dump");
			logDebug("auto_update_check=" + getConfig().getBoolean("auto_update_check"));
			logDebug("debug=" + getConfig().getBoolean("debug"));
			logDebug("lang=" + getConfig().getString("lang"));
			logDebug("unrestrictedsleep=" + getConfig().getBoolean("unrestrictedsleep"));
			logDebug("waketime=" + getConfig().getString("waketime"));
			logDebug("sleepdelay=" + getConfig().getString("sleepdelay"));
			logDebug("enabledayskipper=" + getConfig().getString("enabledayskipper"));
			logDebug("dayskipdelay=" + getConfig().getString("dayskipdelay"));
			logDebug("unrestricteddayskipper=" + getConfig().getBoolean("unrestricteddayskipper"));
			logDebug("dayskipperitemrequired=" + getConfig().getBoolean("dayskipperitemrequired"));
			logDebug("cancelcolor=" + getConfig().getString("cancelcolor"));
			logDebug("sleepmsgcolor=" + getConfig().getString("sleepmsgcolor"));
			logDebug("playernamecolor=" + getConfig().getString("playernamecolor"));
			logDebug("exitbedcancel=" + getConfig().getBoolean("exitbedcancel"));
			logDebug("display_cancel=" + getConfig().getBoolean("display_cancel"));
			logDebug("cancelbroadcast=" + getConfig().getBoolean("cancelbroadcast"));
			logDebug("sleeplimit=" + getConfig().getInt("sleeplimit"));
			logDebug("cancellimit=" + getConfig().getInt("cancellimit"));
			logDebug("notifymustbenight=" + getConfig().getInt("notifymustbenight"));
			logDebug("randomsleepmsgs=" + getConfig().getBoolean("randomsleepmsgs"));
			logDebug("numberofsleepmsgs=" + getConfig().getString("numberofsleepmsgs"));
		}
		String[] serverversion;
		serverversion = getVersion().split("\\.");
		if(debug){logDebug("getVersion = " + getVersion());}
		if(debug){logDebug("serverversion = " + serverversion.length);}
		for (int i = 0; i < serverversion.length; i++)
			if(debug){logDebug(serverversion[i] + " i=" + i);}
		if (!(Integer.parseInt(serverversion[1]) >= 16)){
			is116 = false;
		}else{
			is116 = true;
		}
		if(debug){logDebug(Ansi.BOLD + "" + Ansi.RED + "is116=" + is116 + Ansi.RESET);}
		consoleInfo("ENABLED - Loading took " + LoadTime(startTime));
		try {
			//PluginBase plugin = this;
			Metrics metrics  = new Metrics(this, 5934);
			// New chart here
			// myPlugins()
			metrics.addCustomChart(new Metrics.AdvancedPie("my_other_plugins", new Callable<Map<String, Integer>>() {
				@Override
				public Map<String, Integer> call() throws Exception {
					Map<String, Integer> valueMap = new HashMap<>();
					
					if(getServer().getPluginManager().getPlugin("DragonDropElytra") != null){valueMap.put("DragonDropElytra", 1);}
		    		if(getServer().getPluginManager().getPlugin("NoEndermanGrief") != null){valueMap.put("NoEndermanGrief", 1);}
		    		if(getServer().getPluginManager().getPlugin("PortalHelper") != null){valueMap.put("PortalHelper", 1);}
		    		if(getServer().getPluginManager().getPlugin("ShulkerRespawner") != null){valueMap.put("ShulkerRespawner", 1);}
		    		if(getServer().getPluginManager().getPlugin("MoreMobHeads") != null){valueMap.put("MoreMobHeads", 1);}
		    		if(getServer().getPluginManager().getPlugin("SilenceMobs") != null){valueMap.put("SilenceMobs", 1);}
		    		//if(getServer().getPluginManager().getPlugin("SinglePlayerSleep") != null){valueMap.put("SinglePlayerSleep", 1);}
					if(getServer().getPluginManager().getPlugin("VillagerWorkstationHighlights") != null){valueMap.put("VillagerWorkstationHighlights", 1);}
					if(getServer().getPluginManager().getPlugin("RotationalWrench") != null){valueMap.put("RotationalWrench", 1);}
					return valueMap;
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("auto_update_check", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("auto_update_check").toUpperCase();
				}
			}));
			// add to site
			metrics.addCustomChart(new Metrics.SimplePie("unrestrictedsleep", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("unrestrictedsleep").toUpperCase();
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("var_waketime", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("waketime").toUpperCase();
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("var_sleepdelay", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getInt("sleepdelay");
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("cancelbroadcast", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("cancelbroadcast").toUpperCase();
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("var_debug", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("debug").toUpperCase();
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("var_lang", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("lang").toUpperCase();
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("numberofsleepmsgs", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getInt("numberofsleepmsgs");
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("dayskipdelay", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getInt("dayskipdelay");
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("unrestricteddayskipper", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getBoolean("unrestricteddayskipper");
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("enabledayskipper", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getBoolean("enabledayskipper");
				}
			}));
		}catch (Exception e){
			// Failed to submit the stats
		}
		
	}
	
	@Override // TODO: onDisable
	public void onDisable() {
		consoleInfo("DISABLED");
	}
	
	public void consoleInfo(String state) {
		
		SinglePlayerSleep.logger.info(Ansi.GREEN + "**************************************" + Ansi.RESET);
		SinglePlayerSleep.logger.info(Ansi.YELLOW + THIS_NAME + " v" + THIS_VERSION + Ansi.RESET + " is " + state);
		SinglePlayerSleep.logger.info(Ansi.GREEN + "**************************************" + Ansi.RESET);
	}
	
	public String nameColor() {
		//Only change name colours if one is set
		if (!getConfig().getString("namecolor").contains("NONE")) {
			String nameColor = ChatColorUtils.setColors(getConfig().getString("namecolor"));
			return nameColor;
		} else {
			return "";
		}
	}
	
	
	/**
	 * @param event
	 * @throws InterruptedException
	 */
	@EventHandler
	public void PlayerIsSleeping(PlayerBedEnterEvent event) throws InterruptedException{
		if(debug){logDebug(ChatColor.RED + "** Start PlayerBedEnterEvent **");}
		List<World> worlds = Bukkit.getWorlds();
		//boolean debug = getConfig().getBoolean("debug");
		final Player player = event.getPlayer();
		if(debug){logDebug("PIS player set. ...");}
		final World world = player.getWorld();
		if(debug){logDebug(" PIS world set. ...");}
		int sleepdelay = getConfig().getInt("sleepdelay", 10);
		int dayskipdelay = getConfig().getInt("dayskipdelay", 10);
		event.getBedEnterResult();
		/** Debug info */
		if(debug){logDebug(ChatColor.RED + "**************************************************");}
		if(debug){logDebug(ChatColor.RED + "DEBUG LOG SHOULD CONTAIN THIS");}
		if(debug){logDebug("PIS 12786-23031 = Night, worldTime=" + world.getFullTime());}
		if(debug){logDebug("PIS isNight=" + IsNight(player.getWorld()) + " , isDay=" + IsDay(player.getWorld()));}
		if(debug){logDebug("PIS isOP=" + player.isOp() + ", is116=" + is116);}
		if(debug){logDebug("PIS sps.Hermits=" + player.hasPermission("sps.hermits"));}
		if(debug){logDebug("PIS sps.unrestricted=" + player.hasPermission("sps.unrestricted"));}
		if(debug){logDebug("PIS sps.op=" + player.hasPermission("sps.op"));}
		if(debug){logDebug("PIS unrestrictedsleep=" + getConfig().getBoolean("unrestrictedsleep"));}
		if(debug){logDebug("PIS BedEnterResult=" + event.getBedEnterResult().toString());}
		if(debug){logDebug("PIS isRaining=" + event.getPlayer().getWorld().hasStorm());}
		if(debug){logDebug("PIS isThunderstorm=" + event.getPlayer().getWorld().isThundering());}
		if(debug){logDebug("PIS Permission List:");}
		if(debug){logDebug("PIS sps.hermits=" + player.hasPermission("sps.hermits"));}
		if(debug){logDebug("PIS sps.cancel=" + player.hasPermission("sps.cancel"));}
		if(debug){logDebug("PIS sps.unrestricted=" + player.hasPermission("sps.unrestricted"));}
		if(debug){logDebug("PIS sps.downfall=" + player.hasPermission("sps.downfall"));}
		if(debug){logDebug("PIS sps.thunder=" + player.hasPermission("sps.thunder"));}
		if(debug){logDebug("PIS sps.command=" + player.hasPermission("sps.command"));}
		if(debug){logDebug("PIS sps.update=" + player.hasPermission("sps.update"));}
		if(debug){logDebug("PIS sps.op=" + player.hasPermission("sps.op"));}
		if(debug){logDebug("PIS sps.showUpdateAvailable=" + player.hasPermission("sps.showUpdateAvailable"));}
		if(debug){logDebug("PIS sps.dayskipper=" + player.hasPermission("sps.dayskipper"));}
		if(debug){logDebug("PIS sps.dayskipcommand=" + player.hasPermission("sps.dayskipcommand"));}
		if(debug){logDebug(ChatColor.RED + "**************************************************");}

		if(getServer().getPluginManager().getPlugin("EssentialsX") != null||getServer().getPluginManager().getPlugin("Essentials") != null){
			if(debug){logDebug("perm essentials.sleepingignored=" + player.hasPermission("essentials.sleepingignored"));}
			if(player.hasPermission("essentials.sleepingignored") && !player.isOp()){
				player.sendMessage(ChatColor.RED + "WARNING! " + ChatColor.YELLOW + " you have the permission (" + ChatColor.GOLD + 
						"essentials.sleepingignored" + ChatColor.YELLOW + 
						") which is conflicting with SinglePlaySleep. Please ask for it to be removed. " + ChatColor.RED + "WARNING! ");
				logWarn("Player " + player.getName() + "has the permission " + "essentials.sleepingignored" + " which is known to conflict with SinglePlayerSleep.");
				return;
			}
		}
		if(getConfig().getBoolean("enabledayskipper", false)){ // TODO: Dayskip
			/* Check if it's Day for DaySkipper */
			if(IsDay(player.getWorld())){
				if(debug){logDebug(" DS it is Day");}
				/* OK it's day check if it's a Black bed. */
				if(!player.hasPermission("sps.op")){ // TODO: Dayskip blacklist Check
					if(blacklist_dayskip != null&&!blacklist_dayskip.isEmpty()){
						if(StrUtils.stringContains(blacklist_dayskip, world.getName().toString())){
							log("EDE - World - On blacklist.");
							return;
						}
					}
				}
				/* OK it is a Black bed, now check if they have the DaySkipper item. */
				/*String daMainHand = player.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
				log("daMainHand=" + daMainHand);
				String daOffHand = player.getInventory().getItemInOffHand().getItemMeta().getDisplayName();
				log("daOffHand=" + daOffHand);*/
				ItemStack[] inv = player.getInventory().getContents();
				if(debug){logDebug("passed itemstack");}
				boolean itmDaySkipper = false;
				if(debug){logDebug("itemdayskipper initilized");}
				for(ItemStack item:inv){
					
					if(!(item == null)){
						if(debug){logDebug("item=" + item.getType().name());}
						if(item.getItemMeta().getDisplayName().equalsIgnoreCase("DaySkipper")){
						 		itmDaySkipper = true;
						 		if(debug){logDebug("found the item");}
						 		break;
						 	}
					}
				}
				if(debug){logDebug("inventory iterator finished.");}
				if(!getConfig().getBoolean("dayskipperitemrequired", true)){itmDaySkipper = true;}
				if(itmDaySkipper){ //daMainHand.contentEquals("DaySkipper")||daOffHand.contentEquals("DaySkipper")||
					if(debug){logDebug(" DS item DaySkipper is in inventory.");}
					
					Block block = event.getBed();
					if (((Bed)block.getBlockData()).getMaterial().equals(Material.BLACK_BED)){
							if(debug){logDebug(" DS the bed is Black");}
						/* OK they have the DaySkipper item, now check for the permission*/
						if(player.hasPermission("sps.dayskipper")||player.hasPermission("sps.op")||player.hasPermission("sps.*")){
							if(debug){logDebug(" DS Has perm or is op. ...");}
							String CancelBracketColor = ChatColorUtils.setColorsByName(getConfig().getString("cancelbracketcolor", "YELLOW"));
							/* OK they have the perm, now lets notify the server and schedule the runnable */
							String canmsg = CancelBracketColor + "[\"},{\"text\":\"dacancel\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}},{\"text\":\"" + CancelBracketColor + "]\"}";
							String damsg = "[\"\",{\"text\":\"sleepmsg " + canmsg + "]";
							String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
								if(debug){logDebug(" DS ... msgcolor=" + msgcolor);}
							String sleepmsg = "" + get("sps.message.dayskipmsg","<player> wants to sleep the day away...");
							if(is116){
								sleepmsg = ChatColorUtils.setNametoRGB(sleepmsg);
								sleepmsg = StrUtils.parseRGBNameColors(sleepmsg);
							}else{
								sleepmsg = StrUtils.stripRGBColors(sleepmsg);// strip RGBHEX 
								sleepmsg = ChatColorUtils.setColors(sleepmsg);// SetColorsByName
							}
							
							damsg = damsg.replace("sleepmsg", sleepmsg).replace("\"]\"", "\"" + msgcolor + "]\"");
							//damsg = ChatColorUtils.setColors(damsg);
							//damsg = StrUtils.parseRGBNameColors(damsg);
							/** nickname parser */
							String nickName = getNickname(player);
							String playercolor = "";
							if(!nickName.contains("§")){
								//logWarn("nickName ! contain SS");
								playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
							}else{
								nickName = StrUtils.parseRGBNameColors(nickName);
							}
							/** end nickname parser */
								if(debug){logDebug(" DS ... playercolor=" + playercolor);}
							damsg = damsg.replace("<player>", /**playercolor +*/ nickName /**+ msgcolor*/);
							String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
								if(debug){logDebug(" DS ... cancelcolor=" + cancelcolor);}
							damsg = damsg.replace("dacancel", cancelcolor + get("dayskipcancel") + msgcolor);
							//change cancel color based on config
							damsg = damsg.replace("tooltip", "" + get("dayskipclickcancel"));
								if(debug){logDebug(" DS string processed. ...");}
								if(debug){logDebug(" DS damsg=" + damsg);}
								
							if(getConfig().getBoolean("broadcast_per_world", true)){
								sendJson(player.getWorld(), damsg, canmsg);
							}else{
								sendJson(damsg, canmsg);
							}
								if(debug){logDebug(" DS SendAllJsonMessage. ...");}
							//player.sendMessage("The item in your main hand is named: " + daName);
								if(!isDSCanceled){
									if(debug){logDebug(" DS !isDSCanceled. ...");}
									dayskipTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					
										public void run() {
											setDStime(player, world);
											if(debug){logDebug(" DS setDStime has run. ...");}
										}
										
									}, dayskipdelay * 20);
									
								}else{
									
									isDSCanceled = false;
								}
							return;
						}else{
							player.sendMessage(ChatColor.YELLOW + "" + get("sps.message.noperm"));
							//player.sendTitle("", ChatColor.YELLOW + "" + get("sps.message.noperm"), 0, 0, 5);
						}
					}else{
						player.sendMessage(ChatColor.YELLOW + "" + get("sps.message.dayskipblackbed"));/* NOT A BLACK BED */
					}
				}
			}else{
				if(debug){logDebug("PIS isDay=false");}
			}
		}else{
			if(debug){logDebug("PIS enabledayskipper=false");}
		}
		//if(debug){logDebug("PIS getBedEnterResult=" + event.getBedEnterResult().toString());}
		if(!isBloodmoonInprogress(player.getWorld())){//isBloodmoonInprogress//isBloodMoon
			if(event.getBedEnterResult() == BedEnterResult.OK){
				//Check it's night or if storm
				if (IsNight(player.getWorld())||player.getWorld().isThundering()) {
					if(!player.hasPermission("sps.op")){ // TODO: Sleep Blacklist Check
						if(blacklist_sleep != null&&!blacklist_sleep.isEmpty()){
							if(StrUtils.stringContains(blacklist_sleep, world.getName().toString())){
								log("EDE - World - On blacklist.");
								return;
							}
						}
					}
					//Set default timer for when the player has never slept before
					long timer = 0;
					if(debug){logDebug("PIS IN... " + player.getName() + " is sleeping.");}
					long time = System.currentTimeMillis() / 1000;
					if(sleeplimit.get(player.getUniqueId()) == null){
						if(debug){logDebug("PIS sleeplimit UUID=null");}
						// Check if player has sps.unrestricted
						if (!player.hasPermission("sps.unrestricted")) {
							// Set player's time in HashMap
							sleeplimit.put(player.getUniqueId(), time);
							if(debug){logDebug("PIS IN... " + player.getDisplayName() + " added to playersSlept");}
						}
					}else{
						if(debug){logDebug("PIS sleeplimit UUID !null");}
						// Player is on the list.
						timer = sleeplimit.get(player.getUniqueId());
						if(debug){logDebug("time=" + time);}
						if(debug){logDebug("timer=" + timer);}
						if(debug){logDebug("time - timer=" +  (time - timer));}
						if(debug){logDebug("sleeplimit=" + getConfig().getLong("sleeplimit", 60));}
						// if !time - timer > limit
						if(!((time - timer) > getConfig().getLong("sleeplimit", 60))){
							long length = getConfig().getLong("sleeplimit", 60) - (time - timer) ;
							String sleeplimit = "" + get("sps.message.sleeplimit").toString().replace("<length>", "" + length);
							player.sendMessage(ChatColor.YELLOW + sleeplimit);
							if(debug){logDebug("PIS IN... sleeplimit: " + sleeplimit);}
							//player.sendMessage("You can not do that for " + length + " seconds");
							event.setCancelled(true);
							return;
						}else if((time - timer) > getConfig().getLong("sleeplimit", 60)){
							if(debug){logDebug("time - timer > sleeplimit");}
							sleeplimit.replace(player.getUniqueId(), time);
						}
					}
					
					/** /check if player has already tried sleeping to prevent spam
					if (getConfig().getInt("sleeplimit") > 0) {
						timer = time - pTime;
					}
					
						//Tell the player why they can't sleep
						if (timer < getConfig().getInt("sleeplimit")) {				
							String sleeplimit = "" + get("sps.message.sleeplimit").toString();
							player.sendMessage(ChatColor.YELLOW + sleeplimit);
							log("PIS IN... sleeplimit: " + sleeplimit);
						} else {
							
							//Save the time the player last tried to sleep, skip if player has unrestricted sleep since it will always be successful
							if (!player.hasPermission("sps.unrestricted")) {
								pTime = (int) time;
							}//  */
							
							//Check if players can sleep without the ability for others to cancel it
							if (getConfig().getBoolean("unrestrictedsleep")) {
								if(debug){logDebug("PIS unrestrictedsleep=true");}
								String dastring = "" + get("sps.message.issleep");
								dastring = dastring.replace("<player>", getNickname(player));
								this.broadcast(dastring, world);
								transitionTaskUnrestricted = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
									public void run() {
										setDatime(player, world);
										resetPlayersRestStat(world);
									}						
								}, sleepdelay * 20);
							} else {
								
								//Don't show cancel option if player has unrestricted sleep perm
								if (player.hasPermission("sps.unrestricted")) { //TODO: Unrestricted Broadcast, use random msgs, and colorization
									
									if(debug){logDebug(" PIS Has unrestricted perm. ...");}
									
									//Broadcast to Server
									String dastring = "" + get("sps.message.issleep");
										
									dastring = dastring.replace("<player>", "");
									String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
									if(debug){logDebug(" PIS ... msgcolor=" + msgcolor);}
									String CancelBracketColor = ChatColorUtils.setColorsByName(getConfig().getString("cancelbracketcolor", "YELLOW"));
									if(debug){logDebug(" PIS ... CancelBracketColor=" + CancelBracketColor);}
									//String damsg = "[\"\",{\"text\":\"player\"},{\"text\":\" is sleeping [\"},{\"text\":\"dacancel\",\"color\":\"red\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"tooltip\"}]}}},{\"text\":\"]\",\"color\":\"none\",\"bold\":false}]";
									//String damsg = "[\"\",{\"text\":\"sleepmsg [\"},{\"text\":\"dacancel\",\"color\":\"red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}},{\"text\":\"]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}}]";
									String canmsg = "";//CancelBracketColor + "[\"},{\"text\":\"dacancel\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}},{\"text\":\"" + CancelBracketColor + "]\"";
									String damsg = "[\"\",{\"text\":\"sleepmsg " + canmsg + "\"}]";
									String sleepmsg;
									if (getConfig().getBoolean("randomsleepmsgs")){
										int maxmsgs = getConfig().getInt("numberofsleepmsgs");
										int randomnumber = RandomNumber(maxmsgs);
										sleepmsg = getConfig().getString("sleepmsg" + randomnumber, ChatColor.WHITE + "<player> is sleeping");
										sleepmsg = sleepmsg.replace("<colon>", ":");
										if(debug){logDebug(" PIS ... maxmsgs=" + maxmsgs);}
										if(debug){logDebug(" PIS ... randomnumber=" + randomnumber);}
									}else{
										sleepmsg = (ChatColor.WHITE + "<player> is sleeping");
										if(debug){logDebug(" PIS ... randomsleepmsgs=false");}
									}
									if(is116){
										if(debug){logDebug(" PIS sleepmsg=" + sleepmsg);}
										sleepmsg = ChatColorUtils.setNametoRGB(sleepmsg);
										if(debug){logDebug(" PIS sleepmsg=" + sleepmsg);}
										sleepmsg = StrUtils.parseRGBNameColors(sleepmsg);
										if(debug){logDebug(" PIS sleepmsg=" + sleepmsg);}
									}else{
										if(debug){logDebug(" PIS sleepmsg=" + sleepmsg);}
										sleepmsg = StrUtils.stripRGBColors(sleepmsg);// strip RGBHEX TODO: stripRGBColors
										if(debug){logDebug(" PIS sleepmsg=" + sleepmsg);}
										sleepmsg = ChatColorUtils.setColors(sleepmsg);// SetColorsByName
										if(debug){logDebug(" PIS sleepmsg=" + sleepmsg);}
									}
									if(debug){logDebug(" PIS sleepmsg=" + sleepmsg);}
									//sleepmsg = "<player> Has passed Go, Collected their $200, and checked in at Old Kent Road!";
										
										/**if(sleepmsg.length() > 54){
											sleepmsg = addChar(sleepmsg, msgcolor, 55);
										}*/
									damsg = damsg.replace("sleepmsg", sleepmsg);
									damsg = ChatColorUtils.setColors(damsg);
									//damsg = StrUtils.parseRGBNameColors(damsg);
									/** nickname parser */
									String nickName = getNickname(player);
									String playercolor = "";
									if(!nickName.contains("§")){
										//logWarn("nickName ! contain SS");
										playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
									}else{
										nickName = StrUtils.parseRGBNameColors(nickName);
									}
									/** end nickname parser */
									
										//String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
										if(debug){logDebug(" PIS ... playercolor=" + playercolor);}
									damsg = damsg.replace("<player>", /**playercolor +*/ nickName);
										//String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
										//if(debug){logDebug(" PIS ... cancelcolor=" + cancelcolor);}
									//damsg = damsg.replace("dacancel", cancelcolor + get("cancel") + msgcolor);
									//change cancel color based on config
									//damsg = damsg.replace("tooltip", "" + get("clickcancel"));
									if(debug){logDebug(" PIS string processed. ...");}
									//String oldString = cancelcolor + get("cancel") + msgcolor;
									//damsg = damsg.replace(oldString, "").replace(" [\"", " \"").replace("]\"", "\"").replace(",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"Click to cancel sleep\"}", "");
									
									if(getConfig().getBoolean("broadcast_per_world", true)){
										sendJson(player.getWorld(), damsg, canmsg);
									}else{
										sendJson(damsg, canmsg);
									}
									
									//SendJsonMessages.SendAllJsonMessage(damsg, cancelcolor + get("cancel") + msgcolor, world);
									if(debug){logDebug(" PIS SendAllJsonMessage. ...");}
									
									transitionTaskUnrestricted = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
										public void run() {
											setDatime(player, world);
											resetPlayersRestStat(world);
										}						
									}, sleepdelay * 20);
									
								} else {
									if(!isCanceled&&!event.isCancelled()){
										if(player.hasPermission("sps.hermits")||player.hasPermission("sps.op")){
											if(debug){logDebug(" PIS Has perm or is op. ...");}
											
											//Broadcast to Server
											String dastring = "" + get("sps.message.issleep");
												
											dastring = dastring.replace("<player>", "");
											String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
											if(debug){logDebug(" PIS ... msgcolor=" + msgcolor);}
											String CancelBracketColor = ChatColorUtils.setColorsByName(getConfig().getString("cancelbracketcolor", "YELLOW"));
											if(debug){logDebug(" PIS ... CancelBracketColor=" + CancelBracketColor);}
											//String damsg = "[\"\",{\"text\":\"player\"},{\"text\":\" is sleeping [\"},{\"text\":\"dacancel\",\"color\":\"red\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"tooltip\"}]}}},{\"text\":\"]\",\"color\":\"none\",\"bold\":false}]";
											//String damsg = "[\"\",{\"text\":\"sleepmsg [\"},{\"text\":\"dacancel\",\"color\":\"red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}},{\"text\":\"]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}}]";
											String canmsg = CancelBracketColor + "[\"},{\"text\":\"dacancel\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}},{\"text\":\"" + CancelBracketColor + "]\"";
											String damsg = "[\"\",{\"text\":\"sleepmsg " + canmsg + "}]";
											String sleepmsg;
											if (getConfig().getBoolean("randomsleepmsgs")){
												int maxmsgs = getConfig().getInt("numberofsleepmsgs");
												int randomnumber = RandomNumber(maxmsgs);
												sleepmsg = getConfig().getString("sleepmsg" + randomnumber, ChatColor.WHITE + "<player> is sleeping");
												sleepmsg = sleepmsg.replace("<colon>", ":");
												if(debug){logDebug(" PIS ... maxmsgs=" + maxmsgs);}
												if(debug){logDebug(" PIS ... randomnumber=" + randomnumber);}
											}else{
												sleepmsg = (ChatColor.WHITE + "<player> is sleeping");
												if(debug){logDebug(" PIS ... randomsleepmsgs=false");}
											}
											if(is116){
												if(debug){logDebug(" PIS sleepmsg=" + sleepmsg);}
												sleepmsg = ChatColorUtils.setNametoRGB(sleepmsg);
												if(debug){logDebug(" PIS sleepmsg=" + sleepmsg);}
												sleepmsg = StrUtils.parseRGBNameColors(sleepmsg);
												if(debug){logDebug(" PIS sleepmsg=" + sleepmsg);}
											}else{
												if(debug){logDebug(" PIS sleepmsg=" + sleepmsg);}
												sleepmsg = StrUtils.stripRGBColors(sleepmsg);// strip RGBHEX TODO: stripRGBColors
												if(debug){logDebug(" PIS sleepmsg=" + sleepmsg);}
												sleepmsg = ChatColorUtils.setColors(sleepmsg);// SetColorsByName
												if(debug){logDebug(" PIS sleepmsg=" + sleepmsg);}
											}
											if(debug){logDebug(" PIS sleepmsg=" + sleepmsg);}
											//sleepmsg = "<player> Has passed Go, Collected their $200, and checked in at Old Kent Road!";
												
												/**if(sleepmsg.length() > 54){
													sleepmsg = addChar(sleepmsg, msgcolor, 55);
												}*/
											damsg = damsg.replace("sleepmsg", sleepmsg).replace("\"]\"", "\"" + msgcolor + "]\"");
											damsg = ChatColorUtils.setColors(damsg);
											//damsg = StrUtils.parseRGBNameColors(damsg);
											/** nickname parser */
											String nickName = getNickname(player);
											String playercolor = "";
											if(!nickName.contains("§")){
												//logWarn("nickName ! contain SS");
												playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
											}else{
												nickName = StrUtils.parseRGBNameColors(nickName);
											}
											/** end nickname parser */
											
												//String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
												if(debug){logDebug(" PIS ... playercolor=" + playercolor);}
											damsg = damsg.replace("<player>", /**playercolor +*/ nickName);
												String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
												if(debug){logDebug(" PIS ... cancelcolor=" + cancelcolor);}
											damsg = damsg.replace("dacancel", cancelcolor + get("sps.message.cancel") + msgcolor);
											//change cancel color based on config
											damsg = damsg.replace("tooltip", "" + get("sps.message.clickcancel"));
											if(debug){logDebug(" PIS string processed. ...");}
											//String oldString = cancelcolor + get("cancel") + msgcolor;
											//damsg = damsg.replace(oldString, "").replace(" [\"", " \"").replace("]\"", "\"").replace(",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"Click to cancel sleep\"}", "");
											
											if(getConfig().getBoolean("broadcast_per_world", true)){
												sendJson(player.getWorld(), damsg, canmsg);
											}else{
												sendJson(damsg, canmsg);
											}
											
											//SendJsonMessages.SendAllJsonMessage(damsg, cancelcolor + get("cancel") + msgcolor, world);
											if(debug){logDebug(" PIS SendAllJsonMessage. ...");}
			
											//Thread.sleep(10000);
											if(!isCanceled&&!event.isCancelled()){
												if(debug){logDebug(" PIS !isCanceled. ...");}
												transitionTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
								
													public void run() {
														setDatime(player, world);
														resetPlayersRestStat(world);
														if(debug){logDebug(" PIS setDatime has run. ...");}
													}
													
												}, sleepdelay * 20);
												
											}else{
												if(isCanceled){if(debug){logDebug("PIS isCanceled=" + isCanceled);}}
												if(event.isCancelled()){if(debug){logDebug("PIS event.isCanceled=" + event.isCancelled());}}
												isCanceled = false;
											}
											//player.sendMessage(ChatColor.RED + "isCanceled=" + isCanceled);
										}else{ //Player doesn't have permission so tell them
											player.sendMessage(ChatColor.YELLOW + "" + get("sps.message.noperm"));
										}
									}else{
										isCanceled = false;
										if(isCanceled){if(debug){logDebug("PIS isCanceled=" + isCanceled);}}
										if(event.isCancelled()){if(debug){logDebug("PIS event.isCanceled=" + event.isCancelled());}}
									}
								}
							}
						//}//
				}else{ //It is not Night or Storming so tell the player
					if(getConfig().getBoolean("notifymustbenight")){
						player.sendMessage(ChatColorUtils.setColors("" + get("sps.message.nightorstorm")));
						if(debug){logDebug(" it was not night and player was notified. ...");}
					}
					//if(debug){logDebug("getBedSpawnLocation=" + player.getBedSpawnLocation());}
					//if(debug){logDebug("getBed=" + event.getBed().getLocation());}
					//player.getBedSpawnLocation().equals(event.getBed().getLocation()
					String sv = serverVersion();
					if(!(Integer.parseInt(sv) >= 15)){
						Block bed = event.getBed();
						Location bedSpawn = player.getBedSpawnLocation();
						if(bedSpawn != null){
							boolean isSameBed = checkradius(bedSpawn, event.getBed().getLocation(), 5);
							if (!isSameBed||player.getBedSpawnLocation().equals(null)) {
								if(player.getBedSpawnLocation().equals(null)){
									if(debug){logDebug("bedspawn=null");}
								}else if(!isSameBed){
									if(debug){logDebug("bedspawn!=bed");}
								}
								player.setBedSpawnLocation(event.getBed().getLocation());
								player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + get("sps.message.respawnpointmsg").toString().replace("<x>", "" + bed.getX()).replace("<z>", "" + bed.getZ()));
								if(debug){logDebug(" bedspawn was set for player " + ChatColor.GREEN + player.getDisplayName() + ChatColor.RESET + " ...");}
							}
						}else{
							player.setBedSpawnLocation(event.getBed().getLocation());
							player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + get("sps.message.respawnpointmsg").toString().replace("<x>", "" + bed.getX()).replace("<z>", "" + bed.getZ()));
							if(debug){logDebug(" bedspawn was set for player " + ChatColor.GREEN + player.getDisplayName() + ChatColor.RESET + " ...");}
						}
					}else{
						if(debug){logDebug("Server is 1.15+");}
					}
				}
			}
		}else{
			player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + get("sps.message.bloodmoon", "You can not sleep during a bloodmoon.").toString());
			event.setCancelled(true);
		}
		if(debug){logDebug(ChatColor.RED + "** End PlayerBedEnterEvent **");}
		isCanceled =  false;
	}
	
	public boolean checkradius(Location player, Location event, int radius){
		double distance = player.distance(event);
		if(distance <= radius) {
			if(debug){logDebug("truedistance=" + distance);}
			return true;
			//shulker.teleport(block.getLocation());
		}
		if(debug){logDebug("falsedistance=" + distance);}
		return false;
	}
	
	public String serverVersion(){
		String v = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		String v2 = v.split("_")[1];
		return v2;
	}
	
	public void setDatime(Player player, World world){
		if(world.hasStorm()){
			if(player.hasPermission("sps.downfall")){
				world.setStorm(false);
				if(debug){logDebug("" + get("sps.message.setdownfall") + "...");}
			}else{
				if(debug){logDebug("" + getNickname(player) + " Does not have permission sps.downfall ...");}
			}
		}
		if(world.isThundering()){
			if(player.hasPermission("sps.thunder")){
				world.setThundering(false);
				if(debug){logDebug("" + get("sps.message.setthunder") + "...");}
			}else{
				if(debug){logDebug("" + getNickname(player) + " Does not have permission sps.thunder ...");}
			}
		}
		String waketime = getConfig().getString("waketime", "NORMAL");
		long timeoffset = 0;
		if(waketime.equalsIgnoreCase("early")||waketime.equalsIgnoreCase("23000")){
			timeoffset = 1000;
		}else{timeoffset = 0;}
		long Relative_Time = (24000 - world.getTime()) - timeoffset;
		long daFullTime = world.getFullTime();
		world.setFullTime(daFullTime + Relative_Time);
		/** for dumdan and mueks sync all three worlds time.
		World Lodestar = Bukkit.getWorld("Lodestar_the_end");
		Lodestar.setFullTime(daFullTime + Relative_Time);
		World Lodestar2 = Bukkit.getWorld("Lodestar_nether");
		Lodestar2.setFullTime(daFullTime + Relative_Time);
		*/
		if(debug){logDebug("" + get("sps.message.settime") + "...");}
	}
	
	public void setDStime(Player player, World world){
		int timeoffset = 10000;
		long Relative_Time = (24000 - world.getTime()) - timeoffset;
		world.setFullTime(world.getFullTime() + Relative_Time);
		if(debug){logDebug("" + get("sps.message.dayskipsettime") + "...");}
	}
	
	@Override 
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) { // TODO: Tab Complete
		if (command.getName().equalsIgnoreCase("sps")) {
			List<String> autoCompletes = new ArrayList<>(); //create a new string list for tab completion
			if (args.length == 1) { // reload, toggledebug, playerheads, customtrader, headfix
				autoCompletes.add("reload");
				autoCompletes.add("toggledebug");
				autoCompletes.add("update");
				autoCompletes.add("check");
				return autoCompletes; // then return the list
			}
			if(args[0].equalsIgnoreCase("check")) {
				return null;
			}
		}
		return null;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		if (command.getName().equalsIgnoreCase("SPS")){
			if (args.length == 0){
				sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SinglePlayerSleep" + ChatColor.GREEN + "]===============[]");
				sender.sendMessage(ChatColor.YELLOW + " " + get("sps.message.touse"));//Sleep in a bed to use.");
				sender.sendMessage(ChatColor.WHITE + " ");
				sender.sendMessage(ChatColor.WHITE + " /Sleep - " + get("sps.message.sleephelp"));//subject to server admin approval");
				sender.sendMessage(ChatColor.WHITE + " /spscancel - " + get("sps.command.cancelhelp"));//Cancels SinglePlayerSleep");
				sender.sendMessage(ChatColor.WHITE + " ");
				if(sender.isOp()||sender.hasPermission("sps.op")){
					sender.sendMessage(ChatColor.GOLD + " OP Commands");
					sender.sendMessage(ChatColor.GOLD + " /SPS Reload - " + get("sps.command.spsreload"));//Reload config file.");
					sender.sendMessage(ChatColor.GOLD + " /SPS Toggledebug - " + get("sps.command.toggledebug"));//Check for update.");
					sender.sendMessage(ChatColor.GOLD + " /SPS Update - " + get("sps.command.spsupdate"));//Check for update.");
					//sender.sendMessage(ChatColor.GOLD + " /SPS check true/false - " + get("spscheck"));//set auto-update-check to true or false.");
				}
				sender.sendMessage(ChatColor.GOLD + " /SPS Check - Check your Permissions.");
				sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SinglePlayerSleep" + ChatColor.GREEN + "]===============[]");
				return true;
			}
			
			if(args[0].equalsIgnoreCase("check")){
				// /sps check
				// /sps check @p
				// /cmd 0     1
				if(args.length == 1){
					String damsg = "sps.hermits=" + sender.hasPermission("sps.hermits") + ", " + "sps.cancel=" + sender.hasPermission("sps.cancel") + ", " + 
							"sps.unrestricted=" + sender.hasPermission("sps.unrestricted") + ", " + "sps.downfall=" + sender.hasPermission("sps.downfall") + ", " + 
							"sps.thunder=" + sender.hasPermission("sps.thunder") + ", " + "sps.command=" + sender.hasPermission("sps.command") + ", " + 
							"sps.update=" + sender.hasPermission("sps.update") + ", " + "sps.op=" + sender.hasPermission("sps.op") + ", " + 
							"sps.showUpdateAvailable=" + sender.hasPermission("sps.showUpdateAvailable") + ", " + 
							"sps.dayskipper=" + sender.hasPermission("sps.dayskipper") + ", " + "sps.dayskipcommand=" + sender.hasPermission("sps.dayskipcommand") ;
					sender.sendMessage(damsg.replace("=true,", "=" + ChatColor.GREEN + "true" + ChatColor.RESET + ",").replace("=false,", "=" + ChatColor.RED + "false" + ChatColor.RESET + ","));
					return true;
				}else if(args.length > 1){
					if(!(sender instanceof Player)||sender.hasPermission("sps.op")) {
						try {
							Player player = Bukkit.getPlayer(args[1]);
							String damsg = "Player \"" + player.getName() + "\" has the following permissions: sps.hermits=" + player.hasPermission("sps.hermits") + ", " + "sps.cancel=" + player.hasPermission("sps.cancel") + ", " + 
									"sps.unrestricted=" + player.hasPermission("sps.unrestricted") + ", " + "sps.downfall=" + player.hasPermission("sps.downfall") + ", " + 
									"sps.thunder=" + player.hasPermission("sps.thunder") + ", " + "sps.command=" + player.hasPermission("sps.command") + ", " + 
									"sps.update=" + player.hasPermission("sps.update") + ", " + "sps.op=" + player.hasPermission("sps.op") + ", " + 
									"sps.showUpdateAvailable=" + player.hasPermission("sps.showUpdateAvailable") + ", " + 
									"sps.dayskipper=" + player.hasPermission("sps.dayskipper") + ", " + "sps.dayskipcommand=" + player.hasPermission("sps.dayskipcommand") ;
							sender.sendMessage(damsg.replace("=true,", "=" + ChatColor.GREEN + "true" + ChatColor.RESET + ",").replace("=false,", "=" + ChatColor.RED + "false" + ChatColor.RESET + ","));
							return true;
						}catch(Exception e) {
							sender.sendMessage("Error Player Not found");
							e.printStackTrace();
							return false;
						}
					}else {
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.noperm"));
			    		return false;
					}
				}
			}

		    if(args[0].equalsIgnoreCase("toggledebug")||args[0].equalsIgnoreCase("td")){
		    	if(sender.isOp()||sender.hasPermission("sps.op")||!(sender instanceof Player)){
		    		debug = !debug;
		    		sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.debugtrue").toString().replace("<boolean>", get("sps.message.boolean." + debug) ));
		    		return true;
		    	}else if(!sender.hasPermission("sps.op")){
		    		sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.noperm"));
		    		return false;
		    	}
		    }
			if(args[0].equalsIgnoreCase("reload")){ // TODO: Command Reload
				if(sender.isOp()||sender.hasPermission("sps.op")||!(sender instanceof Player)||sender.hasPermission("sps.*")){
					//ConfigAPI.Reloadconfig(this, p);
					config = new YmlConfiguration();
					try {
						config.load(new File(getDataFolder(), "config.yml"));
					} catch (IOException | InvalidConfigurationException e) {
						e.printStackTrace();
					}
					/**this.reloadConfig();
					SinglePlayerSleep plugin = this;
					getServer().getPluginManager().disablePlugin(plugin);
					getServer().getPluginManager().enablePlugin(plugin);//*/
					reloadConfig();
					blacklist_sleep = config.getString("blacklist.sleep", "");
					blacklist_dayskip = config.getString("blacklist.dayskip", "");
					colorful_console = getConfig().getBoolean("colorful_console", true);
					lang = new YamlConfiguration();
					try {
						lang.load(langFile);
					} catch (IOException | InvalidConfigurationException e1) {
						e1.printStackTrace();
					}
					sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.reloaded"));
				}else if(!sender.hasPermission("sps.op")){
					sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.noperm"));
				}
			}
			if(args[0].equalsIgnoreCase("update")){ // TODO: Command update
				if(!(sender instanceof Player)) {
					/** Console */
					try {
						Bukkit.getConsoleSender().sendMessage("Checking for updates...");
						VersionChecker updater = new VersionChecker(this, projectID, githubURL);
						if(updater.checkForUpdates()) {
							/** Update available */
							UpdateAvailable = true; // TODO: Update Checker
							UColdVers = updater.oldVersion();
							UCnewVers = updater.newVersion();
							
							log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
							log("* " + get("sps.version.message").toString().replace("<MyPlugin>", THIS_NAME) );
							log("* " + get("sps.version.old_vers") + ChatColor.RED + UColdVers );
							log("* " + get("sps.version.new_vers") + ChatColor.GREEN + UCnewVers );
							log("*");
							log("* " + get("sps.version.please_update") );
							log("*");
							log("* " + get("sps.version.download") + ": " + DownloadLink + "/history");
							log("* " + get("sps.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
							log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
							//Bukkit.getConsoleSender().sendMessage(newVerMsg.replace("{oVer}", UColdVers).replace("{nVer}", UCnewVers));
							//Bukkit.getConsoleSender().sendMessage(Ansi.GREEN + UpdateChecker.getResourceUrl() + Ansi.RESET);
						}else{
							/** Up to date */
							UpdateAvailable = false;
							log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
							log("* " + ChatColor.YELLOW + THIS_NAME + ChatColor.RESET + " " + get("sps.version.curvers") + ChatColor.RESET );
							log("* " + get("sps.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
							log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
						}
					}catch(Exception e) {
						/** Error */
						Bukkit.getConsoleSender().sendMessage(ChatColor.RED + get("sps.version.update.error"));
						e.printStackTrace();
					}
					/** end update checker */
					return true;
				}
				if((sender.isOp()||sender.hasPermission("sps.op")||sender.hasPermission("sps.showUpdateAvailable"))){
					BukkitTask updateTask = this.getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
						public void run() {
							try {
								Bukkit.getConsoleSender().sendMessage("Checking for updates...");
								VersionChecker updater = new VersionChecker(THIS_VERSION, projectID, githubURL);
								if(updater.checkForUpdates()) {
									UpdateAvailable = true;
									UColdVers = updater.oldVersion();
									UCnewVers = updater.newVersion();
									String links = "[\"\",{\"text\":\"<Download>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/history\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\" \",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\"| \"},{\"text\":\"<Donate>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://ko-fi.com/joelgodofwar\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Donate_msg>\"}},{\"text\":\" | \"},{\"text\":\"<Notes>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/updates\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Notes_msg>.\"}}]";
									links = links.replace("<DownloadLink>", DownloadLink).replace("<Download>", get("sps.version.download"))
											.replace("<Donate>", get("sps.version.donate")).replace("<please_update>", get("sps.version.please_update"))
											.replace("<Donate_msg>", get("sps.version.donate.message")).replace("<Notes>", get("sps.version.notes"))
											.replace("<Notes_msg>", get("sps.version.notes.message"));
									String versions = "" + ChatColor.GRAY + get("sps.version.new_vers") + ": " + ChatColor.GREEN + "{nVers} | " + get("sps.version.old_vers") + ": " + ChatColor.RED + "{oVers}";
									sender.sendMessage("" + ChatColor.GRAY + get("sps.version.message").toString().replace("<MyPlugin>", ChatColor.GOLD + THIS_NAME + ChatColor.GRAY) );
									Utils.sendJson(sender, links);
									sender.sendMessage(versions.replace("{nVers}", UCnewVers).replace("{oVers}", UColdVers));
								}else{
									String links = "{\"text\":\"<Donate>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://ko-fi.com/joelgodofwar\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Donate_msg>\"}}";
									links = links.replace("<Donate>", get("sps.version.donate")).replace("<Donate_msg>", get("sps.version.donate.message"));
									Utils.sendJson(sender, links);
									sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " v" + THIS_VERSION + ChatColor.RESET + " " + get("sps.version.curvers") + ChatColor.RESET);
									UpdateAvailable = false;
								}
							}catch(Exception e) {
								sender.sendMessage(ChatColor.RED + get("sps.version.update.error"));
								e.printStackTrace();
							}
						}
					});
					return true;
				}else{
					sender.sendMessage(ChatColor.YELLOW + THIS_NAME + " " + get("sps.message.notop"));
					return false;
				}
			}
		}
		
		if(command.getName().equalsIgnoreCase("spsbloodmoon")){
			if (sender instanceof ConsoleCommandSender) {
				isBloodMoon = !isBloodMoon;
				if(debug){logDebug("isBloodMoon=" + isBloodMoon);}
				return true;
			}else{
				sender.sendMessage("Console only command.");
				return false;
			}
		}
		
		if(command.getName().equalsIgnoreCase("spscancel")){ //command.getName().equalsIgnoreCase("cancel") // TODO: Command spscancel
			if(debug){logDebug("command Can command cancel selected");}
			World world;
			Player player;
			List<World> worlds = Bukkit.getWorlds();
			
			if(sender.hasPermission("sps.cancel") || sender.hasPermission("sps.op")) {
				if(sender.hasPermission("sps.cancel")){if(debug){logDebug("command Can " + sender.getName() + " has sps.cancel");}}
				if(sender.hasPermission("sps.op")){if(debug){logDebug("command Can " + sender.getName() + " has sps.op");}}
				if(sender instanceof Player){
					player = (Player) sender;
					world = player.getWorld();
					
					//Set default timer for when the player has never slept before
					long timer = 0;
					if(debug){logDebug("command Can... " + player.getName() + " is sleeping.");}
					long time = System.currentTimeMillis() / 1000;
					if(cancellimit.get(player.getUniqueId()) == null){
						if(debug){logDebug("null - player is not in cancellimit");}
						// Check if player has sps.unrestricted
						if (!player.hasPermission("sps.unrestricted")) {
							// Set player's time in HashMap
							cancellimit.put(player.getUniqueId(), time);
							if(debug){logDebug("command Can " + player.getName() + " added to playersCancelled");}
						}
					}else{
						if(debug){logDebug("not null - player is in cancellimit");}
						// Player is on the list.
						timer = cancellimit.get(player.getUniqueId());
						if(debug){logDebug("time=" + time);}
						if(debug){logDebug("timer=" + timer);}
						if(debug){logDebug("time - timer=" +  (time - timer));}
						if(debug){logDebug("cancellimit=" + getConfig().getLong("cancellimit", 60));}
						// if !time - timer > limit
						if(!((time - timer) > getConfig().getLong("cancellimit", 60))){
							long length = getConfig().getLong("cancellimit", 60) - (time - timer) ;
							String sleeplimit = "" + get("sps.message.sleeplimit").toString().replace("<length>", "" + length);
							player.sendMessage(ChatColor.YELLOW + sleeplimit);
							if(debug){logDebug("command Can... cancellimit: " + sleeplimit);}
							//player.sendMessage("You can not do that for " + length + " seconds");
							return false;
						}else if((time - timer) > getConfig().getLong("cancellimit", 60)){
							if(debug){logDebug("time - timer > cancellimit");}
							cancellimit.replace(player.getUniqueId(), time);
						}
					}
				}else{
					world = Bukkit.getWorlds().get(0);
				}
				
				/* Check if it's Day */
				if(IsDay(world)){
					if(debug){logDebug("command Can It is Day");}
					if (!getConfig().getBoolean("unrestricteddayskipper")) {
						if(debug){logDebug("command Can !unrestricted DaySkipper");}
						if (Bukkit.getScheduler().isCurrentlyRunning((dayskipTask)) || Bukkit.getScheduler().isQueued((dayskipTask))) {
							if(debug){logDebug("command Can DS runnable is scheduled");}
							/**
							long time = System.currentTimeMillis() / 1000;
							//Set default timer
							long timer = 0;
							long pTimeCancel = 0;
							if (playersCancelled.get(sender.getName()) != null) {
								pTimeCancel = playersCancelled.get(sender.getName());
								if(debug){logDebug("command Can DS playerscancelled is not null");}
							}
							//check if player has already tried cancelling to prevent spam
							if (getConfig().getInt("sleeplimit") > 0) {
								timer = time - pTimeCancel;
								if(debug){logDebug("command Can DS timer is: " + timer);}
							}
							//Tell the player why they can't sleep
							if (timer < getConfig().getInt("sleeplimit")) {		
								String sleeplimit = "" + get("sps.message.sleeplimit").toString();
								sender.sendMessage(ChatColor.YELLOW + sleeplimit);
								if(debug){logDebug("command Can DS tell player why they cant sleep");}
							} else {/ */
								if(debug){logDebug("command Can DS sleeplimit not reached");}
								//Set the time this player cancelled to prevent spam
								//playersCancelled.put(sender.getName().toString(), time);
								//if(debug){logDebug("command Can DS added to playersCancelled");}
								//cancel the runnable task
								Bukkit.getScheduler().cancelTask(dayskipTask);
								if(debug){logDebug("command Can DS task cancelled");}
								//Broadcast to Server
								if(debug){logDebug("cancelbroadcast=" + getConfig().getBoolean("cancelbroadcast", false));}
								if (!(getConfig().getBoolean("cancelbroadcast", false) == false)) {
									if(debug){logDebug("command Can DS is it here?");}
									String damsg = "[\"\",{\"text\":\"cancelmsg\"}]";
									//String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
									String msgcolor1 = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
									damsg = damsg.replace("cancelmsg", get("sps.message.dayskipcanceled").toString());
									/** nickname parser */
									String nickName = getNickname(sender);
									String playercolor = "";
									if(!nickName.contains("§")){
										//logWarn("nickName ! contain SS");
										playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
									}else{
										nickName = StrUtils.parseRGBNameColors(nickName);
									}
									/** end nickname parser */
									damsg = damsg.replace("<player>", /**playercolor +*/ nickName + msgcolor1);
									if(debug){logDebug("command Can DS damsg=" + damsg);}
									
									if(getConfig().getBoolean("broadcast_per_world", true)){
										sendJson(world, damsg, "");
									}else{
										sendJson(damsg, "");
									}
									
									//SendJsonMessages.SendAllJsonMessage(damsg, "", world);
									if(debug){logDebug("command Can DS broadcast sent");}
								}else if (getConfig().getBoolean("cancelbroadcast", false) == false){
									if(debug){logDebug("command Can DS broadcast = false");}
								}
								isCanceled = true;
								return true;
							//}//
						} else { //tell player they can't cancel sleep
							sender.sendMessage(ChatColor.YELLOW + "" + get("sps.message.nocancel"));
						}
						
					} else { //unrestricted sleep is on tell the player
						sender.sendMessage(ChatColor.YELLOW + "" + get("sps.message.cancelunrestricted"));
					}
						
				}else { //it's not night tell player
					//sender.sendMessage(ChatColor.YELLOW + "" + get("mustbeday"));
				}
				
				//Check it's night
				if (IsNight(world)||world.hasStorm()) {
					if(debug){if(IsNight(worlds.get(0))){logDebug("command Can It is night");}}
					if(debug){if(worlds.get(0).hasStorm()){logDebug("command Can it is storming");}}
					//Bukkit.getServer().getWorld("");
					//Prevent cancelling if unrestricted sleep is enabled
					if (!getConfig().getBoolean("unrestrictedsleep")) {
						if(debug){logDebug("command Can !unrestricted sleep");}
						
						//Check if this is an unrestricted sleep or not
						if (Bukkit.getScheduler().isCurrentlyRunning((transitionTask)) || Bukkit.getScheduler().isQueued((transitionTask))) {
							if(debug){logDebug("command Can sleep runnable is scheduled");}
							
							
							/** /
							long time = System.currentTimeMillis() / 1000;
							//Set default timer
							long timer = 0;
							long pTimeCancel = 0;
							if (playersCancelled.get(sender.getName()) != null) {
								pTimeCancel = playersCancelled.get(sender.getName());
								if(debug){logDebug("command Can playerscancelled is not null");}
							}
							//check if player has already tried cancelling to prevent spam
							if (getConfig().getInt("sleeplimit") > 0) {
								timer = time - pTimeCancel;
								if(debug){logDebug("command Can timer is: " + timer);}
							}
							//Tell the player why they can't sleep
							if (timer < getConfig().getInt("sleeplimit")) {		
								String sleeplimit = "" + get("sps.message.sleeplimit").toString();
								sender.sendMessage(ChatColor.YELLOW + sleeplimit);
								if(debug){logDebug("command Can tell player why they cant sleep");}
							} else {/ */
								if(debug){logDebug("command Can sleeplimit not reached");}
								//Set the time this player cancelled to prevent spam
								//playersCancelled.put(sender.getName().toString(), time);
								
								//cancel the runnable task
								Bukkit.getScheduler().cancelTask(transitionTask);
								isCanceled = false;
								if(debug){logDebug("command Can task cancelled");}
								//Broadcast to Server
								
								if (!(getConfig().getBoolean("cancelbroadcast", false) == false)) {
									if(debug){logDebug("command Can is it here?");}
									String damsg = "[\"\",{\"text\":\"<player> canceled sleeping.\"}]";
									//String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
									String msgcolor1 = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
									damsg = damsg.replace("<player> canceled sleeping.", get("sps.message.canceledsleep").toString());
									/** nickname parser */
									String nickName = getNickname(sender);
									String playercolor = "";
									if(!nickName.contains("§")){
										//logWarn("nickName ! contain SS");
										playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
									}else{
										nickName = StrUtils.parseRGBNameColors(nickName);
									}
									/** end nickname parser */
									damsg = damsg.replace("<player>", /**playercolor +*/ nickName + msgcolor1);
									if(debug){logDebug("command Can damsg=" + damsg);}
									
									if(getConfig().getBoolean("broadcast_per_world", true)){
										sendJson(world, damsg, "");
									}else{
										sendJson(damsg, "");
									}
									
									//SendJsonMessages.SendAllJsonMessage(damsg, "", world);
									if(debug){logDebug("command Can broadcast sent");}
								}else if (getConfig().getBoolean("cancelbroadcast", false) == false){
									if(debug){logDebug("command Can broadcast = false");}
								}
								isCanceled = true;
								//
								double oldHealth;
								GameMode oldGamemode;
								Location location;
								Location bedspawn;
								
								//Sleep canceled so kick players from beds.
								for (Player p: Bukkit.getOnlinePlayers()){
									player = p;// ((CraftPlayer)p);
									if(debug){logDebug("command Can cycling player " + player.getName());}
									
									//if(debug){logDebug("command Can cancel player=" + player.getDisplayName());}
									
									try {
										bedspawn = player.getBedSpawnLocation();
										bedspawn = new Location(bedspawn.getWorld(), bedspawn.getBlockX(),bedspawn.getBlockY(),bedspawn.getBlockZ(),0,0);
										if(debug){logDebug("command Can bedspawn=" + bedspawn);}
										location = player.getLocation();
										location = new Location(location.getWorld(), location.getBlockX(),location.getBlockY(),location.getBlockZ(),0,0);
										if(debug){logDebug("command Can location=" + location);}
										boolean inbed = false;
										
										if (location.equals(bedspawn)){
											inbed = true;
											}
										else{
											if(bedspawn.distance(player.getLocation()) < 2){
												if(debug){logDebug("command Can distance < 2 - inbed=true");}
												inbed = true;
											}
											location.add(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location1=" + location);}
												inbed = true;
											}
											location.add(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=2" + location);}
												inbed = true;
											}
											location.subtract(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=3" + location);}
												inbed = true;
											}
											location.subtract(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=4" + location);}
												inbed = true;
											}
											location.subtract(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=5" + location);}
												inbed = true;
											}
											location.subtract(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=6" + location);}
												inbed = true;
											}
											location.add(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=7" + location);}
												inbed = true;
											}
											location.add(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location8=" + location);}
												inbed = true;
											}
											location.add(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location9=" + location);}
												inbed = true;
											}
											location.add(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=10" + location);}
												inbed = true;
											}
											location.add(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=11" + location);}
												inbed = true;
											}
											location.add(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=12" + location);}
												inbed = true;
											}
											location.subtract(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=13" + location);}
												inbed = true;
											}
											location.subtract(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=14" + location);}
												inbed = true;
											}
											location.subtract(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=15" + location);}
												inbed = true;
											}
											location.subtract(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=16" + location);}
												inbed = true;
											}
											location.subtract(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=17" + location);}
												inbed = true;
											}
											location.subtract(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=18" + location);}
												inbed = true;
											}
											location.subtract(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=19" + location);}
												inbed = true;
											}
											location.subtract(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=20" + location);}
												inbed = true;
											}
											location.add(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=21" + location);}
												inbed = true;
											}
											location.add(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location22=" + location);}
												inbed = true;
											}
											location.add(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location=23" + location);}
												inbed = true;
											}
											location.add(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("command Can location24=" + location);}
												inbed = true;
											}
											
										}
										
										if(inbed){
											oldGamemode = player.getGameMode();
											oldHealth = player.getHealth();
											if(debug){logDebug("command Can oldHEalth=" + oldHealth);}
											if(debug){logDebug("command Can GameMode=" + oldGamemode.toString());}
											if(oldGamemode != GameMode.SURVIVAL){
												player.setGameMode(GameMode.SURVIVAL);
												if(debug){logDebug("command Can GameMode set to SURVIVAL");}
												//log("survival");
											}
											if(!(oldHealth <= 1)){
												player.damage(1);//.getHandle().a(true,DamageSource.CACTUS);
												if(debug){logDebug("command Can damage=" + player.getHealth());}
												player.setHealth(oldHealth);
												//player.wakeup(true);
											}else{
												player.setHealth(oldHealth + 1);
												player.damage(1);//.getHandle().a(true,DamageSource.CACTUS);
												if(debug){logDebug("command Can damage=" + player.getHealth());}
												player.setHealth(oldHealth);
												//player.wakeup(true);
											}
											player.setGameMode(oldGamemode);
											if(debug){logDebug("command Can GameMode set to " + oldGamemode.toString());}
											//if(player.isSleeping()){
												//player.wakeup(true);
											//}
										}
									}catch (Exception e){
										if(debug){logWarn("[Exception] " + player.getDisplayName() + " has never slept before.");}
										// Failed to submit the stats
									}
									
								}
								if(isCanceled){
									if(debug){logDebug("command Can... isCanceled set to false");}
									isCanceled = false;
								}
								return true;
							//}//
							
						} else { //tell player they can't cancel sleep
							sender.sendMessage(ChatColor.YELLOW + "" + get("sps.message.nocancel"));
							if(debug){logDebug("command Can sleep runnable is NOT scheduled");}
						}
						
					} else { //unrestricted sleep is on tell the player
						sender.sendMessage(ChatColor.YELLOW + "" + get("sps.message.cancelunrestricted"));
					}
						
				}else { //it's not night tell player
					if(getConfig().getBoolean("notifymustbenight")){
						sender.sendMessage(ChatColor.YELLOW + "" + get("sps.message.mustbenight"));
					}
				}
			}else { //Player doesn't have permission so let's tell them
				sender.sendMessage(ChatColor.RED + "" + get("sps.message.noperm"));
			}
			if(isCanceled){
				isCanceled = false;
			}
		}
		if(command.getName().equalsIgnoreCase("sleep")){ // TODO: Command Sleep
			//Player player = (Player) sender;
			List<World> worlds = Bukkit.getWorlds();
			//World w = ((Entity) sender).getWorld();
			
			if(sender.hasPermission("sps.command")||sender.hasPermission("sps.op")) {
				if(sender instanceof Player){ 
					if(!sender.hasPermission("sps.op")){
						Player player = (Player) sender;
						if(blacklist_sleep != null&&!blacklist_sleep.isEmpty()){
							if(StrUtils.stringContains(blacklist_sleep, player.getWorld().getName().toString())){
								log("EDE - World - On blacklist.");
								return false;
							}
						}
					}
				}
				//final Player player1 = (Player) sender;
				final CommandSender daSender = sender;
				World world;
				Player player;
				if(sender instanceof Player){
					player = (Player) sender;
					world = player.getWorld();
					if(!IsNight(world) && !world.hasStorm()){
						sender.sendMessage(ChatColorUtils.setColors("" + get("sps.message.nightorstorm")));
						return false;
					}
					//Set default timer for when the player has never slept before
					long timer = 0;
					if(debug){logDebug("SC " + player.getName() + " is sleeping.");}
					long time = System.currentTimeMillis() / 1000;
					if(sleeplimit.get(player.getUniqueId()) == null){
						if(debug){logDebug("SC null - player not in sleeplimit");}
						// Check if player has sps.unrestricted
						if (!player.hasPermission("sps.unrestricted")) {
							// Set player's time in HashMap
							sleeplimit.put(player.getUniqueId(), time);
							if(debug){logDebug("SC " + player.getName() + " added to playersSlept");}
						}
					}else{
						if(debug){logDebug("SC not null - player in sleeplimit");}
						// Player is on the list.
						timer = sleeplimit.get(player.getUniqueId());
						if(debug){logDebug("SC time=" + time);}
						if(debug){logDebug("SC timer=" + timer);}
						if(debug){logDebug("SC time - timer=" +  (time - timer));}
						if(debug){logDebug("SC sleeplimit=" + getConfig().getLong("sleeplimit", 60));}
						// if !time - timer > limit
						if(!((time - timer) > getConfig().getLong("sleeplimit", 60))){
							long length = getConfig().getLong("sleeplimit", 60) - (time - timer) ;
							String sleeplimit = "" + get("sps.message.sleeplimit").toString().replace("<length>", "" + length);
							player.sendMessage(ChatColor.YELLOW + sleeplimit);
							if(debug){logDebug("SC sleeplimit: " + sleeplimit);}
							//player.sendMessage("You can not do that for " + length + " seconds");
							
							return false;
						}else if((time - timer) > getConfig().getLong("sleeplimit", 60)){
							if(debug){logDebug("SC time - timer > sleeplimit");}
							sleeplimit.replace(player.getUniqueId(), time);
						}
					}
					if(!isBloodmoonInprogress(player.getWorld())){//isBloodmoonInprogress(player.getWorld())//isBloodMoon
						if(debug){logDebug("SC isbloodmoon=false");}
					}else{
						player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + get("sps.message.bloodmoon", "You can not sleep during a bloodmoon.").toString());
						return false;
					}
				}else{
					world = Bukkit.getWorlds().get(0);
					if(!IsNight(world) && !world.hasStorm()){
						sender.sendMessage(ChatColorUtils.setColors("" + get("sps.message.nightorstorm")));
						return false;
					}
				}

				//Broadcast to Server
				String sleepmsg;
				if (getConfig().getBoolean("randomsleepmsgs")){
					int maxmsgs = getConfig().getInt("numberofsleepmsgs");
					int randomnumber = RandomNumber(maxmsgs);
					sleepmsg = getConfig().getString("sleepmsg" + randomnumber, ChatColor.WHITE + "<player> is sleeping");
					sleepmsg = sleepmsg.replace("<colon>", ":");
				}else{
					sleepmsg = getConfig().getString(ChatColor.WHITE + "<player> is sleeping");
				}
				if(is116){
					sleepmsg = ChatColorUtils.setNametoRGB(sleepmsg);
					sleepmsg = StrUtils.parseRGBNameColors(sleepmsg);
				}else{
					sleepmsg = StrUtils.stripRGBColors(sleepmsg);// strip RGBHEX TODO: stripRGBColors
					sleepmsg = ChatColorUtils.setColors(sleepmsg);// SetColorsByName
				}
					String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
				//String dastring = "" + get("sleepcommand");
				//dastring = dastring.replace("<player>", "");
				//String damsg = "[\"\",{\"text\":\"player\"},{\"text\":\" is sleeping [\"},{\"text\":\"dacancel\",\"color\":\"red\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"tooltip\"}]}}},{\"text\":\"]\",\"color\":\"none\",\"bold\":false}]";
				String CancelBracketColor = ChatColorUtils.setColorsByName(getConfig().getString("cancelbracketcolor", "YELLOW"));
				String canmsg = CancelBracketColor + "[\"},{\"text\":\"dacancel\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}},{\"text\":\"" + CancelBracketColor + "]\"}";
				String damsg = "[\"\",{\"text\":\"sleepmsg " + canmsg + "]";
					String msgcolor1 = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
				damsg = damsg.replace("sleepmsg", sleepmsg);
				damsg = ChatColorUtils.setColors(damsg);
				
				//String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
				/** nickname parser */
				String nickName = getNickname(sender);
				String playercolor = "";
				if(!nickName.contains("§")){
					//logWarn("nickName ! contain SS");
					playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
				}else{
					nickName = StrUtils.parseRGBNameColors(nickName);
				}
				/** end nickname parser */
				damsg = damsg.replace("<player>", /**playercolor +*/ nickName);
					String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
				damsg = damsg.replace("dacancel", cancelcolor + get("sps.message.cancel") + msgcolor1);
				damsg = damsg.replace("tooltip", "" + get("sps.message.clickcancel")).replace("\"]\"", "\"" + msgcolor + "]\"");
				
				if(getConfig().getBoolean("broadcast_per_world", true)){
					sendJson(world, damsg, canmsg);
				}else{
					sendJson(damsg, canmsg);
				}
				
				boolean worldhasstorm = world.hasStorm();
				//SendJsonMessages.SendAllJsonMessage(damsg, "", world);
				if(sender.hasPermission("sps.hermits")||sender.hasPermission("sps.*")){
					//Thread.sleep(10000);
					
					if(!isCanceled){
						int sleepdelay = getConfig().getInt("sleepdelay", 10);
						transitionTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

							public void run() {
								//getLogger().info("runnable");
								//setDatime(sender, world);
								if(world.hasStorm()){
									if(daSender.hasPermission("sps.downfall")||daSender.hasPermission("sps.op")||daSender.hasPermission("sps.*")){
										world.setStorm(false);
										if(debug){logDebug("" + get("sps.message.setdownfall") + "...");}
									}
								}
								if(world.isThundering()){
									if(daSender.hasPermission("sps.thunder")||daSender.hasPermission("sps.op")||daSender.hasPermission("sps.*")){
										world.setThundering(false);
										if(debug){logDebug("" + get("sps.message.setthunder") + "...");}
									}
								}
								long Relative_Time = 24000 - world.getTime();
								world.setFullTime(world.getFullTime() + Relative_Time);
								if(debug){logDebug("" + get("sps.message.settime") + "...");}
								resetPlayersRestStat(world);
							}
							
						}, sleepdelay * 20);
						
					}else{
						
						isCanceled = false;
					}
					//player.sendMessage(ChatColor.RED + "isCanceled=" + isCanceled);
				}else{
					sender.sendMessage(ChatColor.RED + "" + get("sps.message.noperm"));
				}
			}else{
				sender.sendMessage(ChatColor.RED + "" + get("sps.message.noperm"));
			}
		}
		if(command.getName().equalsIgnoreCase("dayskip")){ // TODO: Command DaySkip
			if(getConfig().getBoolean("enabledayskipper", false)){
				World world;
				if(sender instanceof Player){
					Player player = (Player) sender;
					world = player.getWorld();
					if(!sender.hasPermission("sps.op")){ // TODO: DaySkip Command Blacklist Check
						if(blacklist_dayskip != null&&!blacklist_dayskip.isEmpty()){
							if(StrUtils.stringContains(blacklist_dayskip, world.getName().toString())){
								log("EDE - World - On blacklist.");
								return false;
							}
						}
					}
				}else{
					world = Bukkit.getWorlds().get(0);
				}
				List<World> worlds = Bukkit.getWorlds();
				//World w = ((Entity) sender).getWorld();
				if(!IsDay(worlds.get(0))){
					sender.sendMessage(ChatColorUtils.setColors("" + get("sps.message.mustbeday")));
					return false;
				}
				if(sender.hasPermission("sps.dayskipcommand")||sender.hasPermission("sps.op")){
					if(debug){logDebug(" DS Has perm or is op. ...");}
					String CancelBracketColor = ChatColorUtils.setColorsByName(getConfig().getString("cancelbracketcolor", "YELLOW"));
					/* OK they have the perm, now lets notify the server and schedule the runnable */
					String damsg = "[\"\",{\"text\":\"sleepmsg " + CancelBracketColor + "[\"},{\"text\":\"dacancel\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}},{\"text\":\"" + CancelBracketColor + "]\"}]";
					String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
						if(debug){logDebug(" DS ... msgcolor=" + msgcolor);}
					String sleepmsg = "" + get("sps.message.dayskipmsgcommand","<player> wants to sleep the day away...<command>");
					if(is116){
						sleepmsg = ChatColorUtils.setNametoRGB(sleepmsg);
						sleepmsg = StrUtils.parseRGBNameColors(sleepmsg);
					}else{
						sleepmsg = StrUtils.stripRGBColors(sleepmsg);// strip RGBHEX TODO: stripRGBColors
						sleepmsg = ChatColorUtils.setColors(sleepmsg);// SetColorsByName
					}
					damsg = damsg.replace("sleepmsg", sleepmsg);
					//damsg = ChatColorUtils.setColors(damsg);
					
					//String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
					/** nickname parser */
					String nickName = getNickname(sender);
					String playercolor = "";
					if(!nickName.contains("§")){
						//logWarn("nickName ! contain SS");
						playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
					}else{
						nickName = StrUtils.parseRGBNameColors(nickName);
					}
					/** end nickname parser */
						if(debug){logDebug(" DS ... playercolor=" + playercolor);}
					damsg = damsg.replace("<player>", /**playercolor +*/ nickName);
					String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
						if(debug){logDebug(" DS ... cancelcolor=" + cancelcolor);}
					damsg = damsg.replace("dacancel", cancelcolor + get("sps.message.dayskipcancel"));
					//change cancel color based on config
					damsg = damsg.replace("tooltip", "" + get("sps.message.dayskipclickcancel")).replace("\"]\"", "\"" + msgcolor + "]\"");
						if(debug){logDebug(" DS string processed. ...");}
						
						if(getConfig().getBoolean("broadcast_per_world", true)){
							sendJson(world, damsg, "");
						}else{
							sendJson(damsg, "");
						}
						
						//SendJsonMessages.SendAllJsonMessage(damsg, "", world);
						if(debug){logDebug(" DS SendAllJsonMessage. ...");}
						if(!isDSCanceled){
							//final World world = worlds.get(0);
							int dayskipdelay = getConfig().getInt("dayskipdelay", 10);
							if(debug){logDebug(" DS !isDSCanceled. ...");}
							dayskipTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			
								public void run() {
									int timeoffset = 10000;
									long Relative_Time = (24000 - world.getTime()) - timeoffset;
									world.setFullTime(world.getFullTime() + Relative_Time);
									if(debug){logDebug("" + get("sps.message.dayskipsettime") + "...");}
								}
								
							}, dayskipdelay * 20);
							
						}else{
							
							isDSCanceled = false;
						}
				}
			}
		}
		
		if (command.getName().equalsIgnoreCase("clearrain")){ // TODO: Command ClearRain
			if(config.getBoolean("clearrain_enabled", false)){
				if(sender instanceof Player){
					Player player = (Player) sender;
					World world = player.getWorld();
					if (!IsNight(player.getWorld())&&player.getWorld().hasStorm()) {
						world.setStorm(false);
						player.sendMessage("Rain stopped.");
					}else{
						sender.sendMessage("Must not be Night, and a rainstorm must be present");
					}
				}else{
					sender.sendMessage("Must be a player to use this command.");
				}
			}else{
				sender.sendMessage("clearrain is not enabled.");
			}
		}

		return true;
	}
	
	public  void log(String dalog){// TODO: Log
		SinglePlayerSleep.logger.info(Ansi.YELLOW + pluginName + " v" + THIS_VERSION + Ansi.RESET + " " + dalog);
	}
	public  void logDebug(String dalog){
		log(Ansi.RED + " [DEBUG] " + Ansi.RESET + dalog);
	}
	public void logWarn(String dalog){
		SinglePlayerSleep.logger.warning(dalog);
	}
	public void broadcast(String message, World world){
		String damsg = "{\"text\":\"broadcastString\"}";
		String msgcolor1 = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
		damsg = damsg.replace("broadcastString", message);
		sendJson(world, damsg, "");
		//SendJsonMessages.SendAllJsonMessage(damsg, "", world);
		
		//getServer().broadcastMessage("" + message);
	}
		
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) // TODO: OnPlayerJoin
		{
		Player player = event.getPlayer();
		//if(p.isOp() && UpdateCheck||p.hasPermission("sps.showUpdateAvailable")){	
		/** Notify Ops */
		if(UpdateAvailable&&(player.isOp()||player.hasPermission("sps.showUpdateAvailable"))){
			String links = "[\"\",{\"text\":\"<Download>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/history\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\" \",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\"| \"},{\"text\":\"<Donate>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://ko-fi.com/joelgodofwar\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Donate_msg>\"}},{\"text\":\" | \"},{\"text\":\"<Notes>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/updates\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Notes_msg>\"}}]";
			links = links.replace("<DownloadLink>", DownloadLink).replace("<Download>", get("sps.version.download"))
					.replace("<Donate>", get("sps.version.donate")).replace("<please_update>", get("sps.version.please_update"))
					.replace("<Donate_msg>", get("sps.version.donate.message")).replace("<Notes>", get("sps.version.notes"))
					.replace("<Notes_msg>", get("sps.version.notes.message"));
			String versions = "" + ChatColor.GRAY + get("sps.version.new_vers") + ": " + ChatColor.GREEN + "{nVers} | " + get("sps.version.old_vers") + ": " + ChatColor.RED + "{oVers}";
			player.sendMessage("" + ChatColor.GRAY + get("sps.version.message").toString().replace("<MyPlugin>", ChatColor.GOLD + THIS_NAME + ChatColor.GRAY) );
			Utils.sendJson(player, links);
			player.sendMessage(versions.replace("{nVers}", UCnewVers).replace("{oVers}", UColdVers));
			//p.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " v" + UColdVers + ChatColor.RESET + " " + get("newvers") + ChatColor.GREEN + " v" + UCnewVers + ChatColor.RESET + "\n" + ChatColor.GREEN + UpdateChecker.getResourceUrl() + ChatColor.RESET);
		}
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd");
		LocalDate localDate = LocalDate.now();
		String daDay = dtf.format(localDate);

		if (daDay.equals("04/16")) {
		    String playerId = player.getUniqueId().toString();
		    if (!triggeredPlayers.contains(playerId)) {
		        if (isPluginRequired(THIS_NAME)) {
		            player.sendTitle("Happy Birthday Mom", "I miss you - 4/16/1954-12/23/2022", 10, 70, 20);
		        }
		        triggeredPlayers.add(playerId);
		    }
		}
		
		if(player.getName().equals("JoelYahwehOfWar")||player.getName().equals("JoelGodOfWar")){
			player.sendMessage(THIS_NAME + " " + THIS_VERSION + " Hello father!");
			//p.sendMessage("seed=" + p.getWorld().getSeed());
		}
	}

	
	public static boolean IsNight(World w){
		long time = (w.getFullTime()) % 24000;
		return time >= mobSpawningStartTime && time < mobSpawningStopTime;
	}
	
	public static boolean IsDay(World w){
		long time = (w.getFullTime()) % 24000;
		return time > 0 && time < 12300;
		//return time >= mobSpawningStartTime && time < mobSpawningStopTime;
	}
	
	public int RandomNumber(int maxnum){
		Random rand = new Random();
		int min = 1;
		int max = maxnum;
		// nextInt as provided by Random is exclusive of the top value so you need to add 1 
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}
	
	public boolean fileContains(String filePath, String searchQuery) throws IOException{
		searchQuery = searchQuery.trim();
		BufferedReader br = null;

		try{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
			String line;
			while ((line = br.readLine()) != null){
				if (line.contains(searchQuery)){
					//log("findstring found");
					return true;
				}else{
				}
			}
		}
		finally{
			try{
				if (br != null)
					br.close();
			}
			catch (Exception e){
				System.err.println("Exception while closing bufferedreader " + e.toString());
			}
		}
		//log("findstring failed");
		return false;
	}
	public String addChar(String str, String ch, int position) {
		StringBuilder sb = new StringBuilder(str);
		sb.insert(position, ch);
		return sb.toString();
	}
	public int myPlugins(){
		//Plugin[] daPlugins = getServer().getPluginManager().getPlugins();
		int dacount = 1;
		if(getServer().getPluginManager().getPlugin("DragonDropElytra") != null){dacount++;}
		if(getServer().getPluginManager().getPlugin("NoEndermanGrief") != null){dacount++;}
		if(getServer().getPluginManager().getPlugin("PortalHelper") != null){dacount++;}
		return dacount;
	}
	
	public static void copyFile_Java7(String origin, String destination) throws IOException {
		Path FROM = Paths.get(origin);
		Path TO = Paths.get(destination);
		//overwrite the destination file if it exists, and copy
		// the file attributes, including the rwx permissions
		CopyOption[] options = new CopyOption[]{
			StandardCopyOption.REPLACE_EXISTING,
			StandardCopyOption.COPY_ATTRIBUTES
		}; 
		Files.copy(FROM, TO, options);
	}
	
	public boolean isBloodmoonInprogress(World world){
		if(getServer().getPluginManager().getPlugin("BloodMoon") != null){
			BloodmoonActuator getactuator = BloodmoonActuator.GetActuator(world);
			if(getactuator != null){
				return getactuator.isInProgress();
			}else{return false;}
		}
		if(getServer().getPluginManager().getPlugin("bloodmoon-advanced") != null){
			return BloodmoonAPI.bloodmoonIsRunning(world);
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public  String getNickname(Player player){
		String playerName = null;
		if(getConfig().getBoolean("nickname.usedisplayname", false)) {
			playerName = ChatColorUtils.setColorsByCode(player.getDisplayName());
		}else if(!getConfig().getBoolean("nickname.usedisplayname", false)) {
			playerName = player.getName();
		}
		if(debug){logDebug("player.getDisplayName()=" + player.getDisplayName());}
		if(debug){logDebug("player.getName()=" + player.getName());}
		if(debug){logDebug("nickname.usedisplayname=" + getConfig().getBoolean("nickname.usedisplayname"));}
		if(getServer().getPluginManager().getPlugin("VentureChat") != null){
			MineverseChatPlayer mcp = MineverseChatAPI.getMineverseChatPlayer(player);
			String nick = mcp.getNickname();
			if(nick != null){
				if(debug){logDebug("mcp.getNickname()=" + mcp.getNickname());}
				if(debug){logDebug("ChatColor.translateAlternateColorCodes('&', nick)=" + ChatColor.translateAlternateColorCodes('&', nick));}
				//ChatColor.translateAlternateColorCodes('&', nick);
				//nick = nick.replaceAll("§", "&");
				nick = ChatColorUtils.setColorsByCode(nick);
				if(debug){logDebug("VentureChat Format.color(nick)=" + nick);}
				return nick;
			}
			if(debug){logDebug("VentureChat Nick=null using " + playerName);}
			return Format.color(playerName);
		}else if(getServer().getPluginManager().getPlugin("Essentials") != null){
			Essentials ess = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
			//User user = ess.getUserMap().getUser(player.getName());
			//if(debug){logDebug("Essnetials Nick=" + ess.getUserMap().getUser(player.getName()).getNickname());}
			String nick = ess.getUserMap().getUser(player.getName()).getNickname();
			if(nick != null){
				if(debug){logDebug("Essentials Nick=" + nick);}
				return ChatColor.translateAlternateColorCodes('&', nick);
			}
			if(debug){logDebug("Essentials Nick=null using: " + playerName );}
			return ChatColorUtils.setColorsByCode(playerName);
		}else if(getServer().getPluginManager().getPlugin("HexNicks") != null){
			String nick = GsonComponentSerializer.gson().serialize(Nicks.api().getNick(player));
			if(nick != null){
				if(debug){logDebug("HexNick Nick=" + nick);}
				if(nick.contains("["))	nick = nick.substring(nick.indexOf("[") + 1);
				if(nick.contains("]"))nick = nick.substring(0, nick.indexOf("]"));
				return "\"}," + ChatColor.translateAlternateColorCodes('&', nick) + ",{\"text\": \"";
			}
			if(debug){logDebug("HexNick Nick=null using " + playerName);}
			return ChatColorUtils.setColorsByCode(playerName);
		}else{
			if(debug){logDebug("No nickname found using=" + playerName);}
			return playerName;
		}
	}
	
	public String getNickname(CommandSender sender){
		if(sender instanceof Player){
			return getNickname((Player)sender);
		}else{
			return "Console";
		}
	}
	
	public void sendJson(String string, String OldString){
		String string2;
		String string3 = OldString;
		for (Player player: Bukkit.getOnlinePlayers()){ // for (Player player: Bukkit.getOnlinePlayers()){
			if(player.hasPermission("sps.cancel")&&displaycancel){
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw \"" + player.getName() + 
				        "\" " + string);
				//player.spigot().sendMessage(string);
				if(debug){logDebug("SAJM - string=" + string);}
				if(debug){logDebug("SAJM - perm & display - Broadcast");}
			}else{
				if(debug){logDebug("SAJM - string3.toString()=" + string3.toString());}
				string2 = string.toString();
				if(debug){logDebug("SAJM - string2=" + string2);}
				string2 = string2.substring(0, string2.lastIndexOf("[")) + "\"}]";
				if(debug){logDebug("SAJM - string2=" + string2);}
				
				String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
				String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
				string2 = string2.replace(cancelcolor + get("sps.message.cancel") + msgcolor, "");
				if(debug){logDebug("SAJM - string2=" + string2);}
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw \"" + player.getName() + 
				        "\" " + string2); /** Quatation marks around name for non alphanumeric player names. (Added by ImDaBigBoss) */
				if(debug){logDebug("SAJM - !perm & display - Broadcast");}
				//player.sendRawMessage(string2);
				//player.spigot().sendMessage(ComponentSerializer.parse(string2));
			}
		}
	}
	
	public void sendJson(World world, String string, String OldString){
		String string2;
		String string3 = OldString;
		for (Player player: world.getPlayers()){ // for (Player player: Bukkit.getOnlinePlayers()){
			if(player.hasPermission("sps.cancel")&&displaycancel){
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw \"" + player.getName() + 
				        "\" " + string);
				//player.spigot().sendMessage(string);
				if(debug){logDebug("SAJM - string=" + string);}
				if(debug){logDebug("SAJM - perm & display - Broadcast");}
			}else{
				if(debug){logDebug("SAJM - string3.toString()=" + string3.toString());}
				string2 = string.toString();
				if(debug){logDebug("SAJM - string2=" + string2);}
				string2 = string2.substring(0, string2.lastIndexOf("[")) + "\"}]";
				if(debug){logDebug("SAJM - string2=" + string2);}
				
				String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
				String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
				string2 = string2.replace(cancelcolor + get("sps.message.cancel") + msgcolor, "");
				if(debug){logDebug("SAJM - string2=" + string2);}
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw \"" + player.getName() + 
				        "\" " + string2); /** Quatation marks around name for non alphanumeric player names. (Added by ImDaBigBoss) */
				Bukkit.getServer().broadcastMessage(string3);
				if(debug){logDebug("SAJM - !perm & display - Broadcast");}
				//player.sendRawMessage(string2);
				//player.spigot().sendMessage(ComponentSerializer.parse(string2));
			}
		}
	}
	
	public void resetPlayersRestStat(World world) {
		if(getConfig().getBoolean("reset_insomnia", false)) {
			List<Player> players = world.getPlayers();
			for(Player player: players) {
				if(player.getStatistic(Statistic.TIME_SINCE_REST) > 0) {
					player.setStatistic(Statistic.TIME_SINCE_REST, 0);
				}
			}
		}
	}
	
	public static String getVersion() {
		String strVersion = Bukkit.getVersion();
		strVersion = strVersion.substring(strVersion.indexOf("MC: "), strVersion.length());
		strVersion = strVersion.replace("MC: ", "").replace(")", "");
		return strVersion;
	}
	
	@EventHandler
	public void PlayerIsSleeping(PlayerBedLeaveEvent event) throws InterruptedException{ 
		Player player = event.getPlayer();
		if( getConfig().getBoolean("exitbedcancel", false) ) {
			Bukkit.dispatchCommand(player, "spscancel");
		}
	}
	
	public String LoadTime(long startTime) {
	    long elapsedTime = System.currentTimeMillis() - startTime;
	    long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
	    long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60;
	    long milliseconds = elapsedTime % 1000;
	    
	    if (minutes > 0) {
	        return String.format("%d min %d s %d ms.", minutes, seconds, milliseconds);
	    } else if (seconds > 0) {
	        return String.format("%d s %d ms.", seconds, milliseconds);
	    } else {
	        return String.format("%d ms.", elapsedTime);
	    }
	}
	
	@SuppressWarnings("static-access")
	public String get(String key, String... defaultValue) {
		return lang2.get(key, defaultValue);
	}
	
	public boolean isPluginRequired(String pluginName) {
	    String[] requiredPlugins = {"SinglePlayerSleep", "MoreMobHeads", "NoEndermanGrief", "ShulkerRespawner", "DragonDropElytra", "RotationalWrench", "SilenceMobs", "VillagerWorkstationHighlights"};
	    for (String requiredPlugin : requiredPlugins) {
	        if (getServer().getPluginManager().getPlugin(requiredPlugin) != null && getServer().getPluginManager().isPluginEnabled(requiredPlugin)) {
	            if (requiredPlugin.equals(pluginName)) {
	                return true;
	            } else {
	                return false;
	            }
	        }
	    }
	    return true;
	}
	
}
