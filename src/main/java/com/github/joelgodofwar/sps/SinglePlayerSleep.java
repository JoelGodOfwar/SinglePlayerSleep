package com.github.joelgodofwar.sps;


import com.github.joelgodofwar.sps.api.Metrics;
import com.github.joelgodofwar.sps.commands.Cmd_dayskip;
import com.github.joelgodofwar.sps.commands.Cmd_sleep;
import com.github.joelgodofwar.sps.commands.Cmd_spscancel;
import com.github.joelgodofwar.sps.commands.Cmd_update;
import com.github.joelgodofwar.sps.common.PluginLibrary;
import com.github.joelgodofwar.sps.common.PluginLogger;
import com.github.joelgodofwar.sps.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.sps.common.error.Report;
import com.github.joelgodofwar.sps.enums.Perms;
import com.github.joelgodofwar.sps.events.PlayerBedLeaveHandler;
import com.github.joelgodofwar.sps.events.PlayerJoinHandler;
import com.github.joelgodofwar.sps.i18n.Translator;


import lib.github.joelgodofwar.coreutils.CoreUtils;
import lib.github.joelgodofwar.coreutils.util.ChatColorUtils;
import lib.github.joelgodofwar.coreutils.util.*;

import org.bukkit.*;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author JoelGodOfWar(JoelYahwehOfWar)
 * some code added by ColdCode(coldcode69)
 */

@SuppressWarnings("unused")
public class SinglePlayerSleep extends JavaPlugin implements Listener{
	/** Languages: čeština (cs_CZ), Deutsch (de_DE), English (en_US), Español (es_ES), Español (es_MX), Français (fr_FR), Italiano (it_IT), Magyar (hu_HU), 日本語 (ja_JP), 한국어 (ko_KR), Lolcat (lol_US), Melayu (my_MY), Nederlands (nl_NL), Polski (pl_PL), Português (pt_BR), Русский (ru_RU), Svenska (sv_SV), Türkçe (tr_TR), 中文(简体) (zh_CN), 中文(繁體) (zh_TW) */
	// public final static Logger logger = Logger.getLogger("Minecraft");
	public static String THIS_NAME;
	public static String THIS_VERSION;
	/** update checker variables */
	public int projectID = 68139; // https://spigotmc.org/resources/71236
	public String githubURL = "https://github.com/JoelGodOfwar/SinglePlayerSleep/raw/master/versioncheck/1.20/versions.xml";
	public boolean UpdateAvailable =  false;
	public String UColdVers;
	public String UCnewVers;
	public static boolean UpdateCheck;
	public String DownloadLink = "https://www.spigotmc.org/resources/singleplayersleep.68139";
	/** end update checker variables */
	Version MINIMUM_MINECRAFT_VERSION = new Version("1.20");
	Version MAXIMUM_MINECRAFT_VERSION = new Version("1.21.10");
	Version CURRENT_MINECRAFT_VERSION = Version.getCurrentVersion();
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
	public YmlConfiguration config = new YmlConfiguration(this);
	YamlConfiguration oldconfig = new YamlConfiguration();
	public YmlConfiguration messages = new YmlConfiguration(this);
	public YamlConfiguration oldMessages;
	public FileConfiguration fileVersions  = new YamlConfiguration();
	public File fileVersionsFile;
	public File configFile;
	public File messagesFile;
	public Version minConfigVersion = new Version("1.0.9");
	public Version minMessagesVersion = new Version("1.0.3");
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
	public static PluginLogger LOGGER;

