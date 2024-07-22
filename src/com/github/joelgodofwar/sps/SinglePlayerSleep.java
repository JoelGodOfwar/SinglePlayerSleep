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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
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
import org.spectralmemories.bloodmoon.BloodmoonActuator;

import com.earth2me.essentials.Essentials;
import com.github.joelgodofwar.sps.api.ChatColorUtils;
import com.github.joelgodofwar.sps.api.Metrics;
import com.github.joelgodofwar.sps.api.YmlConfiguration;
import com.github.joelgodofwar.sps.commands.Cmd_dayskip;
import com.github.joelgodofwar.sps.commands.Cmd_sleep;
import com.github.joelgodofwar.sps.commands.Cmd_spscancel;
import com.github.joelgodofwar.sps.commands.Cmd_update;
import com.github.joelgodofwar.sps.common.PluginLibrary;
import com.github.joelgodofwar.sps.common.PluginLogger;
import com.github.joelgodofwar.sps.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.sps.common.error.Report;
import com.github.joelgodofwar.sps.i18n.Translator;
import com.github.joelgodofwar.sps.util.FormatUtil;
import com.github.joelgodofwar.sps.util.StrUtils;
import com.github.joelgodofwar.sps.util.Utils;
import com.github.joelgodofwar.sps.util.Version;
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
	// public final static Logger logger = Logger.getLogger("Minecraft");
	public static String THIS_NAME;
	static String THIS_VERSION;
	/** update checker variables */
	public int projectID = 68139; // https://spigotmc.org/resources/71236
	public String githubURL = "https://github.com/JoelGodOfwar/SinglePlayerSleep/raw/master/versioncheck/1.13/versions.xml";
	public boolean UpdateAvailable =  false;
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
	YmlConfiguration messages = new YmlConfiguration();
	public YamlConfiguration oldMessages;
	public FileConfiguration fileVersions  = new YamlConfiguration();
	public File fileVersionsFile;
	public File configFile;
	public File messagesFile;
	public Version minConfigVersion = new Version("1.0.9");
	public Version minMessagesVersion = new Version("1.0.1");
	public boolean isBloodMoon = false;
	public String jsonColorString = "\"},{\"text\":\"<text>\",\"color\":\"<color>\"},{\"text\":\"";
	public boolean is116 = true;
	public String blacklist_sleep;
	public String blacklist_dayskip;
	boolean colorful_console;
	//String configVersion = "1.0.7";
	String pluginName = THIS_NAME;
	public String jarfilename = this.getFile().getAbsoluteFile().toString();
	public static DetailedErrorReporter reporter;
	public PluginLogger LOGGER;


	@Override
	public void onLoad() {

		LOGGER = new PluginLogger(this);
		reporter = new DetailedErrorReporter(this);
		UpdateCheck = getConfig().getBoolean("auto_update_check", true);
		debug = getConfig().getBoolean("debug", false);
		daLang = getConfig().getString("lang", "en_US");
		displaycancel = getConfig().getBoolean("display_cancel", true);
		//log("displaycancel=" + displaycancel);
		config = new YmlConfiguration();
		oldconfig = new YamlConfiguration();
		messages = new YmlConfiguration();
		oldMessages = new YamlConfiguration();
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

	}

	@Override // TODO: onEnable
	public void onEnable(){
		long startTime = System.currentTimeMillis();
		LOGGER = new PluginLogger(this);
		reporter = new DetailedErrorReporter(this);
		UpdateCheck = getConfig().getBoolean("auto_update_check", true);
		debug = getConfig().getBoolean("debug", false);
		daLang = getConfig().getString("lang", "en_US");
		displaycancel = getConfig().getBoolean("display_cancel", true);
		//log("displaycancel=" + displaycancel);
		config = new YmlConfiguration();
		oldconfig = new YamlConfiguration();
		messages = new YmlConfiguration();
		oldMessages = new YamlConfiguration();
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

		LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
		LOGGER.log(ChatColor.GREEN + "v" + THIS_VERSION + ChatColor.RESET + " Loading...");
		LOGGER.log("Jar Filename: " + this.getFile().getName());//.getAbsoluteFile());
		LOGGER.log("Server Version: " + getServer().getVersion().toString());

		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			LOGGER.debug(ChatColor.RED + "Jar file contains -DEV, debug set to true" + ChatColor.RESET);
			//log("jarfile contains dev, debug set to true.");
		}

		// Make sure directory exists and files exist.
		checkDirectories();
		LOGGER.log("Loading file version checker...");
		fileVersionsFile = new File(getDataFolder() + "" + File.separatorChar + "fileVersions.yml");
		try {
			fileVersions.load(fileVersionsFile);
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_FILEVERSION).error(exception));
		}
		// Check if Config needs update.
		checkConfig();
		// Check if MEssages needs update.
		checkMessages();

		LOGGER.log("Loading config.yml...");
		configFile = new File(getDataFolder() + "" + File.separatorChar + "config.yml");
		try {
			config.load(configFile);
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
		}

		LOGGER.log("Loading messages.yml...");
		messagesFile = new File(getDataFolder(), "messages.yml");
		try {
			messages.load(messagesFile);
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_LOADING_MESSAGES_FILE).error(exception));
		}


		/** Update Checker */
		if(UpdateCheck){
			try {
				LOGGER.log("Checking for updates...");
				VersionChecker updater = new VersionChecker(this, projectID, githubURL);
				if(updater.checkForUpdates()) {
					/** Update available */
					UpdateAvailable = true; // TODO: Update Checker
					UColdVers = updater.oldVersion();
					UCnewVers = updater.newVersion();

					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					LOGGER.log("* " + get("sps.version.message").toString().replace("<MyPlugin>", THIS_NAME) );
					LOGGER.log("* " + get("sps.version.old_vers") + ChatColor.RED + UColdVers );
					LOGGER.log("* " + get("sps.version.new_vers") + ChatColor.GREEN + UCnewVers );
					LOGGER.log("*");
					LOGGER.log("* " + get("sps.version.please_update") );
					LOGGER.log("*");
					LOGGER.log("* " + get("sps.version.download") + ": " + DownloadLink + "/history");
					LOGGER.log("* " + get("sps.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
				}else{
					/** Up to date */
					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					LOGGER.log("* " + get("sps.version.curvers"));
					LOGGER.log("* " + get("sps.version.donate") + ": https://ko-fi.com/joelgodofwar");
					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					UpdateAvailable = false;
				}
			}catch(Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_UPDATE_PLUGIN).error(exception));
			}
		}else {
			/** auto_update_check is false so nag. */
			LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
			LOGGER.log( "* " + get("sps.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
			LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
		}
		/** end update checker */

		File  file = new File(getDataFolder(), "permissions.yml");
		LOGGER.log("" + file);
		if(!file.exists()){
			LOGGER.log("permissions.yml not found, creating! This is a sample only!");
			saveResource("permissions.yml", true);
		}
		getServer().getPluginManager().registerEvents(this, this);

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
		}catch (Exception exception){
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_METRICS_LOAD_ERROR).error(exception));
		}

	}

	@Override // TODO: onDisable
	public void onDisable() {
		consoleInfo(ChatColor.RED + "DISABLED");
	}

	public void consoleInfo(String state) {
		//LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
		LOGGER.log(ChatColor.YELLOW + " v" + THIS_VERSION + ChatColor.RESET + " is " + state  + ChatColor.RESET);
		//LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
	}

	public static void log(String message) {
		log(message);
	}

	public void log(String message, Object... args) {
		LOGGER.log(message, args);
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

	public void broadcast(String message, World world){
		String damsg = "{\"text\":\"broadcastString\"}";
		String msgcolor1 = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
		damsg = damsg.replace("broadcastString", message);
		sendJson(world, damsg, "");
		//SendJsonMessages.SendAllJsonMessage(damsg, "", world);

		//getServer().broadcastMessage("" + message);
	}

	/**
	 * @param event
	 * @throws InterruptedException
	 */
	@EventHandler
	public void PlayerIsSleeping(PlayerBedEnterEvent event) throws InterruptedException{
		try {
			LOGGER.debug(ChatColor.RED + "** Start PlayerBedEnterEvent **");
			List<World> worlds = Bukkit.getWorlds();
			//boolean debug = getConfig().getBoolean("debug");
			final Player player = event.getPlayer();
			LOGGER.debug("PIS player set. ...");
			final World world = player.getWorld();
			LOGGER.debug("PIS world set. ...");
			int sleepdelay = getConfig().getInt("sleepdelay", 10);
			int dayskipdelay = getConfig().getInt("dayskipdelay", 10);
			event.getBedEnterResult();

			if((getServer().getPluginManager().getPlugin("EssentialsX") != null)||(getServer().getPluginManager().getPlugin("Essentials") != null)){
				LOGGER.debug("PIS perm essentials.sleepingignored=" + player.hasPermission("essentials.sleepingignored"));
				if(player.hasPermission("essentials.sleepingignored") && !player.isOp()){
					player.sendMessage(ChatColor.RED + "WARNING! " + ChatColor.YELLOW + " you have the permission (" + ChatColor.GOLD +
							"essentials.sleepingignored" + ChatColor.YELLOW +
							") which is conflicting with SinglePlaySleep. Please ask for it to be removed. " + ChatColor.RED + "WARNING! ");
					LOGGER.warn("PIS Player " + player.getName() + "has the permission " + "essentials.sleepingignored" + " which is known to conflict with SinglePlayerSleep.");
					return;
				}
			}
			if(getConfig().getBoolean("enabledayskipper", false)){ // TODO: Dayskip
				/* Check if it's Day for DaySkipper */
				if(IsDay(player.getWorld())){
					LOGGER.debug("PIS DS it is Day");
					/* OK it's day check if it's a Black bed. */
					if(!player.hasPermission("sps.op")){ // TODO: Dayskip blacklist Check
						if((blacklist_dayskip != null)&&!blacklist_dayskip.isEmpty()){
							if(StrUtils.stringContains(blacklist_dayskip, world.getName().toString())){
								LOGGER.log("PIS DS - World - On blacklist.");
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
					LOGGER.debug("PIS DS got inventory");
					boolean itmDaySkipper = false;
					LOGGER.debug("PIS DS itemdayskipper initilized");
					if( getConfig().getBoolean("dayskipperitemrequired", true) ){
						for(ItemStack item:inv){
							if(!(item == null)){
								LOGGER.debug("PIS DS item=" + item.getType().name());
								if(item.getItemMeta().getDisplayName().equalsIgnoreCase("DaySkipper")){
									itmDaySkipper = true;
									LOGGER.debug("PIS DS found the item");
									break;
								}
							}
						}
						LOGGER.debug("PIS DS inventory iterator finished.");
					}else {
						LOGGER.debug("PIS DS Item not required");
						itmDaySkipper = true;
					}
					LOGGER.debug("PIS DS dayskipperitemrequired = " + confirmBoolean("dayskipperitemrequired"));
					//if(!getConfig().getBoolean("dayskipperitemrequired", true)){ itmDaySkipper = true; }
					if(itmDaySkipper){ //daMainHand.contentEquals("DaySkipper")||daOffHand.contentEquals("DaySkipper")||
						LOGGER.debug("PIS DS item DaySkipper is in inventory.");

						Block block = event.getBed();
						LOGGER.debug("PIS DS block.material = " + block.getType());
						LOGGER.debug("PIS DS isBed(block) = " + isBed(block));
						if ( isBed(block) ){
							// ((Bed)block.getBlockData()).getMaterial().equals(Material.BLACK_BED)
							LOGGER.debug("PIS DS the block is a bed.");
							/* OK they have the DaySkipper item, now check for the permission*/
							if(player.hasPermission("sps.dayskipper")||player.hasPermission("sps.op")||player.hasPermission("sps.*")){
								LOGGER.debug("PIS DS Has perm or is op. ...");
								String CancelBracketColor = ChatColorUtils.setColorsByName(getConfig().getString("cancelbracketcolor", "YELLOW"));
								/* OK they have the perm, now lets notify the server and schedule the runnable */
								String canmsg = CancelBracketColor + "[\"},{\"text\":\"dacancel\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}},{\"text\":\"" + CancelBracketColor + "]\"}";
								String damsg = "[\"\",{\"text\":\"sleepmsg " + canmsg + "]";
								String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
								LOGGER.debug("PIS DS ... msgcolor=" + msgcolor);
								//String sleepmsg = "" + get("sps.message.dayskipmsg","<player> wants to sleep the day away...");
								int maxmsgs = messages.getInt("messages.dayskip.count");
								int randomnumber = RandomNumber(maxmsgs);
								String sleepmsg = messages.getString("messages.dayskip.message_" + randomnumber, ChatColor.WHITE + "<player> wants to sleep the day away...");
								if(is116){
									sleepmsg = ChatColorUtils.setNametoRGB(sleepmsg);
									//sleepmsg = StrUtils.parseRGBNameColors(sleepmsg);
								}else{
									sleepmsg = StrUtils.stripRGBColors(sleepmsg);// strip RGBHEX
									sleepmsg = ChatColorUtils.setColors(sleepmsg);// SetColorsByName
								}
								sleepmsg = sleepmsg.replace("<colon>", ":");
								sleepmsg = FormatUtil.formatString(sleepmsg);

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
									nickName = FormatUtil.formatString(nickName);
								}
								/** end nickname parser */
								LOGGER.debug("PIS DS ... playercolor=" + playercolor);
								damsg = damsg.replace("<player>", /**playercolor +*/ nickName /**+ msgcolor*/);
								String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
								LOGGER.debug("PIS DS ... cancelcolor=" + cancelcolor);
								damsg = damsg.replace("dacancel", cancelcolor + get("sps.message.dayskipcancel") + msgcolor);
								//change cancel color based on config
								damsg = damsg.replace("tooltip", "" + get("sps.message.dayskipclickcancel"));
								LOGGER.debug("PIS DS string processed. ...");
								LOGGER.debug("PIS DS damsg=" + damsg);

								if(getConfig().getBoolean("broadcast_per_world", true)){
									sendJson(player.getWorld(), damsg, canmsg);
								}else{
									sendJson(damsg, canmsg);
								}
								LOGGER.debug("PIS DS SendAllJsonMessage. ...");
								//player.sendMessage("The item in your main hand is named: " + daName);
								if(!isDSCanceled){
									LOGGER.debug("PIS DS !isDSCanceled. ...");
									dayskipTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

										@Override public void run() {
											setDStime(player, world);
											LOGGER.debug("PIS DS setDStime has run. ...");
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
							LOGGER.debug("PIS DS block is not a Bed");
							player.sendMessage(ChatColor.YELLOW + "" + get("sps.message.dayskipblackbed"));/* NOT A BLACK BED */
							return;
						}
					}else {
						LOGGER.log("PIS DS it is Day, Item is required, Item is not in inventory.");
						LOGGER.debug(ChatColor.RED + "** End PlayerBedEnterEvent **");
						isCanceled =  false;
						return;
					}
				} else {
					LOGGER.debug("PIS DS isDay=false");
				}
			} else {
				LOGGER.debug("PIS DS enabledayskipper=false");
			}
			//if(debug){logDebug("PIS getBedEnterResult=" + event.getBedEnterResult().toString());}
			if(!isBloodmoonInprogress(player.getWorld())){//isBloodmoonInprogress//isBloodMoon
				if(event.getBedEnterResult() == BedEnterResult.OK){
					//Check it's night or if storm
					if (IsNight(player.getWorld())||player.getWorld().isThundering()) {
						if(!player.hasPermission("sps.op")){ // TODO: Sleep Blacklist Check
							if((blacklist_sleep != null)&&!blacklist_sleep.isEmpty()){
								if(StrUtils.stringContains(blacklist_sleep, world.getName().toString())){
									LOGGER.log("PIS IN - World - On blacklist.");
									return;
								}
							}
						}
						//Set default timer for when the player has never slept before
						long timer = 0;
						LOGGER.debug("PIS IN... " + player.getName() + " is sleeping.");
						long time = System.currentTimeMillis() / 1000;
						if(sleeplimit.get(player.getUniqueId()) == null){
							LOGGER.debug("PIS IN sleeplimit UUID=null");
							// Check if player has sps.unrestricted
							if (!player.hasPermission("sps.unrestricted")) {
								// Set player's time in HashMap
								sleeplimit.put(player.getUniqueId(), time);
								LOGGER.debug("PIS IN... " + player.getDisplayName() + " added to playersSlept");
							}
						}else{
							LOGGER.debug("PIS IN sleeplimit UUID !null");
							// Player is on the list.
							timer = sleeplimit.get(player.getUniqueId());
							LOGGER.debug("PIS IN time=" + time);
							LOGGER.debug("PIS IN timer=" + timer);
							LOGGER.debug("PIS IN time - timer=" +  (time - timer));
							LOGGER.debug("PIS IN sleeplimit=" + getConfig().getLong("sleeplimit", 60));
							// if !time - timer > limit
							if(!((time - timer) > getConfig().getLong("sleeplimit", 60))){
								long length = getConfig().getLong("sleeplimit", 60) - (time - timer) ;
								String sleeplimit = "" + get("sps.message.sleeplimit").toString().replace("<length>", "" + length);
								player.sendMessage(ChatColor.YELLOW + sleeplimit);
								LOGGER.debug("PIS IN... sleeplimit: " + sleeplimit);
								//player.sendMessage("You can not do that for " + length + " seconds");
								event.setCancelled(true);
								return;
							}else if((time - timer) > getConfig().getLong("sleeplimit", 60)){
								LOGGER.debug("PIS IN time - timer > sleeplimit");
								sleeplimit.replace(player.getUniqueId(), time);
							}
						}

						//Check if players can sleep without the ability for others to cancel it
						if (getConfig().getBoolean("unrestrictedsleep")) {
							LOGGER.debug("PIS IN unrestrictedsleep=true");
							String dastring = "" + get("sps.message.issleep");
							dastring = dastring.replace("<player>", getNickname(player));
							this.broadcast(dastring, world);
							transitionTaskUnrestricted = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
								@Override public void run() {
									setDatime(player, world);
									resetPlayersRestStat(world);
								}
							}, sleepdelay * 20);
						}else //Don't show cancel option if player has unrestricted sleep perm
							if (player.hasPermission("sps.unrestricted")) { //TODO: Unrestricted Broadcast
								//use random msgs, and colorization

								LOGGER.debug("PIS IN Has unrestricted perm. ...");

								//Broadcast to Server
								String dastring = "" + get("sps.message.issleep");

								dastring = dastring.replace("<player>", "");
								String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
								LOGGER.debug("PIS IN ... msgcolor=" + msgcolor);
								String CancelBracketColor = ChatColorUtils.setColorsByName(getConfig().getString("cancelbracketcolor", "YELLOW"));
								LOGGER.debug("PIS IN ... CancelBracketColor=" + CancelBracketColor);
								//String damsg = "[\"\",{\"text\":\"player\"},{\"text\":\" is sleeping [\"},{\"text\":\"dacancel\",\"color\":\"red\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"tooltip\"}]}}},{\"text\":\"]\",\"color\":\"none\",\"bold\":false}]";
								//String damsg = "[\"\",{\"text\":\"sleepmsg [\"},{\"text\":\"dacancel\",\"color\":\"red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}},{\"text\":\"]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}}]";
								String canmsg = "";//CancelBracketColor + "[\"},{\"text\":\"dacancel\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}},{\"text\":\"" + CancelBracketColor + "]\"";
								String damsg = "[\"\",{\"text\":\"sleepmsg " + canmsg + "\"}]";
								String sleepmsg;
								if (getConfig().getBoolean("randomsleepmsgs")){
									int maxmsgs = messages.getInt("messages.sleep.count");
									int randomnumber = RandomNumber(maxmsgs);
									sleepmsg = messages.getString("messages.sleep.message_" + randomnumber, ChatColor.WHITE + "<player> is sleeping");
									sleepmsg = sleepmsg.replace("<colon>", ":");
									sleepmsg = FormatUtil.formatString(sleepmsg);
									LOGGER.debug("PIS IN ... maxmsgs=" + maxmsgs);
									LOGGER.debug("PIS IN ... randomnumber=" + randomnumber);
								}else{
									sleepmsg = (ChatColor.WHITE + "<player> is sleeping");
									LOGGER.debug("PIS IN ... randomsleepmsgs=false");
								}
								if(is116){
									LOGGER.debug("PIS IN sleepmsg=" + sleepmsg);
									sleepmsg = ChatColorUtils.setNametoRGB(sleepmsg);
									LOGGER.debug("PIS IN sleepmsg=" + sleepmsg);
									/**sleepmsg = StrUtils.parseRGBNameColors(sleepmsg);
									LOGGER.debug("PIS IN sleepmsg=" + sleepmsg);//*/
								}else{
									LOGGER.debug("PIS IN sleepmsg=" + sleepmsg);
									sleepmsg = StrUtils.stripRGBColors(sleepmsg);// strip RGBHEX
									LOGGER.debug("PIS IN sleepmsg=" + sleepmsg);
									sleepmsg = ChatColorUtils.setColors(sleepmsg);// SetColorsByName
									LOGGER.debug("PIS IN sleepmsg=" + sleepmsg);
								}
								LOGGER.debug("PIS IN sleepmsg=" + sleepmsg);
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
									nickName = FormatUtil.formatString(nickName);
								}
								/** end nickname parser */

								//String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
								LOGGER.debug("PIS IN ... playercolor=" + playercolor);
								damsg = damsg.replace("<player>", /**playercolor +*/ nickName);
								//String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
								//LOGGER.debug("PIS IN ... cancelcolor=" + cancelcolor);}
								//damsg = damsg.replace("dacancel", cancelcolor + get("cancel") + msgcolor);
								//change cancel color based on config
								//damsg = damsg.replace("tooltip", "" + get("clickcancel"));
								LOGGER.debug("PIS IN string processed. ...");
								//String oldString = cancelcolor + get("cancel") + msgcolor;
								//damsg = damsg.replace(oldString, "").replace(" [\"", " \"").replace("]\"", "\"").replace(",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"Click to cancel sleep\"}", "");

								if(getConfig().getBoolean("broadcast_per_world", true)){
									sendJson(player.getWorld(), damsg, canmsg);
								}else{
									sendJson(damsg, canmsg);
								}

								//SendJsonMessages.SendAllJsonMessage(damsg, cancelcolor + get("cancel") + msgcolor, world);
								LOGGER.debug("PIS IN SendAllJsonMessage. ...");

								transitionTaskUnrestricted = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
									@Override public void run() {
										setDatime(player, world);
										resetPlayersRestStat(world);
									}
								}, sleepdelay * 20);

							} else if(!isCanceled&&!event.isCancelled()){ // TODO: Normal Sleep
								if(player.hasPermission("sps.hermits")||player.hasPermission("sps.op")){
									LOGGER.debug("PIS IN Has perm or is op. ...");

									//Broadcast to Server
									String dastring = "" + get("sps.message.issleep");

									dastring = dastring.replace("<player>", "");
									String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
									LOGGER.debug("PIS IN ... msgcolor=" + msgcolor);
									String CancelBracketColor = ChatColorUtils.setColorsByName(getConfig().getString("cancelbracketcolor", "YELLOW"));
									LOGGER.debug("PIS IN ... CancelBracketColor=" + CancelBracketColor);
									//String damsg = "[\"\",{\"text\":\"player\"},{\"text\":\" is sleeping [\"},{\"text\":\"dacancel\",\"color\":\"red\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"tooltip\"}]}}},{\"text\":\"]\",\"color\":\"none\",\"bold\":false}]";
									//String damsg = "[\"\",{\"text\":\"sleepmsg [\"},{\"text\":\"dacancel\",\"color\":\"red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}},{\"text\":\"]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}}]";
									String canmsg = CancelBracketColor + "[\"},{\"text\":\"dacancel\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}},{\"text\":\"" + CancelBracketColor + "]\"";
									String damsg = "[\"\",{\"text\":\"sleepmsg " + canmsg + "}]";
									String sleepmsg;
									if (getConfig().getBoolean("randomsleepmsgs")){
										int maxmsgs = getConfig().getInt("numberofsleepmsgs");
										int randomnumber = RandomNumber(maxmsgs);
										sleepmsg = config_getString("sleepmsg" + randomnumber, ChatColor.WHITE + "<player> is sleeping");
										sleepmsg = sleepmsg.replace("<colon>", ":");
										LOGGER.debug("PIS IN ... maxmsgs=" + maxmsgs);
										LOGGER.debug("PIS IN ... randomnumber=" + randomnumber);
									}else{
										sleepmsg = (ChatColor.WHITE + "<player> is sleeping");
										LOGGER.debug("PIS IN ... randomsleepmsgs=false");
									}
									if(is116){
										LOGGER.debug("PIS IN sleepmsg=" + sleepmsg);
										sleepmsg = ChatColorUtils.setNametoRGB(sleepmsg);
										LOGGER.debug("PIS IN name2RGB sleepmsg=" + sleepmsg);
										sleepmsg = FormatUtil.formatString(sleepmsg);
										LOGGER.debug("PIS IN parseRGB sleepmsg=" + sleepmsg);
									}else{
										LOGGER.debug("PIS IN sleepmsg=" + sleepmsg);
										sleepmsg = StrUtils.stripRGBColors(sleepmsg);// strip RGBHEX
										LOGGER.debug("PIS IN stripRGB sleepmsg=" + sleepmsg);
										sleepmsg = ChatColorUtils.setColors(sleepmsg);// SetColorsByName
										LOGGER.debug("PIS IN SC sleepmsg=" + sleepmsg);
									}
									LOGGER.debug("PIS IN sleepmsg=" + sleepmsg);
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
										LOGGER.debug("PIS IN nickName contain §");
										playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
									}else{
										nickName = FormatUtil.formatString(nickName);
										LOGGER.debug("PIS IN nick !contains §" );
										LOGGER.debug("PIS IN nickName AfterParse = " + nickName );
									}
									/** end nickname parser */

									//String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
									LOGGER.debug("PIS IN ... playercolor=" + playercolor);
									damsg = damsg.replace("<player>", /**playercolor +*/ nickName);
									String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
									LOGGER.debug("PIS IN ... cancelcolor=" + cancelcolor);
									damsg = damsg.replace("dacancel", cancelcolor + get("sps.message.cancel") + msgcolor);
									//change cancel color based on config
									damsg = damsg.replace("tooltip", "" + get("sps.message.clickcancel"));
									LOGGER.debug("PIS IN string processed. ...");
									//String oldString = cancelcolor + get("cancel") + msgcolor;
									//damsg = damsg.replace(oldString, "").replace(" [\"", " \"").replace("]\"", "\"").replace(",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"Click to cancel sleep\"}", "");

									if(getConfig().getBoolean("broadcast_per_world", true)){
										sendJson(player.getWorld(), damsg, canmsg);
									}else{
										sendJson(damsg, canmsg);
									}

									//SendJsonMessages.SendAllJsonMessage(damsg, cancelcolor + get("cancel") + msgcolor, world);
									LOGGER.debug("PIS IN SendAllJsonMessage. ...");

									//Thread.sleep(10000);
									if(!isCanceled&&!event.isCancelled()){
										LOGGER.debug("PIS IN !isCanceled. ...");
										transitionTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

											@Override public void run() {
												setDatime(player, world);
												resetPlayersRestStat(world);
												LOGGER.debug("PIS IN setDatime has run. ...");
											}

										}, sleepdelay * 20);

									}else{
										if(isCanceled){
											LOGGER.debug("PIS IN isCanceled=" + isCanceled);
										}
										if(event.isCancelled()){
											LOGGER.debug("PIS IN event.isCanceled=" + event.isCancelled());
										}
										isCanceled = false;
									}
									//player.sendMessage(ChatColor.RED + "isCanceled=" + isCanceled);
								}else{ //Player doesn't have permission so tell them
									player.sendMessage(ChatColor.YELLOW + "" + get("sps.message.noperm"));
								}
							}else{
								isCanceled = false;
								if(isCanceled){LOGGER.debug("PIS IN isCanceled=" + isCanceled);}
								if(event.isCancelled()){LOGGER.debug("PIS event.isCanceled=" + event.isCancelled());}
							}
					}else{ //It is not Night or Storming so tell the player
						if(getConfig().getBoolean("notifymustbenight")){
							player.sendMessage(ChatColorUtils.setColors("" + get("sps.message.nightorstorm")));
							LOGGER.debug("PIS IN it was not night and player was notified. ...");
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
										LOGGER.debug("PIS IN bedspawn=null");
									}else if(!isSameBed){
										LOGGER.debug("PIS IN bedspawn!=bed");
									}
									player.setBedSpawnLocation(event.getBed().getLocation());
									player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + get("sps.message.respawnpointmsg").toString().replace("<x>", "" + bed.getX()).replace("<z>", "" + bed.getZ()));
									LOGGER.debug("PIS IN bedspawn was set for player " + ChatColor.GREEN + player.getDisplayName() + ChatColor.RESET + " ...");
								}
							}else{
								player.setBedSpawnLocation(event.getBed().getLocation());
								player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + get("sps.message.respawnpointmsg").toString().replace("<x>", "" + bed.getX()).replace("<z>", "" + bed.getZ()));
								LOGGER.debug("PIS IN bedspawn was set for player " + ChatColor.GREEN + player.getDisplayName() + ChatColor.RESET + " ...");
							}
						} else {LOGGER.debug("PIS IN Server is 1.15+");
						}
					}
				}
			}else{
				player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + get("sps.message.bloodmoon", "You can not sleep during a bloodmoon.").toString());
				event.setCancelled(true);
			}
			LOGGER.debug(ChatColor.RED + "** End PlayerBedEnterEvent **");
			isCanceled =  false;
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_ENTER_BED_EVENT).error(exception));
		}
	}

	public String config_getString(String path, String string) {
		String getIt = getConfig().getString(path, "error");
		if (getIt == "error") {
			LOGGER.debug("Could not get '" + path + "', returned default.");
			return string;
		}
		return getIt;
	}

	public boolean checkradius(Location player, Location event, int radius){
		double distance = player.distance(event);
		if(distance <= radius) {
			LOGGER.debug("truedistance=" + distance);
			return true;
			//shulker.teleport(block.getLocation());
		}
		LOGGER.debug("falsedistance=" + distance);
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
				LOGGER.debug("sDT " + get("sps.message.setdownfall") + "...");
			} else {LOGGER.debug("sDT " + getNickname(player) + " Does not have permission sps.downfall ...");}
		}
		if(world.isThundering()){
			if(player.hasPermission("sps.thunder")){
				world.setThundering(false);
				LOGGER.debug("sDT" + get("sps.message.setthunder") + "...");
			} else {LOGGER.debug("sDT" + getNickname(player) + " Does not have permission sps.thunder ...");}
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
		LOGGER.debug("" + get("sps.message.settime") + "...");
	}

	public void setDStime(Player player, World world){
		int timeoffset = 10000;
		long Relative_Time = (24000 - world.getTime()) - timeoffset;
		world.setFullTime(world.getFullTime() + Relative_Time);
		LOGGER.debug("sDSt " + get("sps.message.dayskipsettime") + "...");
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
		try{
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
				}//*/

				if(args[0].equalsIgnoreCase("UPDATE")){ // TODO: Command update
					return new Cmd_update(this).execute(sender, args);
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
							}catch(Exception exception) {
								sender.sendMessage("Error Player Not found");
								reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_PLAYER_NOT_FOUND).error(exception));
								return false;
							}
						}else {
							sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.noperm"));
							return false;
						}
					}
				}//*/

				if(args[0].equalsIgnoreCase("toggledebug")||args[0].equalsIgnoreCase("td")){
					if(sender.isOp()||sender.hasPermission("sps.op")||!(sender instanceof Player)){
						debug = !debug;
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.debugtrue").toString().replace("<boolean>", get("sps.message.boolean." + debug) ));
						return true;
					}else if(!sender.hasPermission("sps.op")){
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.noperm"));
						return false;
					}
				}//*/

				if(args[0].equalsIgnoreCase("reload")){ // TODO: Command Reload
					if(sender.isOp()||sender.hasPermission("sps.op")||!(sender instanceof Player)||sender.hasPermission("sps.*")){
						//ConfigAPI.Reloadconfig(this, p);
						config = new YmlConfiguration();
						try {
							config.load(new File(getDataFolder(), "config.yml"));
						} catch (Exception exception) {
							reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
						}
						//this.reloadConfig();
						SinglePlayerSleep plugin = this;
						//getServer().getPluginManager().disablePlugin(plugin);
						//getServer().getPluginManager().enablePlugin(plugin);//
						reloadConfig();
						blacklist_sleep = config.getString("blacklist.sleep", "");
						blacklist_dayskip = config.getString("blacklist.dayskip", "");
						colorful_console = getConfig().getBoolean("colorful_console", true);
						/**lang = new YamlConfiguration();
						try {
							lang.load(langFile);a
						} catch (Exception exception) {
							reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
						}//*/
						lang2 = new Translator(daLang, getDataFolder().toString());
						LOGGER = new PluginLogger(this);
						reporter = new DetailedErrorReporter(this);
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.reloaded"));
					}else if(!sender.hasPermission("sps.op")){
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.noperm"));
					}
				}//*/



				return true;
			}
			if(command.getName().equalsIgnoreCase("spscancel")){ //command.getName().equalsIgnoreCase("cancel") // TODO: Command spscancel
				return new Cmd_spscancel(this).execute(sender, args);
			}

			if(command.getName().equalsIgnoreCase("sleep")){ // TODO: Command Sleep
				return new Cmd_sleep(this).execute(sender, args);
			}

			if(command.getName().equalsIgnoreCase("dayskip")){ // TODO: Command DaySkip
				return new Cmd_dayskip(this).execute(sender, args);
			}
			if(command.getName().equalsIgnoreCase("spsbloodmoon")){
				if (sender instanceof ConsoleCommandSender) {
					isBloodMoon = !isBloodMoon;
					LOGGER.debug("isBloodMoon=" + isBloodMoon);
					return true;
				}else{
					sender.sendMessage("Console only command.");
					return false;
				}
			}//*/

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
			}//*/
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.UNHANDLED_COMMAND_ERROR).error(exception));
		}
		LOGGER.debug("" + command + args + " returned default.");
		return true;
	}


	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) { // TODO: OnPlayerJoin
		Player player = event.getPlayer();
		//if(p.isOp() && UpdateCheck||p.hasPermission("sps.showUpdateAvailable")){
		// Notify Ops
		try {
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
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_UPDATE_PLUGIN).error(exception));
		}
		if(player.getName().equals("JoelYahwehOfWar")||player.getName().equals("JoelGodOfWar")){
			player.sendMessage(THIS_NAME + " " + THIS_VERSION + " §x§1§1§F§F§A§AHello §x§A§A§F§F§1§1father!");
		}
	}


	public boolean IsNight(World w){
		long time = (w.getFullTime()) % 24000;
		return (time >= mobSpawningStartTime) && (time < mobSpawningStopTime);
	}

	public boolean IsDay(World w){
		long time = (w.getFullTime()) % 24000;
		return (time > 0) && (time < 12300);
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
				if (br != null) {
					br.close();
				}
			}
			catch (Exception exception){
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_SEARCHING_FILE).error(exception));
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

	public static void copyFile(String origin, String destination) throws IOException {
		try {
			Path FROM = Paths.get(origin);
			Path TO = Paths.get(destination);
			// Ensure the destination directory exists
			if (TO.getParent() != null) {
				Files.createDirectories(TO.getParent());
			}
			//overwrite the destination file if it exists, and copy
			// the file attributes, including the rwx permissions
			CopyOption[] options = new CopyOption[]{
					StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.COPY_ATTRIBUTES
			};
			Files.copy(FROM, TO, options);
		} catch (Exception exception) {
			reporter.reportDetailed(getInstance(), Report.newBuilder(PluginLibrary.REPORT_CANNOT_COPY_FILE).error(exception));
		}
	}

	public boolean isBloodmoonInprogress(World world){
		try {
			if(getServer().getPluginManager().getPlugin("BloodMoon") != null){
				BloodmoonActuator getactuator = BloodmoonActuator.GetActuator(world);
				if(getactuator != null){
					return getactuator.isInProgress();
				}else{return false;}
			}
			if(getServer().getPluginManager().getPlugin("bloodmoon-advanced") != null){
				return BloodmoonAPI.bloodmoonIsRunning(world);
			}
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_CHECKING_BLOODMOON).error(exception));
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	/**
	 * Retrieves the nickname of the specified player.
	 * 
	 * @param player The player whose nickname is to be retrieved.
	 * @return The nickname of the player, or the player's name if no nickname is found.
	 */
	public  String getNickname(Player player){
		String playerName = null;
		if(getConfig().getBoolean("nickname.usedisplayname", false)) {
			playerName = ChatColorUtils.setColorsByCode(player.getDisplayName());
		}else if(!getConfig().getBoolean("nickname.usedisplayname", false)) {
			playerName = player.getName();
		}
		LOGGER.debug("player.getDisplayName()=" + player.getDisplayName());
		LOGGER.debug("player.getName()=" + player.getName());
		LOGGER.debug("nickname.usedisplayname=" + getConfig().getBoolean("nickname.usedisplayname"));
		try {
			if(getServer().getPluginManager().getPlugin("VentureChat") != null){
				MineverseChatPlayer mcp = MineverseChatAPI.getMineverseChatPlayer(player);
				String nick = mcp.getNickname();
				if(nick != null){
					LOGGER.debug("mcp.getNickname()=" + mcp.getNickname());
					LOGGER.debug("ChatColor.translateAlternateColorCodes('&', nick)=" + ChatColor.translateAlternateColorCodes('&', nick));
					//ChatColor.translateAlternateColorCodes('&', nick);
					//nick = nick.replaceAll("§", "&");
					nick = ChatColorUtils.setColorsByCode(nick);
					LOGGER.debug("VentureChat Format.color(nick)=" + nick);
					return nick;
				}
				LOGGER.debug("VentureChat Nick=null using " + playerName);
				return ChatColorUtils.setColorsByCode(playerName);
			}else if(getServer().getPluginManager().getPlugin("Essentials") != null){
				Essentials ess = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
				//User user = ess.getUserMap().getUser(player.getName());
				//LOGGER.debug("Essnetials Nick=" + ess.getUserMap().getUser(player.getName()).getNickname());}
				String nick = ess.getUserMap().getUser(player.getName()).getNickname();
				if(nick != null){
					LOGGER.debug("Essentials Nick=" + nick);
					return ChatColor.translateAlternateColorCodes('&', nick);
				}
				LOGGER.debug("Essentials Nick=null using: " + playerName );
				return ChatColorUtils.setColorsByCode(playerName);
			}else if(getServer().getPluginManager().getPlugin("HexNicks") != null){
				String nick = GsonComponentSerializer.gson().serialize(Nicks.api().getNick(player));
				if(nick != null){
					LOGGER.debug("HexNick Nick=" + nick);
					if(nick.contains("[")) {
						nick = nick.substring(nick.indexOf("[") + 1);
					}
					if(nick.contains("]")) {
						nick = nick.substring(0, nick.indexOf("]"));
					}
					return "\"}," + ChatColor.translateAlternateColorCodes('&', nick) + ",{\"text\": \"";
				}
				LOGGER.debug("HexNick Nick=null using " + playerName);
				return ChatColorUtils.setColorsByCode(playerName);
			}else{
				LOGGER.debug("No nickname found using=" + playerName);
				return playerName;
			}
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_PARSING_NICKNAME).error(exception));
		}
		return playerName;

	}

	public String getNickname(CommandSender sender){
		if(sender instanceof Player){
			return getNickname((Player)sender);
		}else{
			return "Console";
		}
	}

	public void sendPermJson(String string, String perm){
		for (Player player: Bukkit.getOnlinePlayers()){
			if(player.hasPermission(perm)) {
				sendJsonString(player, string);
			}
		}
	}
	public void sendPermJson(World world, String string, String perm){
		for (Player player: world.getPlayers()){
			if(player.hasPermission(perm)) {
				sendJsonString(player, string);
			}
		}
	}
	public void sendJsonString(Player player, String string) {
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw \"" + player.getName() + "\" " + string);
	}
	public void sendJson(String string, String OldString){
		String string2;
		String string3 = OldString;
		for (Player player: Bukkit.getOnlinePlayers()){
			if(player.hasPermission("sps.cancel")&&displaycancel){
				sendJsonString(player, string);
				//player.spigot().sendMessage(string);
				LOGGER.debug("SAJM - string=" + string);
				LOGGER.debug("SAJM - perm & display - Broadcast");
			}else{
				LOGGER.debug("SAJM - string3.toString()=" + string3.toString());
				string2 = string.toString();
				LOGGER.debug("SAJM - string2=" + string2);
				string2 = string2.substring(0, string2.lastIndexOf("[")) + "\"}]";
				LOGGER.debug("SAJM - string2=" + string2);

				String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
				String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
				string2 = string2.replace(cancelcolor + get("sps.message.cancel") + msgcolor, "");
				LOGGER.debug("SAJM - string2=" + string2);
				sendJsonString(player, string2);
				/** Quatation marks around name for non alphanumeric player names. (Added by ImDaBigBoss) */
				LOGGER.debug("SAJM - !perm & display - Broadcast");
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
				sendJsonString(player, string);
				//player.spigot().sendMessage(string);
				LOGGER.debug("WS SAJM - string=" + string);
				LOGGER.debug("WS SAJM - perm & display - Broadcast");
			}else{
				LOGGER.debug("WS SAJM - string3.toString()=" + string3.toString());
				string2 = string.toString();
				LOGGER.debug("WS SAJM - string2=" + string2);
				string2 = string2.substring(0, string2.lastIndexOf("[")) + "\"}]";
				LOGGER.debug("WS SAJM - string2=" + string2);

				String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
				String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
				string2 = string2.replace(cancelcolor + get("sps.message.cancel") + msgcolor, "");
				LOGGER.debug("WS SAJM - string2=" + string2);
				sendJsonString(player, string2);
				/** Quatation marks around name for non alphanumeric player names. (Added by ImDaBigBoss) */
				//Bukkit.getServer().broadcastMessage(string3);
				LOGGER.debug("WS SAJM - !perm & display - Broadcast");
				//player.sendRawMessage(string2);
				//player.spigot().sendMessage(ComponentSerializer.parse(string2));
			}
		}
	}

	public void resetPlayersRestStat(World world) {
		try {
			if(getConfig().getBoolean("reset_insomnia", false)) {
				List<Player> players = world.getPlayers();
				for(Player player: players) {
					if(player.getStatistic(Statistic.TIME_SINCE_REST) > 0) {
						player.setStatistic(Statistic.TIME_SINCE_REST, 0);
					}
				}
			}
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_RESET_INSOMNIA).error(exception));
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
		try {
			Player player = event.getPlayer();
			if( getConfig().getBoolean("exitbedcancel", false) ) {
				Bukkit.dispatchCommand(player, "spscancel");
			}
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_LEAVE_BED_EVENT).error(exception));
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

	public boolean isDSrunning() {
		return Bukkit.getScheduler().isCurrentlyRunning(dayskipTask);
	}

	public boolean isDSqueued() {
		return Bukkit.getScheduler().isQueued(dayskipTask);
	}

	public boolean isSleepRunning() {
		return Bukkit.getScheduler().isCurrentlyRunning(transitionTask);
	}

	public boolean isSleepQueued() {
		return Bukkit.getScheduler().isQueued(transitionTask);
	}

	/**public boolean isPluginRequired(String pluginName) {
		String[] requiredPlugins = {"SinglePlayerSleep", "MoreMobHeads", "NoEndermanGrief", "ShulkerRespawner", "DragonDropElytra", "RotationalWrench", "SilenceMobs", "VillagerWorkstationHighlights"};
		for (String requiredPlugin : requiredPlugins) {
			if ((getServer().getPluginManager().getPlugin(requiredPlugin) != null) && getServer().getPluginManager().isPluginEnabled(requiredPlugin)) {
				if (requiredPlugin.equals(pluginName)) {
					return true;
				} else {
					return false;
				}
			}
		}
		return true;
	}//*/

	// Used to check Minecraft version
	private Version verifyMinecraftVersion() {
		Version minimum = new Version(PluginLibrary.MINIMUM_MINECRAFT_VERSION);
		Version maximum = new Version(PluginLibrary.MAXIMUM_MINECRAFT_VERSION);
		try {
			Version current = new Version(this.getServer());

			// We'll just warn the user for now
			if (current.compareTo(minimum) < 0) {
				LOGGER.warn("Version " + current + " is lower than the minimum " + minimum);
			}
			if (current.compareTo(maximum) > 0) {
				LOGGER.warn("Version " + current + " has not yet been tested! Proceed with caution.");
			}

			return current;
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_PARSE_MINECRAFT_VERSION).error(exception).messageParam(maximum));
			// Unknown version - just assume it is the latest
			return maximum;
		}
	}

	public String getjarfilename() {
		return jarfilename;
	}

	public boolean getDebug() {
		return debug;
	}

	public static SinglePlayerSleep getInstance() {
		return getPlugin(SinglePlayerSleep.class);
	}

	public String confirmBoolean(String path) {
		String string = getConfig().getString(path, "error");
		if(string.equalsIgnoreCase("error")) {
			LOGGER.warn("Error reading config value from " + path);
			return string;
		}
		string = string.trim();
		string = string.replace("\'", "");
		return string;
	}

	public boolean isBed(Block block) {
		Material mat = block.getType();
		switch(mat) {
		case BLACK_BED:
		case BLUE_BED:
		case BROWN_BED:
		case CYAN_BED:
		case GRAY_BED:
		case GREEN_BED:
		case LIME_BED:
		case LIGHT_BLUE_BED:
		case LIGHT_GRAY_BED:
		case MAGENTA_BED:
		case ORANGE_BED:
		case PINK_BED:
		case PURPLE_BED:
		case RED_BED:
		case WHITE_BED:
		case YELLOW_BED:
			return true;
		default:
			return false;
		}
	}

	public void checkDirectories() {
		/**	Check for config */
		try{
			if(!getDataFolder().exists()){
				LOGGER.log("Data Folder doesn't exist");
				LOGGER.log("Creating Data Folder");
				getDataFolder().mkdirs();
				LOGGER.log("Data Folder Created at " + getDataFolder());
			}
			File file = new File(getDataFolder(), "config.yml");
			if(!file.exists()){
				LOGGER.log("config.yml not found, creating!");
				saveResource("config.yml", true);
			}
			file = new File(getDataFolder(), "messages.yml");
			if(!file.exists()){
				LOGGER.log("messages.yml not found, creating!");
				saveResource("messages.yml", true);
			}
			file = new File(getDataFolder(), "fileVersions.yml");
			if(!file.exists()){
				LOGGER.log("fileVersions.yml not found, creating!");
				saveResource("fileVersions.yml", true);
			}
		}catch(Exception exception){
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_CHECK_CONFIG).error(exception));
		}
	}

	public void checkConfig() {
		// Config file check
		Version curConfigVersion = new Version(fileVersions.getString("config", "0.0.1"));
		if(curConfigVersion.compareTo(minConfigVersion) < 0) {
			LOGGER.log("config.yml is outdated backing up...");
			try {
				copyFile(getDataFolder() + "" + File.separatorChar + "config.yml",getDataFolder() + "" + File.separatorChar + "backup" + File.separatorChar + "config.yml");
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_COPY_FILE).error(exception));
			}
			LOGGER.log("Saving new config.yml...");
			saveResource("config.yml", true);
			// from new File(getDataFolder() + "" + File.separatorChar + "backup", "config.yml")
			copyConfig("" + getDataFolder() + File.separatorChar + "backup" + File.separatorChar + "config.yml", "" + getDataFolder() + File.separatorChar + "config.yml");
		}
		LOGGER.log("Loading config file...");
		try {
			config.load(new File(getDataFolder() + "" + File.separatorChar + "config.yml"));
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
		}
	}

	public void copyConfig(String from, String to){
		LOGGER.log("Loading new config.yml...");
		try {
			config.load(new File(to));
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_CHECK_CONFIG).error(exception));
		}
		LOGGER.log("Loading old config.yml...");
		try {
			oldconfig.load(new File(from));
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_CHECK_CONFIG).error(exception));
		}
		LOGGER.log("Copying values from backup" + File.separatorChar + "config.yml...");
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
		LOGGER.log("Saving config.yml...");
		try {
			config.save(new File(getDataFolder(), "config.yml"));
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_SAVE_CONFIG).error(exception));
		}
		config = new YmlConfiguration();
		oldconfig = null;
		LOGGER.log("Update complete config.yml...");
	}

	public void checkMessages() {
		// Message file check
		Version curMessagesVersion = new Version(fileVersions.getString("messages", "0.0.1"));
		if(curMessagesVersion.compareTo(minMessagesVersion) < 0) {
			LOGGER.log("messages.yml is outdated backing up...");
			try {
				copyFile(getDataFolder() + "" + File.separatorChar + "messages.yml", getDataFolder() + "" + File.separatorChar + "backup" + File.separatorChar + "messages.yml");
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_MESSAGES_COPY_ERROR).error(exception));
			}
			LOGGER.log("Saving new messages.yml...");
			saveResource("messages.yml", true);
			LOGGER.log("Copying values from backup" + File.separatorChar + "messages.yml...");

			try {
				updateMessages(new File(getDataFolder() + "" + File.separatorChar + "backup" + File.separatorChar + "messages.yml"),
						new File(getDataFolder() + "" + File.separatorChar + "messages.yml"), "sleep");
				updateMessages(new File(getDataFolder() + "" + File.separatorChar + "backup" + File.separatorChar + "messages.yml"),
						new File(getDataFolder() + "" + File.separatorChar + "messages.yml"), "dayskip");
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_MESSAGES_LOAD_ERROR).error(exception));
			}

			LOGGER.log("Saving messages.yml...");
			try {
				messages.save(new File(getDataFolder(), "messages.yml"));
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_OLDMESSAGES_SAVE_ERROR).error(exception));
			}
			messages = new YmlConfiguration();
			oldMessages = null;
			LOGGER.log("Update complete config.yml...");
		}
		LOGGER.log("Loading messages file...");
		try {
			messages.load(new File(getDataFolder() + "" + File.separatorChar + "messages.yml"));
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_MESSAGES_LOAD_ERROR).error(exception));
		}
	}

	public void updateMessages(File oldFile, File newFile, String sectionName) throws IOException {
		// Load the old and new messages.yml files
		FileConfiguration oldMessages = YamlConfiguration.loadConfiguration(oldFile);
		FileConfiguration newMessages = YamlConfiguration.loadConfiguration(newFile);

		// Fetch the specified sections from both files
		ConfigurationSection oldMessagesSection = oldMessages.getConfigurationSection("messages." + sectionName);
		ConfigurationSection newMessagesSection = newMessages.getConfigurationSection("messages." + sectionName);

		if ((oldMessagesSection != null) && (newMessagesSection != null)) {
			Set<String> uniqueMessages = new HashSet<>();

			// Collect unique messages from the old file
			for (String key : oldMessagesSection.getKeys(false)) {
				if (key.startsWith("message_")) {
					String message = oldMessagesSection.getString(key);
					if (message != null) {
						uniqueMessages.add(message);
					}
				}
			}

			// Collect unique messages from the new file
			for (String key : newMessagesSection.getKeys(false)) {
				if (key.startsWith("message_")) {
					String message = newMessagesSection.getString(key);
					if (message != null) {
						uniqueMessages.add(message);
					}
				}
			}

			// Convert the Set to a List
			List<String> messageList = new ArrayList<>(uniqueMessages);

			// Write the unique messages back to the new messages.yml file
			ConfigurationSection updatedMessagesSection = newMessages.createSection("messages." + sectionName);

			updatedMessagesSection.set("count", messageList.size());
			for (int i = 0; i < messageList.size(); i++) {
				updatedMessagesSection.set("message_" + (i + 1), messageList.get(i));
			}
			try {
				// Save the updated configuration to the new file
				newMessages.save(newFile);
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_OLDMESSAGES_SAVE_ERROR).error(exception));
			}
			// Free up memory
			uniqueMessages.clear();
			messageList.clear();
			oldMessagesSection = null;
			newMessagesSection = null;
			updatedMessagesSection = null;
			oldMessages = null;
			newMessages = null;

			// Suggest garbage collection
			System.gc();
		}
	}

}