	private final Pattern HEX_COLOR_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})(?![>a-fA-F0-9])");
	// Pattern to match hex color codes in the format §xFFFFFF
	private final Pattern ALT_HEX_COLOR_PATTERN = Pattern.compile("§x([A-Fa-f0-9]{6})");
	// Pattern to match hex color codes in the format §x§F§F§F§F§F§F
	private final Pattern ALT_HEX_COLOR_PATTERN_ALT = Pattern.compile("§x(§[A-Fa-f0-9]){6}");
	private JsonConverter messageConverter;
	public CoreUtils coreUtils = new CoreUtils(this);
	public JsonMessageUtils jsonMessageUtils = new JsonMessageUtils(this);

	@Override
	public void onLoad() {

		LOGGER = new PluginLogger(this);
		reporter = new DetailedErrorReporter(this);
		UpdateCheck = getConfig().getBoolean("auto_update_check", true);
		debug = getConfig().getBoolean("debug", false);
		daLang = getConfig().getString("lang", "en_US");
		displaycancel = getConfig().getBoolean("display_cancel", true);
		//log("displaycancel=" + displaycancel);
		config = new YmlConfiguration(this);
		oldconfig = new YamlConfiguration();
		messages = new YmlConfiguration(this);
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
		config = new YmlConfiguration(this);
		oldconfig = new YamlConfiguration();
		messages = new YmlConfiguration(this);
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
		messageConverter = new JsonConverter(this);

		LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
		LOGGER.log(ChatColor.GREEN + "v" + THIS_VERSION + ChatColor.RESET + " Loading...");
		LOGGER.log("Jar Filename: " + this.getFile().getName());//.getAbsoluteFile());
		LOGGER.log("Server Version: " + getServer().getVersion());
		// Handle unexpected Minecraft versions
		Version checkVersion = this.verifyMinecraftVersion();

		//** DEV check **/
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


		//** Update Checker */
		if(UpdateCheck){
			try {
				LOGGER.log("Checking for updates...");
				VersionChecker updater = new VersionChecker(this, projectID, githubURL);
				if(updater.checkForUpdates()) {
					//** Update available */
					UpdateAvailable = true; // TODO: Update Checker
					UColdVers = updater.oldVersion();
					UCnewVers = updater.newVersion();

					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					LOGGER.log("* " + get("sps.version.message").replace("<MyPlugin>", THIS_NAME) );
					LOGGER.log("* " + get("sps.version.old_vers") + ChatColor.RED + UColdVers );
					LOGGER.log("* " + get("sps.version.new_vers") + ChatColor.GREEN + UCnewVers );
					LOGGER.log("*");
					LOGGER.log("* " + get("sps.version.please_update") );
					LOGGER.log("*");
					LOGGER.log("* " + get("sps.version.download") + ": " + DownloadLink + "/history");
					LOGGER.log("* " + get("sps.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
				}else{
					//** Up to date */
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
			//** auto_update_check is false so nag. */
			LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
			LOGGER.log( "* " + get("sps.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
			LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
		}
		//** end update checker */

		File  file = new File(getDataFolder(), "permissions.yml");
		LOGGER.log("" + file);
		if(!file.exists()){
			LOGGER.log("permissions.yml not found, creating! This is a sample only!");
			saveResource("permissions.yml", true);
		}

		getServer().getPluginManager().registerEvents(new PlayerJoinHandler(this), this);
		getServer().getPluginManager().registerEvents(new PlayerBedLeaveHandler(this), this);
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
					return Objects.requireNonNull(getConfig().getString("auto_update_check")).toUpperCase();
				}
			}));
			// add to site
			metrics.addCustomChart(new Metrics.SimplePie("unrestrictedsleep", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return Objects.requireNonNull(getConfig().getString("unrestrictedsleep")).toUpperCase();
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("var_waketime", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return Objects.requireNonNull(getConfig().getString("waketime")).toUpperCase();
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
					return Objects.requireNonNull(getConfig().getString("cancelbroadcast")).toUpperCase();
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("var_debug", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return Objects.requireNonNull(getConfig().getString("debug")).toUpperCase();
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("var_lang", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return Objects.requireNonNull(getConfig().getString("lang")).toUpperCase();
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
		fileVersions.set("config", getConfig().getString("version"));
		fileVersions.set("messages", messages.getString("version"));
		try {
			fileVersions.save(fileVersionsFile);
		} catch (IOException exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_SAVE_FILEVERSION).error(exception));
		}

		consoleInfo(ChatColor.RED + "DISABLED");
	}

	public void consoleInfo(String state) {
		//LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
		LOGGER.log(ChatColor.YELLOW + " v" + THIS_VERSION + ChatColor.RESET + " is " + state  + ChatColor.RESET);
		//LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
	}

	public void log(String message, Object... args) {
		LOGGER.log(message, args);
	}
	public String nameColor() {
		//Only change name colours if one is set
		if (!Objects.requireNonNull(getConfig().getString("namecolor")).contains("NONE")) {
            return ChatColorUtils.setColors(Objects.requireNonNull(getConfig().getString("namecolor")));
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
			BedEnterResult theResult = event.getBedEnterResult();

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
					if(!Perms.OP.hasPermission(player)){ // TODO: Dayskip blacklist Check
						if((blacklist_dayskip != null)&&!blacklist_dayskip.isEmpty()){
							if(StrUtils.stringContains(blacklist_dayskip, world.getName())){
								LOGGER.log("PIS DS - World - On blacklist.");
								return;
							}
						}
					}
					ItemStack[] inv = player.getInventory().getContents();
					LOGGER.debug("PIS DS got inventory");
					boolean itmDaySkipper = false;
					LOGGER.debug("PIS DS itemdayskipper initilized");
					if( getConfig().getBoolean("dayskipperitemrequired", true) ){
						for(ItemStack item:inv){
							if(!(item == null)){
								LOGGER.debug("PIS DS item=" + item.getType().name());
								if(Objects.requireNonNull(item.getItemMeta()).getDisplayName().equalsIgnoreCase("DaySkipper")){
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
					if(itmDaySkipper){
						LOGGER.debug("PIS DS item DaySkipper is in inventory.");

						Block block = event.getBed();
						LOGGER.debug("PIS DS block.material = " + block.getType());
						LOGGER.debug("PIS DS isBed(block) = " + isBed(block));
						if ( isBed(block) ){
							LOGGER.debug("PIS DS the block is a bed.");
							/* OK, they have the DaySkipper item, now check for the permission*/
							if(Perms.DAYSKIPPER.hasPermission(player)||Perms.OP.hasPermission(player)){
								LOGGER.debug("PIS DS Has perm or is op. ...");
								String CancelBracketColor = "<" + getConfig().getString("cancelbracketcolor", "YELLOW") + ">";
								/* OK, they have the perm, now lets notify the server and schedule the runnable */
								String canmsg = "<msgcolor>[<cancelcolor><click:run_command:'/spscancel'><hover:show_text:'tooltip'>dacancel</hover></click><msgcolor>]";
								String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
								LOGGER.debug("PIS DS ... msgcolor=" + msgcolor);
								int maxmsgs = messages.getInt("messages.dayskip.count");
								int randomnumber = RandomNumber(maxmsgs);
								String sleepmsg;
								if(randomnumber != 0) {
									sleepmsg = messages.getString("messages.dayskip.message_" + randomnumber, "<#FFFFFF><player> <gradient:#000000:#FFFF00:#000000:#FFFF00>wants to sleep the day away</gradient>...");
								}else {
									sleepmsg = "<#FFFFFF><player> <gradient:#FF0000:#FFFFFF>error selecting random message</gradient>...";
								}
								LOGGER.debug("PIS DS maxmsgs=" + maxmsgs);
								LOGGER.debug("PIS DS randomnumber=" + randomnumber);

								LOGGER.debug("PIS IN U sleepmsg=" + sleepmsg);
								sleepmsg = coreUtils.fixColors(sleepmsg);
								LOGGER.debug("PIS DS fixHexCodes sleepmsg=" + sleepmsg);

								sleepmsg = sleepmsg.replace("<colon>", ":");

								/* nickname parser */
								String nickName = getNickname(player);
								if(!nickName.contains("§")){
									LOGGER.debug("PIS DS nickName !contain §");
								}else{
									nickName = coreUtils.fixColors(nickName);
									LOGGER.debug("PIS DS nick contains §" );
									LOGGER.debug("PIS DS nickName AfterParse = " + nickName );
								}
								/* end nickname parser */

								sleepmsg = sleepmsg.replace("<player>", nickName);
								String cancelcolor = "<" + getConfig().getString("cancelcolor") + ">";
								LOGGER.debug("PIS DS ... cancelcolor=" + cancelcolor);
								canmsg = canmsg.replace("dacancel", get("sps.message.cancel") );
								canmsg = canmsg.replace("<cancelcolor>", cancelcolor);
								canmsg = canmsg.replace("<msgcolor>", CancelBracketColor);
								canmsg = canmsg.replace("tooltip",  get("sps.message.clickcancel"));
								LOGGER.debug("PIS DS string processed. ...");

								if(getConfig().getBoolean("broadcast_per_world", true)){
									sendJson(player.getWorld(), sleepmsg, canmsg);
								}else{
									sendJson(sleepmsg, canmsg);
								}
								LOGGER.debug("PIS DS SendAllJsonMessage. ...");
								if(!isDSCanceled){
									LOGGER.debug("PIS DS !isDSCanceled. ...");

									dayskipTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

										@Override public void run() {
											setDStime(player, world);
											LOGGER.debug("PIS DS setDStime has run. ...");
										}
									}, dayskipdelay * 20L);

								}else{
									isDSCanceled = false;
								}
								return;
							}else{
								player.sendMessage(ChatColor.YELLOW + get("sps.message.noperm"));
							}
						}else{
							LOGGER.debug("PIS DS block is not a Bed");
							player.sendMessage(ChatColor.YELLOW + get("sps.message.dayskipblackbed"));/* NOT A BLACK BED */
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
			if(!isBloodmoonInprogress(player.getWorld())){
				if(event.getBedEnterResult() == BedEnterResult.OK){
					// Check it's night or if storm
					if (IsNight(player.getWorld())||player.getWorld().isThundering()) {
						if(!Perms.OP.hasPermission(player)){ // TODO: Sleep Blacklist Check
							if((blacklist_sleep != null)&&!blacklist_sleep.isEmpty()){
								if(StrUtils.stringContains(blacklist_sleep, world.getName())){
									LOGGER.log("PIS IN - World - On blacklist.");
									return;
								}
							}
						}
						// Set the default timer for when the player has never slept before
						long timer = 0;
						LOGGER.debug("PIS IN... " + player.getName() + " is sleeping.");
						long time = System.currentTimeMillis() / 1000;
						if(sleeplimit.get(player.getUniqueId()) == null){
							LOGGER.debug("PIS IN sleeplimit UUID=null");
							// Check if player has sps.unrestricted
							if (!Perms.UNRESTRICTED.hasPermission(player)) {
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
								String sleeplimit = get("sps.message.sleeplimit").replace("<length>", "" + length);
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
							LOGGER.debug("PIS IN U unrestrictedsleep=true");
							String dastring = get("sps.message.issleep");
							dastring = dastring.replace("<player>", getNickname(player));
							this.broadcast(dastring, world);
							long startTime = System.currentTimeMillis();
							transitionTaskUnrestricted = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
								@Override public void run() {
									setDatime(player, world);
									resetPlayersRestStat(world);
									String elapsedTime = LoadTime(startTime); // Reuse your existing method
									LOGGER.debug("PIS IN U setDatime has run. Elapsed: " + elapsedTime);
								}
							}, sleepdelay * 20L);
						}else //Don't show a cancel option if a player has unrestricted sleep perm
							if (Perms.UNRESTRICTED.hasPermission(player)) { //TODO: Unrestricted Broadcast
								//use random msgs, and colorization

								LOGGER.debug("PIS IN U Has unrestricted perm. ...");

								//Broadcast to Server
								String dastring = get("sps.message.issleep");

								dastring = dastring.replace("<player>", "");
								String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
								LOGGER.debug("PIS IN U ... msgcolor=" + msgcolor);
								String CancelBracketColor = "<" + getConfig().getString("cancelbracketcolor", "YELLOW") + ">";
								LOGGER.debug("PIS IN U ... CancelBracketColor=" + CancelBracketColor);
								String canmsg = "<msgcolor>[<cancelcolor><hover:show_text:'tooltip'><st>dacancel</st></hover><msgcolor>]";
								String damsg = ""; //"[\"\",{\"text\":\"sleepmsg " + canmsg + "\"}]";
								String sleepmsg;
								if (getConfig().getBoolean("randomsleepmsgs")){
									int maxmsgs = messages.getInt("messages.sleep.count");
									int randomnumber = RandomNumber(maxmsgs);
									if(randomnumber != 0) {
										sleepmsg = messages.getString("messages.sleep.message_" + randomnumber, "<#FFFFFF><player> <#FFAA00>went to bed. Sweet Dreams!");
									}else {
										sleepmsg = "<#FFFFFF><player> <gradient:#FF0000:#FFFFFF>error selecting random message</gradient>...";
									}
									sleepmsg = sleepmsg.replace("<colon>", ":");
									LOGGER.debug("PIS IN U ... maxmsgs=" + maxmsgs);
									LOGGER.debug("PIS IN U ... randomnumber=" + randomnumber);
								}else{
									sleepmsg = ("<#FFFFFF><player> <#FFAA00>went to bed. Sweet Dreams!");
									LOGGER.debug("PIS IN U ... randomsleepmsgs=false");
								}

								LOGGER.debug("PIS IN U sleepmsg=" + sleepmsg);
								sleepmsg = coreUtils.fixColors(sleepmsg);
								LOGGER.debug("PIS IN U fixHexCodes sleepmsg=" + sleepmsg);

								/* nickname parser */
								String nickName = getNickname(player);
								if(!nickName.contains("§")){
									LOGGER.debug("PIS IN U nickName !contain §");
								}else{
									nickName = coreUtils.fixColors(nickName);
									LOGGER.debug("PIS IN U nick contains §" );
									LOGGER.debug("PIS IN U nickName AfterParse = " + nickName );
								}
								/* end nickname parser */

								sleepmsg = sleepmsg.replace("<player>", nickName);
								String cancelcolor = "<" + getConfig().getString("cancelcolor") + ">";
								LOGGER.debug("PIS IN U ... cancelcolor=" + cancelcolor);
								canmsg = canmsg.replace("dacancel", get("sps.message.cancel") );
								canmsg = canmsg.replace("<cancelcolor>", cancelcolor);
								canmsg = canmsg.replace("<msgcolor>", CancelBracketColor);
								canmsg = canmsg.replace("tooltip",  "Unrestricted cannot cancel.");
								LOGGER.debug("PIS IN U string processed. ...");

								if(getConfig().getBoolean("broadcast_per_world", true)){
									sendJson(player.getWorld(), sleepmsg, canmsg);
								}else{
									sendJson(sleepmsg, canmsg);
								}
								LOGGER.debug("PIS IN U SendAllJsonMessage. ...");

								long startTime = System.currentTimeMillis();
								transitionTaskUnrestricted = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
									@Override public void run() {
										setDatime(player, world);
										resetPlayersRestStat(world);
										String elapsedTime = LoadTime(startTime); // Reuse your existing method
										LOGGER.debug("PIS IN U setDatime has run. Elapsed: " + elapsedTime);
									}
								}, sleepdelay * 20L);

							} else if(!isCanceled&&!event.isCancelled()){ // TODO: Normal Sleep
								if (Perms.HERMITS.hasPermission(player) || Perms.OP.hasPermission(player)) {
									LOGGER.debug("PIS IN Has perm or is op.");

									// Colors from config
									String cancelBracketColor = getConfig().getString("cancelbracketcolor", "YELLOW").toLowerCase();
									LOGGER.debug("PIS IN ... CancelBracketColor=<" + cancelBracketColor + ">");
									String cancelcolor = getConfig().getString("cancelcolor", "RED").toLowerCase();
									LOGGER.debug("PIS IN ... cancelcolor=<" + cancelcolor + ">");

									// Convert config colors to JSON-compatible colors
									String jsonCancelBracketColor = messageConverter.convertToJsonColor(cancelBracketColor);
									String jsonCancelColor = messageConverter.convertToJsonColor(cancelcolor);

									// Construct a sleep message
									String sleepmsg;
									if (getConfig().getBoolean("randomsleepmsgs")) {
										int maxmsgs = messages.getInt("messages.sleep.count");
										int randomnumber = RandomNumber(maxmsgs);
										LOGGER.debug("PIS IN ... maxmsgs=" + maxmsgs);
										LOGGER.debug("PIS IN ... randomnumber=" + randomnumber);
										if (randomnumber != 0) {
											sleepmsg = messages.getString("messages.sleep.message_" + randomnumber, "<#FFFFFF><player> <#FFAA00>went to bed. Sweet Dreams!");
										} else {
											sleepmsg = "<#FFFFFF><player> <gradient:#FF0000:#FFFFFF>error selecting random message</gradient>...";
										}
										sleepmsg = sleepmsg.replace("<colon>", ":");
									} else {
										sleepmsg = messages.getString("messages.sleep.message_2", "<#FFFFFF><player> <#FFAA00>went to bed. Sweet Dreams!");
										LOGGER.debug("PIS IN ... randomsleepmsgs=false");
									}
									LOGGER.debug("PIS IN sleepmsg=" + sleepmsg);

									// Fix old hex codes
									sleepmsg = coreUtils.fixColors(sleepmsg);
									LOGGER.debug("PIS IN fixHexCodes sleepmsg=" + sleepmsg);

									// Nickname parser
									String nickName = getNickname(player);
									if (nickName.contains("§") || nickName.contains("&")) {
										nickName = coreUtils.fixColors(nickName);
										LOGGER.debug("PIS IN nick contains § or &");
										LOGGER.debug("PIS IN nickName AfterParse = " + nickName);
									} else {
										LOGGER.debug("PIS IN nickName !contain § or &");
									}

									// Convert to JSON
									String jsonSleepmsg = messageConverter.convert(sleepmsg, nickName);
									LOGGER.debug("PIS IN jsonSleepmsg=" + jsonSleepmsg);

									// Construct cancel message as JSON
									String jsonCanmsg = "[{\"text\":\"[\",\"color\":\"" + jsonCancelBracketColor + "\"}," +
											"{\"text\":\"CANCEL\",\"color\":\"" + jsonCancelColor + "\"," +
											"\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"}," +
											"\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"" + get("sps.message.clickcancel", "Click to cancel sleep") + "\"}}," +
											"{\"text\":\"]\",\"color\":\"" + jsonCancelBracketColor + "\"}]";
									LOGGER.debug("PIS IN jsonCanmsg=" + jsonCanmsg);

									// Broadcast message
									if (getConfig().getBoolean("broadcast_per_world", true)) {
										sendJson(player.getWorld(), jsonSleepmsg, jsonCanmsg);
									} else {
										sendJson(jsonSleepmsg, jsonCanmsg);
									}
									LOGGER.debug("PIS IN SendAllJsonMessage.");

									// Schedule time transition
									if (!isCanceled && !event.isCancelled()) {
										LOGGER.debug("PIS IN !isCanceled.");
										long startTime = System.currentTimeMillis();
										transitionTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
											setDatime(player, world);
											resetPlayersRestStat(world);
											String elapsedTime = LoadTime(startTime); // Reuse your existing method
											LOGGER.debug("PIS IN setDatime has run. Elapsed: " + elapsedTime);
										}, sleepdelay * 20L);
									} else {
										if (isCanceled) {
											LOGGER.debug("PIS IN isCanceled=" + isCanceled);
										}
										if (event.isCancelled()) {
											LOGGER.debug("PIS IN event.isCanceled=" + event.isCancelled());
										}
										isCanceled = false;
									}
								} else {
									player.sendMessage(ChatColor.YELLOW + get("sps.message.noperm"));
								}
							}else{
								isCanceled = false;
								if(isCanceled){LOGGER.debug("PIS IN isCanceled=" + isCanceled);}
								if(event.isCancelled()){LOGGER.debug("PIS event.isCanceled=" + event.isCancelled());}
							}
					}else{ // It is not Night or Storming, so tell the player
						if(getConfig().getBoolean("notifymustbenight")){
							player.sendMessage(ChatColorUtils.setColors(get("sps.message.nightorstorm")));
							LOGGER.debug("PIS IN it was not night and player was notified. ...");
						}
						//if(debug){logDebug("getBedSpawnLocation=" + player.getBedSpawnLocation());}
						//if(debug){logDebug("getBed=" + event.getBed().getLocation());}
						//player.getBedSpawnLocation().equals(event.getBed().getLocation()
						//String sv = serverVersion();
						Version sVersion = Version.getCurrentVersion();
						if(!sVersion.isAtLeast(new Version("1.15"))){
							Block bed = event.getBed();
							Location bedSpawn = player.getBedSpawnLocation();
							if(bedSpawn != null){
								boolean isSameBed = checkradius(bedSpawn, event.getBed().getLocation(), 5);
								if (!isSameBed||player.getBedSpawnLocation().equals(null)) {
									if(player.getBedSpawnLocation().equals(null)){
										LOGGER.debug("PIS !IN bedspawn=null");
									}else if(!isSameBed){
										LOGGER.debug("PIS !IN bedspawn!=bed");
									}
									player.setBedSpawnLocation(event.getBed().getLocation());
									player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + get("sps.message.respawnpointmsg").replace("<x>", "" + bed.getX()).replace("<z>", "" + bed.getZ()));
									LOGGER.debug("PIS !IN bedspawn was set for player " + ChatColor.GREEN + player.getDisplayName() + ChatColor.RESET + " ...");
								}
							}else{
								player.setBedSpawnLocation(event.getBed().getLocation());
								player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + get("sps.message.respawnpointmsg").replace("<x>", "" + bed.getX()).replace("<z>", "" + bed.getZ()));
								LOGGER.debug("PIS !IN bedspawn was set for player " + ChatColor.GREEN + player.getDisplayName() + ChatColor.RESET + " ...");
							}
						} else {LOGGER.debug("PIS !IN Server is 1.15+");
						}
					}
				}
			}else{
				player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + get("sps.message.bloodmoon", "You can not sleep during a bloodmoon."));
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
		if (getIt.equals("error")) {
			LOGGER.debug("Could not get '" + path + "', returned default.");
			return string;
		}
		return getIt;
	}

	public boolean checkradius(Location player, Location location, int radius){
		double distance = player.distance(location);
		if(distance <= radius) {
			LOGGER.debug("truedistance=" + distance);
			return true;
			//shulker.teleport(block.getLocation());
		}
		LOGGER.debug("falsedistance=" + distance);
		return false;
	}

	public void setDatime(Player player, World world){
		if(world.hasStorm()){
			if(Perms.DOWNFALL.hasPermission(player)){
				world.setStorm(false);
				LOGGER.debug("sDT " + get("sps.message.setdownfall") + "...");
			} else {LOGGER.debug("sDT " + getNickname(player) + " Does not have permission sps.downfall ...");}
		}
		if(world.isThundering()){
			if(Perms.THUNDER.hasPermission(player)){
				world.setThundering(false);
				LOGGER.debug("sDT" + get("sps.message.setthunder") + "...");
			} else {LOGGER.debug("sDT" + getNickname(player) + " Does not have permission sps.thunder ...");}
		}
		String waketime = getConfig().getString("waketime", "NORMAL");
		long timeoffset = 0;
		if(waketime.equalsIgnoreCase("early")||waketime.equalsIgnoreCase("23000")){
			timeoffset = 1000;
		}
		long Relative_Time = (24000 - world.getTime()) - timeoffset;
		long daFullTime = world.getFullTime();
		world.setFullTime(daFullTime + Relative_Time);

		LOGGER.debug(get("sps.message.settime") + "...");
	}

	public void setDStime(Player player, World world){
		int timeoffset = 10000;
		long Relative_Time = (24000 - world.getTime()) - timeoffset;
		world.setFullTime(world.getFullTime() + Relative_Time);
		LOGGER.debug("sDSt " + get("sps.message.dayskipsettime") + "...");
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) { // TODO: Tab Complete
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
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args){
		try{
			if (command.getName().equalsIgnoreCase("SPS")){
				if (args.length == 0){
					sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SinglePlayerSleep" + ChatColor.GREEN + "]===============[]");
					sender.sendMessage(ChatColor.YELLOW + " " + get("sps.message.touse"));//Sleep in a bed to use.");
					sender.sendMessage(ChatColor.WHITE + " ");
					sender.sendMessage(ChatColor.WHITE + " /Sleep - " + get("sps.message.sleephelp"));//subject to server admin approval");
					sender.sendMessage(ChatColor.WHITE + " /spscancel - " + get("sps.command.cancelhelp"));//Cancels SinglePlayerSleep");
					sender.sendMessage(ChatColor.WHITE + " ");
					if(Perms.OP.hasPermissionOrOp(sender)){
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
					if(Perms.UPDATE.hasPermissionOrOp(sender)) {
						return new Cmd_update(this).execute(sender, args);
					}
				}

				if(args[0].equalsIgnoreCase("check")){
					// /sps check
					// /sps check @p
					// /cmd 0     1
					if(args.length == 1){
						String damsg = "sps.hermits=" + Perms.HERMITS.hasPermission(sender) + ", " + "sps.cancel=" + Perms.CANCEL.hasPermission(sender) + ", " +
								"sps.unrestricted=" + Perms.UNRESTRICTED.hasPermission(sender) + ", " + "sps.downfall=" + Perms.DOWNFALL.hasPermission(sender) + ", " +
								"sps.thunder=" + Perms.THUNDER.hasPermission(sender) + ", " + "sps.command=" + Perms.COMMAND.hasPermission(sender) + ", " +
								"sps.update=" + Perms.UPDATE.hasPermission(sender) + ", " + "sps.op=" + Perms.OP.hasPermission(sender) + ", " +
								"sps.showUpdateAvailable=" + Perms.SHOW_UPDATE_AVAILABLE.hasPermission(sender) + ", " +
								"sps.dayskipper=" + Perms.DAYSKIPPER.hasPermission(sender) + ", " + "sps.dayskipcommand=" + Perms.DAYSKIPCOMMAND.hasPermission(sender) ;
						sender.sendMessage(damsg.replace("=true,", "=" + ChatColor.GREEN + "true" + ChatColor.RESET + ",").replace("=false,", "=" + ChatColor.RED + "false" + ChatColor.RESET + ","));
						return true;
					}else if(args.length > 1){
						if(!(sender instanceof Player)||Perms.OP.hasPermission(sender)) {
							try {
								Player player = Bukkit.getPlayer(args[1]);
                                assert player != null;
                                String damsg = "Player \"" + player.getName() + "\" has the following permissions: " +
										"sps.hermits=" + Perms.HERMITS.hasPermission(player) + ", " + "sps.cancel=" + Perms.CANCEL.hasPermission(player) + ", " +
										"sps.unrestricted=" + Perms.UNRESTRICTED.hasPermission(player) + ", " + "sps.downfall=" + Perms.DOWNFALL.hasPermission(player) + ", " +
										"sps.thunder=" + Perms.THUNDER.hasPermission(player) + ", " + "sps.command=" + Perms.COMMAND.hasPermission(player) + ", " +
										"sps.update=" + Perms.UPDATE.hasPermission(player) + ", " + "sps.op=" + Perms.OP.hasPermission(player) + ", " +
										"sps.showUpdateAvailable=" + Perms.SHOW_UPDATE_AVAILABLE.hasPermission(player) + ", " +
										"sps.dayskipper=" + Perms.DAYSKIPPER.hasPermission(player) + ", " + "sps.dayskipcommand=" + Perms.DAYSKIPCOMMAND.hasPermission(player) ;
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
					if(Perms.OP.hasPermissionOrOp(sender)){
						debug = !debug;
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.debugtrue").replace("<boolean>", get("sps.message.boolean." + debug) ));
						return true;
					}else{
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.noperm"));
						return false;
					}
				}//*/

				if(args[0].equalsIgnoreCase("reload")){ // TODO: Command Reload
					if(Perms.OP.hasPermissionOrOp(sender)){
						//ConfigAPI.Reloadconfig(this, p);
						config = new YmlConfiguration(this);
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

						lang2 = new Translator(daLang, getDataFolder().toString());
						LOGGER = new PluginLogger(this);
						reporter = new DetailedErrorReporter(this);
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.reloaded"));
					}else if(!Perms.OP.hasPermission(sender)){
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
		LOGGER.debug(command + Arrays.toString(args) + " returned default.");
		return true;
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

	public int RandomNumber(int maximum){
		Random rand = new Random();
		int min = 1;
        // nextInt as provided by Random is exclusive of the top value so you need to add 1
		int randomNum;
		try {
			randomNum = rand.nextInt((maximum - min) + 1) + min;
		}catch(Exception exception) {
			randomNum = 0;
		}
		return randomNum;
	}

	public boolean fileContains(String filePath, String searchQuery) throws IOException{
		searchQuery = searchQuery.trim();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(searchQuery)) {
                    //log("findstring found");
                    return true;
                }
            }
        } catch (Exception exception) {
            reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_SEARCHING_FILE).error(exception));
        }
		//log("findstring failed");
		return false;
	}
	public String addChar(String str, String ch, int position) {
		StringBuilder sb = new StringBuilder(str);
		sb.insert(position, ch);
		return sb.toString();
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

	public boolean isBloodmoonInprogress(World world) {
		// com.pseudonova.bloodmoon.api.BloodmoonAPI
		try {
			Plugin bloodmoonPlugin = getServer().getPluginManager().getPlugin("bloodmoon-advanced");
			if (bloodmoonPlugin != null) {
				// Use the plugin's ClassLoader to load BloodmoonAPI
				Class<?> bloodmoonApiClass = Class.forName("BloodmoonAPI", true, bloodmoonPlugin.getClass().getClassLoader());

				// Get the method: bloodmoonIsRunning(World)
				Method bloodmoonIsRunning = bloodmoonApiClass.getMethod("bloodmoonIsRunning", World.class);

				// Invoke the method (assuming static)
				return (Boolean) bloodmoonIsRunning.invoke(null, world);
			}
		} catch (ClassNotFoundException e) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_CHECKING_BLOODMOON)
					.error(new ClassNotFoundException("BloodmoonAPI class not found in bloodmoon-advanced plugin")));
		} catch (NoSuchMethodException e) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_CHECKING_BLOODMOON)
					.error(new NoSuchMethodException("bloodmoonIsRunning(World) method not found")));
		} catch (Exception e) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_CHECKING_BLOODMOON).error(e));
		}
		return false;
	}

	/**
	 * Retrieves the nickname of the specified player.
	 *
	 * @param player The player whose nickname is to be retrieved.
	 * @return The nickname of the player, or the player's name if no nickname is found.
	 */
	public String getNickname(Player player) {
		LOGGER.debug("GN - player.getDisplayName()=" + player.getDisplayName());
		LOGGER.debug("GN - player.getName()=" + player.getName());
		LOGGER.debug("GN - usedisplayname=" + getConfig().getBoolean("nickname.usedisplayname"));

		boolean useDisplayName = config.getBoolean("nickname.usedisplayname", false);
		String fallbackName = useDisplayName ? player.getDisplayName() : player.getName();

		// Wrap library's async callback in the future for sync blocking
		CompletableFuture<String> nickFuture = new CompletableFuture<>();
		// Library provides color-fixed nick
		coreUtils.getNicknameAsync(this, player, useDisplayName, nickFuture::complete);

		try {
			// Block briefly; timeout prevents hangs (adjust if needed)
			String libraryNick = nickFuture.get(1, TimeUnit.SECONDS);
			LOGGER.debug("GN - Library fetched nick: " + libraryNick);
			return libraryNick; // Trust library's color fixing
		} catch (ExecutionException | InterruptedException e) {
			LOGGER.debug("GN - Library interrupted, using fallback: " + fallbackName);
		} catch (java.util.concurrent.TimeoutException e) {
			LOGGER.debug("GN - Library timeout, using fallback: " + fallbackName);
		}
		// Fallback if no nick or error
		LOGGER.debug("GN - No library nick found, using: " + fallbackName);
		return coreUtils.fixColors(fallbackName); // Trust library for fallback colors
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
			if( Perms.SHOW_CANCELLED_MSG.hasPermission(player) ) {
				sendJsonString(player, string);
			}
		}
	}
	public void sendPermJson(World world, String string, String perm){
		for (Player player: world.getPlayers()){
			if( Perms.SHOW_CANCELLED_MSG.hasPermission(player) ) {
				sendJsonString(player, string);
			}
		}
	}
	public void sendJsonString(Player player, String string) {
		jsonMessageUtils.sendJsonMessage(player, string);
	}

	/**
	 * Sends JSON-formatted messages to all players on the server using Bukkit.getOnlinePlayers().
	 * Players with the "sps.cancel" permission receive a combined message of sleep_msg and can_msg,
	 * while others receive only the sleep_msg. The method logs debug information for the messages sent.
	 *
	 * @param sleep_msg  The JSON-formatted sleep message to be sent to all players.
	 * @param can_msg    The JSON-formatted cancel message to be sent to players with "sps.cancel" permission.
	 */
	public void sendJson(String sleep_msg, String can_msg) {
		int i = 0;
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (Perms.CANCEL.hasPermission(player) && displaycancel) {
				// Combine JSON arrays
				String fullMessage = "[" + sleep_msg.substring(1, sleep_msg.length() - 1) + ",{\"text\":\" \"},"
						+ can_msg.substring(1, can_msg.length() - 1) + "]";
				jsonMessageUtils.sendJsonMessage(player, fullMessage);
				LOGGER.debug("SAJM - fullMessage=" + fullMessage);
				if (i == 0) {
					LOGGER.debug("SAJM - fullMessageContent=" + fullMessage.replace("\\", ""));
				}
				LOGGER.debug("SAJM - perm & display - Broadcast");
			} else {
				jsonMessageUtils.sendJsonMessage(player, sleep_msg);
				LOGGER.debug("SAJM - sleep_msg=" + sleep_msg);
				if (i == 0) {
					LOGGER.debug("SAJM - sleepMessageContent=" + sleep_msg.replace("\\", ""));
				}
				LOGGER.debug("SAJM - !perm & display - Broadcast");
			}
			i++;
		}
	}

	/**
	 * Sends JSON-formatted messages to all players in the specified world.
	 * Players with the "sps.cancel" permission receive a combined message of sleep_msg and can_msg,
	 * while others receive only the sleep_msg. The method logs debug information for the messages sent.
	 *
	 * @param world      The world containing the players to send messages to.
	 * @param sleep_msg  The JSON-formatted sleep message to be sent to all players.
	 * @param can_msg    The JSON-formatted cancel message to be sent to players with "sps.cancel" permission.
	 */
	public void sendJson(World world, String sleep_msg, String can_msg) {
		int i = 0;
		for (Player player : world.getPlayers()) {
			if (player.hasPermission("sps.cancel") && displaycancel) {
				// Combine JSON arrays
				String fullMessage = "[" + sleep_msg.substring(1, sleep_msg.length() - 1) + ",{\"text\":\" \"},"
						+ can_msg.substring(1, can_msg.length() - 1) + "]";
				jsonMessageUtils.sendJsonMessage(player, fullMessage);
				LOGGER.debug("WS SAJM - fullMessage=" + fullMessage);
				if (i == 0) {
					LOGGER.debug("WS SAJM - fullMessageContent=" + fullMessage.replace("\\", ""));
				}
				LOGGER.debug("WS SAJM - perm & display - Broadcast");
			} else {
				jsonMessageUtils.sendJsonMessage(player, sleep_msg);
				LOGGER.debug("WS SAJM - sleep_msg=" + sleep_msg);
				if (i == 0) {
					LOGGER.debug("WS SAJM - sleepMessageContent=" + sleep_msg.replace("\\", ""));
				}
				LOGGER.debug("WS SAJM - !perm & display - Broadcast");
			}
			i++;
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

	// Used to check the Minecraft version
	private Version verifyMinecraftVersion() {
		try {
			// We'll just warn the user for now
			if (CURRENT_MINECRAFT_VERSION.compareTo(MINIMUM_MINECRAFT_VERSION) < 0) {
				LOGGER.warn("Version " + CURRENT_MINECRAFT_VERSION + " is lower than the minimum " + MINIMUM_MINECRAFT_VERSION);
			}
			if (CURRENT_MINECRAFT_VERSION.compareTo(MAXIMUM_MINECRAFT_VERSION) > 0) {
				LOGGER.warn("Version " + CURRENT_MINECRAFT_VERSION + " has not yet been tested! Proceed with caution.");
			}
			return CURRENT_MINECRAFT_VERSION;
		} catch (Exception exception) {
			reporter.reportWarning(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_PARSE_MINECRAFT_VERSION).error(exception).messageParam(MAXIMUM_MINECRAFT_VERSION));
			// Unknown version - just assume it is the latest
			return MAXIMUM_MINECRAFT_VERSION;
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
		//**	Check for config */
		try{
			if(!getDataFolder().exists()){
				LOGGER.log("Data Folder doesn't exist");
				LOGGER.log("Creating Data Folder");
				boolean theResult = getDataFolder().mkdirs();
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
		config = new YmlConfiguration(this);
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

			/* LOGGER.log("Saving messages.yml...");
			try {
				messages.save(new File(getDataFolder(), "messages.yml"));
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_OLDMESSAGES_SAVE_ERROR).error(exception));
			}//*/
			messages = new YmlConfiguration(this);
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
		FileConfiguration oldMessages = YmlConfiguration.loadConfiguration(oldFile);
		FileConfiguration newMessages = YmlConfiguration.loadConfiguration(newFile);

		// Fetch the specified sections from both files
		ConfigurationSection oldMessagesSection = oldMessages.getConfigurationSection("messages." + sectionName);
		ConfigurationSection newMessagesSection = newMessages.getConfigurationSection("messages." + sectionName);

		if ((oldMessagesSection != null) && (newMessagesSection != null)) {
			Set<String> uniqueMessages = new HashSet<>();

			// Collect unique messages from the new file
			for (String key : newMessagesSection.getKeys(false)) {
				if (key.startsWith("message_")) {
					String message = newMessagesSection.getString(key);
					if (message != null) {
						uniqueMessages.add(coreUtils.fixColors(message));
						LOGGER.debug("Added new " + key + " = " + message);
					}else {
						LOGGER.debug("new " + key + " = empty");
					}
				}
			}

			// Collect unique messages from the old file
			for (String key : oldMessagesSection.getKeys(false)) {
				if (key.startsWith("message_")) {
					String message = oldMessagesSection.getString(key);
					if (message != null) {
						uniqueMessages.add(coreUtils.fixColors(message));
						LOGGER.debug("Added old " + key + " = " + message);
					}else {
						LOGGER.debug("old " + key + " = empty");
					}
				}
			}


			// Convert the Set to a List
			List<String> messageList = new ArrayList<>(uniqueMessages);
			LOGGER.debug("Converted unique messages set to list with size: " + messageList.size());

			// Write the unique messages back to the new messages.yml file
			ConfigurationSection updatedMessagesSection = newMessages.createSection("messages." + sectionName);

			updatedMessagesSection.set("count", messageList.size());
			for (int i = 0; i < messageList.size(); i++) {
				updatedMessagesSection.set("message_" + (i + 1), messageList.get(i));
				LOGGER.debug("Set message_" + (i + 1) + ": " + messageList.get(i));
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

	public static PluginLogger getPluginLogger(){
        return LOGGER;
    }

	/*
	 * Converts hex color codes in the format #FFFFFF, §xFFFFFF, or §x§F§F§F§F§F§F to <#FFFFFF>.
	 * @param message The input string with hex color codes.
	 * @return The formatted string with standardized hex color codes.
	 */
	/* public String fixHexCodes(String message) {
		// Convert §x§F§F§F§F§F§F format to #FFFFFF
		Matcher altMatcherAlt = ALT_HEX_COLOR_PATTERN_ALT.matcher(message);
		StringBuffer altResultAlt = new StringBuffer();

		while (altMatcherAlt.find()) {
			String hexColor = altMatcherAlt.group(); // Grab the entire matched string
			hexColor = "<" + hexColor.replace("§x", "#").replace("§", "") + ">"; // Transform to #FFFFFF
			altMatcherAlt.appendReplacement(altResultAlt, hexColor);
		}
		altMatcherAlt.appendTail(altResultAlt);
		String standardizedMessageAlt = altResultAlt.toString();

		// Convert §xFFFFFF format to #FFFFFF
		Matcher altMatcher = ALT_HEX_COLOR_PATTERN.matcher(standardizedMessageAlt);
		StringBuffer altResult = new StringBuffer();

		while (altMatcher.find()) {
			String hexColor = altMatcher.group(1);
			String standardizedHex = "<#" + hexColor + ">";

			altMatcher.appendReplacement(altResult, standardizedHex);
		}
		altMatcher.appendTail(altResult);
		String standardizedMessage = altResult.toString();

		// Convert #FFFFFF format to <#FFFFFF>
		Matcher hexMatcher = HEX_COLOR_PATTERN.matcher(standardizedMessage);
		StringBuffer hexResult = new StringBuffer();

		while (hexMatcher.find()) {
			String hexColor = hexMatcher.group(1);
			int start = hexMatcher.start();
			int end = hexMatcher.end();

			// Check if the hex code is inside a gradient tag
			boolean insideGradient = standardizedMessage.substring(0, start).matches(".*<gradient:[^>]+$") &&
					standardizedMessage.substring(end).matches("^[^<]*</gradient>.*");

			// Check if the hex code is surrounded by colons
			boolean surroundedByColons = ((start > 0) && (standardizedMessage.charAt(start - 1) == ':')) ||
					((end < standardizedMessage.length()) && (standardizedMessage.charAt(end) == ':'));

			if (!insideGradient && !surroundedByColons) {
				String formattedHex = "<#" + hexColor + ">";
				hexMatcher.appendReplacement(hexResult, formattedHex);
			} else {
				hexMatcher.appendReplacement(hexResult, "#" + hexColor);
			}
		}
		hexMatcher.appendTail(hexResult);
		return hexResult.toString();
	}//*/

}
