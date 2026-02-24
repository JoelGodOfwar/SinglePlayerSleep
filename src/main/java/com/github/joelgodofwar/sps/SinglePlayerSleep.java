package com.github.joelgodofwar.sps;

import com.github.joelgodofwar.sps.commands.Cmd_dayskip;
import com.github.joelgodofwar.sps.commands.Cmd_sleep;
import com.github.joelgodofwar.sps.commands.Cmd_spscancel;
import com.github.joelgodofwar.sps.commands.Cmd_update;
import com.github.joelgodofwar.sps.common.PluginLibrary;

import com.github.joelgodofwar.sps.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.sps.common.error.Report;
import com.github.joelgodofwar.sps.enums.Perms;
import com.github.joelgodofwar.sps.events.PlayerBedEnterHandler;
import com.github.joelgodofwar.sps.events.PlayerBedLeaveHandler;
import com.github.joelgodofwar.sps.events.PlayerJoinHandler;
import com.github.joelgodofwar.sps.events.PlayerQuitHandler;
import com.github.joelgodofwar.sps.i18n.Translator;
import lib.github.joelgodofwar.coreutils.CoreUtils;
import lib.github.joelgodofwar.coreutils.util.*;
import lib.github.joelgodofwar.coreutils.util.common.PluginLogger;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.LinkedHashSet;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.joelgodofwar.sps.util.SPSUtil.IsNight;

/**
 * @author JoelGodOfWar(JoelYahwehOfWar)
 * some code added by ColdCode(coldcode69)
 */

@SuppressWarnings("unused")
public class SinglePlayerSleep extends JavaPlugin implements Listener {
	/**
	 * Languages: čeština (cs_CZ), Deutsch (de_DE), English (en_US), Español (es_ES), Español (es_MX), Français (fr_FR), Italiano (it_IT), Magyar (hu_HU), 日本語 (ja_JP), 한국어 (ko_KR), Lolcat (lol_US), Melayu (my_MY), Nederlands (nl_NL), Polski (pl_PL), Português (pt_BR), Русский (ru_RU), Svenska (sv_SV), Türkçe (tr_TR), 中文(简体) (zh_CN), 中文(繁體) (zh_TW)
	 */
	// public final static CoreUtils CoreUtils = CoreUtils.getCoreUtils("Minecraft");
	public static String THIS_NAME;
	public static String THIS_VERSION;
	/**
	 * update checker variables
	 */
	public int projectID = 68139; // https://spigotmc.org/resources/71236
	public String githubURL = "https://github.com/JoelGodOfwar/SinglePlayerSleep/raw/master/versioncheck/1.20/versions.xml";
	public boolean UpdateAvailable = false;
	public String UColdVers;
	public String UCnewVers;
	public static boolean UpdateCheck;
	public String DownloadLink = "https://www.spigotmc.org/resources/singleplayersleep.68139";
	/**
	 * end update checker variables
	 */
	Version MINIMUM_MINECRAFT_VERSION = new Version("1.20");
	Version MAXIMUM_MINECRAFT_VERSION = new Version("1.21.11");
	Version CURRENT_MINECRAFT_VERSION = Version.getCurrentVersion();
	public Version minConfigVersion = new Version("1.1.0");
	public Version minMessagesVersion = new Version("1.0.4");
	public static boolean cancelbroadcast;
	public static boolean debug;
	public static String daLang;
	//private boolean UpdateAviable = false;

	public volatile boolean isCanceled = false;
	public boolean isDSCanceled = false;
	public boolean isSleepInProgress = false;
	public boolean isDaySkipInProgress = false;
	public int transitionTask = -1;
	public int transitionTaskUnrestricted = -1;
	public int dayskipTask = -1;
	public long lastSleepTime = 0; // Timestamp of last sleep event
	public long lastDaySkipTime = 0; // Timestamp of last day skip event
	public long pTime = 0;
	public Map<String, Long> playersCancelled = new HashMap<String, Long>();
	private URL url;
	File langFile;
	FileConfiguration lang;
	Translator lang2;
	public static boolean DisplayCancel;
	public static boolean sleepDisplayCancel;
	public static boolean dayskipDisplayCancel;
	public HashMap<UUID, Long> sleeplimit = new HashMap<UUID, Long>();
	public HashMap<UUID, Long> cancellimit = new HashMap<UUID, Long>();
	public YmlConfiguration config = new YmlConfiguration();
	YamlConfiguration oldconfig = new YamlConfiguration();
	public YmlConfiguration messages = new YmlConfiguration();
	public YamlConfiguration oldMessages;
	public FileConfiguration fileVersions = new YamlConfiguration();
	public File fileVersionsFile;
	public File configFile;
	public File messagesFile;

	public boolean isBloodMoon = false;
	public String jsonColorString = "\"},{\"text\":\"<text>\",\"color\":\"<color>\"},{\"text\":\"";
	public String blacklist_sleep;
	public String blacklist_dayskip;
	boolean colorful_console;
	String pluginName = THIS_NAME;
	public String jarfilename = this.getFile().getAbsoluteFile().toString();
	public static DetailedErrorReporter reporter;
	public String jar_file_name = this.getFile().getAbsoluteFile().toString();
	private final Pattern HEX_COLOR_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})(?![>a-fA-F0-9])");
	// Pattern to match hex color codes in the format §xFFFFFF
	private final Pattern ALT_HEX_COLOR_PATTERN = Pattern.compile("§x([A-Fa-f0-9]{6})");
	// Pattern to match hex color codes in the format §x§F§F§F§F§F§F
	private final Pattern ALT_HEX_COLOR_PATTERN_ALT = Pattern.compile("§x(§[A-Fa-f0-9]){6}");
	public JsonConverter messageConverter;
	public CoreUtils coreUtils = new CoreUtils(this);
	public JsonMessageUtils jsonMessageUtils;
	public final Map<UUID, Player> onlinePlayers = new HashMap<>();
	public long joelTimeOffset = 0L;
	private PluginLogger logger;

	@Override
	public void onLoad() {
		reporter = new DetailedErrorReporter(this);  // ← only thing that truly needs to be early
	}

	@Override // TODO: onEnable
	public void onEnable() {
		long startTime = System.currentTimeMillis();
		UpdateCheck = getConfig().getBoolean("plugin.auto_update_check", true);
		debug = getConfig().getBoolean("plugin.debug", false);
		daLang = getConfig().getString("plugin.lang", "en_US");
		String pluginName = getConfig().getBoolean("global.long_plugin_name", true)
				? THIS_NAME
				: "SPS";
		this.logger = new PluginLogger(pluginName, () -> debug);
		CoreUtils.initLogger(logger);

		sleepDisplayCancel = getConfig().getBoolean("sleep.messages.display_cancel", true);
		dayskipDisplayCancel = getConfig().getBoolean("dayskip.messages.display_cancel", true);
		config = new YmlConfiguration();
		oldconfig = new YamlConfiguration();
		messages = new YmlConfiguration();
		oldMessages = new YamlConfiguration();
		blacklist_sleep = config.getString("blacklist.sleep", "");
		blacklist_dayskip = config.getString("blacklist.dayskip", "");
		colorful_console = getConfig().getBoolean("colorful_console", true);
		THIS_NAME = this.getDescription().getName();
		THIS_VERSION = this.getDescription().getVersion();
		
		lang2 = new Translator(daLang, getDataFolder().toString());
		messageConverter = new JsonConverter(this);
		jsonMessageUtils = coreUtils.jsonMessageUtils;

		CoreUtils.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
		CoreUtils.log(ChatColor.GREEN + "v" + THIS_VERSION + ChatColor.RESET + " Loading...");
		CoreUtils.log("Jar Filename: " + this.getFile().getName());//.getAbsoluteFile());
		CoreUtils.log("Server Version: " + getServer().getVersion());
		// Handle unexpected Minecraft versions
		Version checkVersion = this.verifyMinecraftVersion();

		//** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if (jarfile.toString().contains("-DEV")) {
			CoreUtils.warn(ChatColor.RED + "YOU ARE USING A DEV=BUILD, PLEASE REPORT ANY ISSUES." + ChatColor.RESET);
			CoreUtils.warn(ChatColor.RED + "jarfilename = " + StrUtils.Right(jar_file_name, jar_file_name.length() - jar_file_name.lastIndexOf(File.separatorChar)) + ChatColor.RESET);
			//log("jarfile contains dev, debug set to true.");
		}

		// Make sure directory exists and files exist.
		checkDirectories();
		CoreUtils.log("Loading file version checker...");
		fileVersionsFile = new File(getDataFolder() + "" + File.separatorChar + "fileVersions.yml");
		try {
			fileVersions.load(fileVersionsFile);
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_FILEVERSION).error(exception));
		}
		// Check if Config needs update.
		checkConfig();
		// Check if Messages needs update.
		checkMessages();

		CoreUtils.log("Loading config.yml...");
		configFile = new File(getDataFolder() + "" + File.separatorChar + "config.yml");
		try {
			config.load(configFile);
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
		}

		CoreUtils.log("Loading messages.yml...");
		messagesFile = new File(getDataFolder(), "messages.yml");
		try {
			messages.load(messagesFile);
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_LOADING_MESSAGES_FILE).error(exception));
		}


		//** Update Checker */
		if (UpdateCheck) {
			try {
				CoreUtils.log("Checking for updates...");
				VersionChecker updater = new VersionChecker(this, projectID, githubURL);
				if (updater.checkForUpdates()) {
					//** Update available */
					UpdateAvailable = true; // TODO: Update Checker
					UColdVers = updater.oldVersion();
					UCnewVers = updater.newVersion();

					CoreUtils.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					CoreUtils.log("* " + get("sps.version.message").replace("<MyPlugin>", THIS_NAME));
					CoreUtils.log("* " + get("sps.version.old_vers") + ChatColor.RED + UColdVers);
					CoreUtils.log("* " + get("sps.version.new_vers") + ChatColor.GREEN + UCnewVers);
					CoreUtils.log("*");
					CoreUtils.log("* " + get("sps.version.please_update"));
					CoreUtils.log("*");
					CoreUtils.log("* " + get("sps.version.download") + ": " + DownloadLink + "/history");
					CoreUtils.log("* " + get("sps.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
					CoreUtils.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
				} else {
					//** Up to date */
					CoreUtils.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					CoreUtils.log("* " + get("sps.version.curvers"));
					CoreUtils.log("* " + get("sps.version.donate") + ": https://ko-fi.com/joelgodofwar");
					CoreUtils.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					UpdateAvailable = false;
				}
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_UPDATE_PLUGIN).error(exception));
			}
		} else {
			//** auto_update_check is false so nag. */
			CoreUtils.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
			CoreUtils.log("* " + get("sps.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
			CoreUtils.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
		}
		//** end update checker */

		File file = new File(getDataFolder(), "permissions.yml");
		CoreUtils.log("" + file);
		if (!file.exists()) {
			CoreUtils.log("permissions.yml not found, creating! This is a sample only!");
			saveResource("permissions.yml", true);
		}

		getServer().getPluginManager().registerEvents(new PlayerJoinHandler(this), this);
		getServer().getPluginManager().registerEvents(new PlayerQuitHandler(this), this);
		getServer().getPluginManager().registerEvents(new PlayerBedEnterHandler(this), this);
		getServer().getPluginManager().registerEvents(new PlayerBedLeaveHandler(this), this);
		getServer().getPluginManager().registerEvents(this, this);

		consoleInfo("ENABLED - Loading took " + coreUtils.LoadTime(startTime));
		try {
			//PluginBase plugin = this;
			Metrics metrics = new Metrics(this, 5934);
			// New chart here
			// myPlugins()
			metrics.addCustomChart(new AdvancedPie("my_other_plugins", new Callable<Map<String, Integer>>() {
				@Override
				public Map<String, Integer> call() throws Exception {
					Map<String, Integer> valueMap = new HashMap<>();

					if (getServer().getPluginManager().getPlugin("DragonDropElytra") != null) {
						valueMap.put("DragonDropElytra", 1);
					}
					if (getServer().getPluginManager().getPlugin("NoEndermanGrief") != null) {
						valueMap.put("NoEndermanGrief", 1);
					}
					if (getServer().getPluginManager().getPlugin("PortalHelper") != null) {
						valueMap.put("PortalHelper", 1);
					}
					if (getServer().getPluginManager().getPlugin("ShulkerRespawner") != null) {
						valueMap.put("ShulkerRespawner", 1);
					}
					if (getServer().getPluginManager().getPlugin("MoreMobHeads") != null) {
						valueMap.put("MoreMobHeads", 1);
					}
					if (getServer().getPluginManager().getPlugin("SilenceMobs") != null) {
						valueMap.put("SilenceMobs", 1);
					}
					//if(getServer().getPluginManager().getPlugin("SinglePlayerSleep") != null){valueMap.put("SinglePlayerSleep", 1);}
					if (getServer().getPluginManager().getPlugin("VillagerWorkstationHighlights") != null) {
						valueMap.put("VillagerWorkstationHighlights", 1);
					}
					if (getServer().getPluginManager().getPlugin("RotationalWrench") != null) {
						valueMap.put("RotationalWrench", 1);
					}
					return valueMap;
				}
			}));
			metrics.addCustomChart(new SimplePie("auto_update_check", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return Objects.requireNonNull(config.getString("plugin.auto_update_check")).toUpperCase();
				}
			}));
			// add to site
			metrics.addCustomChart(new SimplePie("unrestrictedsleep", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return Objects.requireNonNull(config.getString("sleep.unrestrictedsleep")).toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("var_waketime", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return Objects.requireNonNull(config.getString("sleep.waketime")).toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("var_sleepdelay", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + config.getInt("sleep.delay");
				}
			}));
			metrics.addCustomChart(new SimplePie("cancelbroadcast", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return Objects.requireNonNull(config.getString("sleep.broadcast_cancel")).toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("var_debug", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return Objects.requireNonNull(config.getString("plugin.debug")).toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("var_lang", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return Objects.requireNonNull(config.getString("plugin.lang")).toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("numberofsleepmsgs", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + config.getInt("numberofsleepmsgs");
				}
			}));
			metrics.addCustomChart(new SimplePie("dayskipdelay", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + config.getInt("dayskip.delay");
				}
			}));
			metrics.addCustomChart(new SimplePie("unrestricteddayskipper", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + config.getBoolean("dayskip.unrestricted");
				}
			}));
			metrics.addCustomChart(new SimplePie("enabledayskipper", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + config.getBoolean("dayskip.enabled");
				}
			}));
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_METRICS_LOAD_ERROR).error(exception));
		}

	}

	@Override // TODO: onDisable
	public void onDisable() {
		fileVersions.set("config", config.getString("version"));
		fileVersions.set("messages", messages.getString("version"));
		try {
			fileVersions.save(fileVersionsFile);
		} catch (IOException exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_SAVE_FILEVERSION).error(exception));
		}

		consoleInfo(ChatColor.RED + "DISABLED");
	}

	public void consoleInfo(String state) {
		//CoreUtils.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
		CoreUtils.log(ChatColor.YELLOW + " v" + THIS_VERSION + ChatColor.RESET + " is " + state + ChatColor.RESET);
		//CoreUtils.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
	}

	public void log(String message, Object... args) {
		CoreUtils.log(message, args);
	}

	public void broadcast(String message, World world) {
		String damsg = "{\"text\":\"broadcastString\"}";
		String msgcolor1 = ChatColorUtils.setColorsByName(config.getString("global.message_color", "WHITE"));
		damsg = damsg.replace("broadcastString", message);
		sendJson(world, damsg, "");
		//SendJsonMessages.SendAllJsonMessage(damsg, "", world);

		//getServer().broadcastMessage("" + message);
	}

	public boolean checkradius(Location player, Location location, int radius) {
		double distance = player.distance(location);
		if (distance <= radius) {
			CoreUtils.debug("truedistance=" + distance);
			return true;
			//shulker.teleport(block.getLocation());
		}
		CoreUtils.debug("falsedistance=" + distance);
		return false;
	}

	public void setDatime(Player player, World world) {
		if (world.hasStorm()) {
			if (Perms.DOWNFALL.hasPermission(player)) {
				world.setStorm(false);
				CoreUtils.debug("sDT " + get("sps.message.setdownfall") + "...");
			} else {
				CoreUtils.debug("sDT " + getNickname(player) + " Does not have permission sps.downfall ...");
			}
		}
		if (world.isThundering()) {
			if (Perms.THUNDER.hasPermission(player)) {
				world.setThundering(false);
				CoreUtils.debug("sDT" + get("sps.message.setthunder") + "...");
			} else {
				CoreUtils.debug("sDT" + getNickname(player) + " Does not have permission sps.thunder ...");
			}
		}
		String waketime = config.getString("sleep.waketime", "NORMAL");
		long timeoffset = 0;
		if (waketime.equalsIgnoreCase("early") || waketime.equalsIgnoreCase("23000")) {
			timeoffset = 1000;
		}
		if (joelTimeOffset != 0L) {
			timeoffset = joelTimeOffset; // 6000 = perfect noon
		}
		long Relative_Time = (24000 - world.getTime()) - timeoffset;
		long daFullTime = world.getFullTime();
		world.setFullTime(daFullTime + Relative_Time);

		CoreUtils.debug(get("sps.message.settime") + "...");
	}

	public void setDStime(Player player, World world) {
		int timeoffset = 10000;
		long Relative_Time = (24000 - world.getTime()) - timeoffset;
		world.setFullTime(world.getFullTime() + Relative_Time);
		CoreUtils.debug("sDSt " + get("sps.message.dayskipsettime") + "...");
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
			if (args[0].equalsIgnoreCase("check")) {
				return null;
			}
		}
		return null;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		try {
			if (command.getName().equalsIgnoreCase("SPS")) {
				if (args.length == 0) {
					sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SinglePlayerSleep" + ChatColor.GREEN + "]===============[]");
					sender.sendMessage(ChatColor.YELLOW + " " + get("sps.message.touse"));//Sleep in a bed to use.");
					sender.sendMessage(ChatColor.WHITE + " ");
					sender.sendMessage(ChatColor.WHITE + " /Sleep - " + get("sps.message.sleephelp"));//subject to server admin approval");
					sender.sendMessage(ChatColor.WHITE + " /spscancel - " + get("sps.command.cancelhelp"));//Cancels SinglePlayerSleep");
					sender.sendMessage(ChatColor.WHITE + " ");
					if (Perms.OP.hasPermissionOrOp(sender)) {
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

				if (args[0].equalsIgnoreCase("UPDATE")) { // TODO: Command update
					if (Perms.UPDATE.hasPermissionOrOp(sender)) {
						return new Cmd_update(this).execute(sender, args);
					}
				}

				if (args[0].equalsIgnoreCase("check")) {
					// /sps check
					// /sps check @p
					// /cmd 0     1
					if (args.length == 1) {
						String damsg = "sps.hermits=" + Perms.HERMITS.hasPermission(sender) + ", " + "sps.cancel=" + Perms.CANCEL.hasPermission(sender) + ", " +
								"sps.unrestricted=" + Perms.UNRESTRICTED.hasPermission(sender) + ", " + "sps.downfall=" + Perms.DOWNFALL.hasPermission(sender) + ", " +
								"sps.thunder=" + Perms.THUNDER.hasPermission(sender) + ", " + "sps.command=" + Perms.COMMAND.hasPermission(sender) + ", " +
								"sps.update=" + Perms.UPDATE.hasPermission(sender) + ", " + "sps.op=" + Perms.OP.hasPermission(sender) + ", " +
								"sps.showUpdateAvailable=" + Perms.SHOW_UPDATE_AVAILABLE.hasPermission(sender) + ", " +
								"sps.dayskipper=" + Perms.DAYSKIPPER.hasPermission(sender) + ", " + "sps.dayskipcommand=" + Perms.DAYSKIPCOMMAND.hasPermission(sender);
						sender.sendMessage(damsg.replace("=true,", "=" + ChatColor.GREEN + "true" + ChatColor.RESET + ",").replace("=false,", "=" + ChatColor.RED + "false" + ChatColor.RESET + ","));
						return true;
					} else if (args.length > 1) {
						if (!(sender instanceof Player) || Perms.OP.hasPermission(sender)) {
							try {
								Player player = Bukkit.getPlayer(args[1]);
								assert player != null;
								String damsg = "Player \"" + player.getName() + "\" has the following permissions: " +
										"sps.hermits=" + Perms.HERMITS.hasPermission(player) + ", " + "sps.cancel=" + Perms.CANCEL.hasPermission(player) + ", " +
										"sps.unrestricted=" + Perms.UNRESTRICTED.hasPermission(player) + ", " + "sps.downfall=" + Perms.DOWNFALL.hasPermission(player) + ", " +
										"sps.thunder=" + Perms.THUNDER.hasPermission(player) + ", " + "sps.command=" + Perms.COMMAND.hasPermission(player) + ", " +
										"sps.update=" + Perms.UPDATE.hasPermission(player) + ", " + "sps.op=" + Perms.OP.hasPermission(player) + ", " +
										"sps.showUpdateAvailable=" + Perms.SHOW_UPDATE_AVAILABLE.hasPermission(player) + ", " +
										"sps.dayskipper=" + Perms.DAYSKIPPER.hasPermission(player) + ", " + "sps.dayskipcommand=" + Perms.DAYSKIPCOMMAND.hasPermission(player);
								sender.sendMessage(damsg.replace("=true,", "=" + ChatColor.GREEN + "true" + ChatColor.RESET + ",").replace("=false,", "=" + ChatColor.RED + "false" + ChatColor.RESET + ","));
								return true;
							} catch (Exception exception) {
								sender.sendMessage("Error Player Not found");
								reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_PLAYER_NOT_FOUND).error(exception));
								return false;
							}
						} else {
							sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.noperm"));
							return false;
						}
					}
				}//*/

				if (args[0].equalsIgnoreCase("toggledebug") || args[0].equalsIgnoreCase("td")) {
					if (Perms.OP.hasPermissionOrOp(sender)) {
						debug = !debug;
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.debugtrue").replace("<boolean>", get("sps.message.boolean." + debug)));
						return true;
					} else {
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.noperm"));
						return false;
					}
				}//*/

				if (args[0].equalsIgnoreCase("reload")) { // TODO: Command Reload
					if (Perms.OP.hasPermissionOrOp(sender)) {
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
						ReloadConfig();
						blacklist_sleep = config.getString("sleep.blacklist", "");
						blacklist_dayskip = config.getString("dayskip.blacklist", "");
						colorful_console = config.getBoolean("global.colorful_console", true);

						lang2 = new Translator(daLang, getDataFolder().toString());
						reporter = new DetailedErrorReporter(this);
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.reloaded"));
					} else if (!Perms.OP.hasPermission(sender)) {
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sps.message.noperm"));
					}
				}//*/


				return true;
			}
			if (command.getName().equalsIgnoreCase("spscancel")) { //command.getName().equalsIgnoreCase("cancel") // TODO: Command spscancel
				return new Cmd_spscancel(this).execute(sender, args);
			}

			if (command.getName().equalsIgnoreCase("sleep")) { // TODO: Command Sleep
				return new Cmd_sleep(this).execute(sender, args);
			}

			if (command.getName().equalsIgnoreCase("dayskip")) { // TODO: Command DaySkip
				return new Cmd_dayskip(this).execute(sender, args);
			}
			if (command.getName().equalsIgnoreCase("spsbloodmoon")) {
				if (sender instanceof ConsoleCommandSender) {
					isBloodMoon = !isBloodMoon;
					CoreUtils.debug("isBloodMoon=" + isBloodMoon);
					return true;
				} else {
					sender.sendMessage("Console only command.");
					return false;
				}
			}//*/

			if (command.getName().equalsIgnoreCase("clearrain")) { // TODO: Command ClearRain
				if (config.getBoolean("clearrain_enabled", false)) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						World world = player.getWorld();
						if (!IsNight(player.getWorld()) && player.getWorld().hasStorm()) {
							world.setStorm(false);
							player.sendMessage("Rain stopped.");
						} else {
							sender.sendMessage("Must not be Night, and a rainstorm must be present");
						}
					} else {
						sender.sendMessage("Must be a player to use this command.");
					}
				} else {
					sender.sendMessage("clearrain is not enabled.");
				}
			}//*/
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.UNHANDLED_COMMAND_ERROR).error(exception));
		}
		CoreUtils.debug(command + Arrays.toString(args) + " returned default.");
		return true;
	}

	public boolean fileContains(String filePath, String searchQuery) throws IOException {
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
		CoreUtils.debug("GN - player.getDisplayName()=" + player.getDisplayName());
		CoreUtils.debug("GN - player.getName()=" + player.getName());
		CoreUtils.debug("GN - usedisplayname=" + config.getBoolean("global.nickname_use_displayname"));

		boolean useDisplayName = config.getBoolean("global.nickname_use_displayname", false);
		String fallbackName = useDisplayName ? player.getDisplayName() : player.getName();

		CompletableFuture<String> nickFuture = new CompletableFuture<>();
		coreUtils.getNicknameAsync(this, player, useDisplayName, nickFuture::complete);

		try {
			String libraryNick = nickFuture.get(400, TimeUnit.MILLISECONDS);
			CoreUtils.debug("GN - Library fetched nick: " + libraryNick);
			return libraryNick; // library already fixed colors
		} catch (TimeoutException e) {
			CoreUtils.debug("GN - Nickname fetch timed out after 400ms for " + player.getName());
		} catch (ExecutionException | InterruptedException e) {
			CoreUtils.debug("GN - Nickname fetch error for " + player.getName(), e);
		}

		CoreUtils.debug("GN - Falling back to: " + fallbackName);
		return fallbackName;
	}

	public String getNickname(CommandSender sender) {
		if (sender instanceof Player) {
			return getNickname((Player) sender);
		}
		return "Console";
	}

	public void sendPermJson(String string, String perm) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (Perms.SHOW_CANCELLED_MSG.hasPermission(player)) {
				sendJsonString(player, string);
			}
		}
	}

	public void sendPermJson(World world, String string, String perm) {
		for (Player player : world.getPlayers()) {
			if (Perms.SHOW_CANCELLED_MSG.hasPermission(player)) {
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
	 * @param sleep_msg The JSON-formatted sleep message to be sent to all players.
	 * @param can_msg   The JSON-formatted cancel message to be sent to players with "sps.cancel" permission.
	 */
	public void sendJson(String sleep_msg, String can_msg) {
		int i = 0;
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (Perms.CANCEL.hasPermission(player) && DisplayCancel) {
				// Combine JSON arrays
				String fullMessage = "[" + sleep_msg.substring(1, sleep_msg.length() - 1) + ",{\"text\":\" \"},"
						+ can_msg.substring(1, can_msg.length() - 1) + "]";
				jsonMessageUtils.sendJsonMessage(player, fullMessage);
				CoreUtils.debug("SAJM - fullMessage=" + fullMessage);
				if (i == 0) {
					CoreUtils.debug("SAJM - fullMessageContent=" + fullMessage.replace("\\", ""));
				}
				CoreUtils.debug("SAJM - perm & display - Broadcast");
			} else {
				jsonMessageUtils.sendJsonMessage(player, sleep_msg);
				CoreUtils.debug("SAJM - sleep_msg=" + sleep_msg);
				if (i == 0) {
					CoreUtils.debug("SAJM - sleepMessageContent=" + sleep_msg.replace("\\", ""));
				}
				CoreUtils.debug("SAJM - !perm & display - Broadcast");
			}
			i++;
		}
	}

	/**
	 * Sends JSON-formatted messages to all players in the specified world.
	 * Players with the "sps.cancel" permission receive a combined message of sleep_msg and can_msg,
	 * while others receive only the sleep_msg. The method logs debug information for the messages sent.
	 *
	 * @param world     The world containing the players to send messages to.
	 * @param sleep_msg The JSON-formatted sleep message to be sent to all players.
	 * @param can_msg   The JSON-formatted cancel message to be sent to players with "sps.cancel" permission.
	 */
	public void sendJson(World world, String sleep_msg, String can_msg) {
		int i = 0;
		for (Player player : world.getPlayers()) {
			if (player.hasPermission("sps.cancel") && DisplayCancel) {
				// Combine JSON arrays
				String fullMessage = "[" + sleep_msg.substring(1, sleep_msg.length() - 1) + ",{\"text\":\" \"},"
						+ can_msg.substring(1, can_msg.length() - 1) + "]";
				jsonMessageUtils.sendJsonMessage(player, fullMessage);
				CoreUtils.debug("WS SAJM - fullMessage=" + fullMessage);
				if (i == 0) {
					CoreUtils.debug("WS SAJM - fullMessageContent=" + fullMessage.replace("\\", ""));
				}
				CoreUtils.debug("WS SAJM - perm & display - Broadcast");
			} else {
				jsonMessageUtils.sendJsonMessage(player, sleep_msg);
				CoreUtils.debug("WS SAJM - sleep_msg=" + sleep_msg);
				if (i == 0) {
					CoreUtils.debug("WS SAJM - sleepMessageContent=" + sleep_msg.replace("\\", ""));
				}
				CoreUtils.debug("WS SAJM - !perm & display - Broadcast");
			}
			i++;
		}
	}

	public void resetPlayersRestStat(World world) {
		try {
			if (config.getBoolean("sleep.reset_insomnia", false)) {
				List<Player> players = world.getPlayers();
				for (Player player : players) {
					if (player.getStatistic(Statistic.TIME_SINCE_REST) > 0) {
						player.setStatistic(Statistic.TIME_SINCE_REST, 0);
					}
				}
			}
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_RESET_INSOMNIA).error(exception));
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
				CoreUtils.warn("Version " + CURRENT_MINECRAFT_VERSION + " is lower than the minimum " + MINIMUM_MINECRAFT_VERSION);
			}
			if (CURRENT_MINECRAFT_VERSION.compareTo(MAXIMUM_MINECRAFT_VERSION) > 0) {
				CoreUtils.warn("Version " + CURRENT_MINECRAFT_VERSION + " has not yet been tested! Proceed with caution.");
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

	public boolean isBed(Block block) {
		Material mat = block.getType();
		if (block instanceof Bed) {
			return true;
		}
		switch (mat) {
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
		try {
			if (!getDataFolder().exists()) {
				CoreUtils.log("Data Folder doesn't exist");
				CoreUtils.log("Creating Data Folder");
				boolean theResult = getDataFolder().mkdirs();
				CoreUtils.log("Data Folder Created at " + getDataFolder());
			}
			File file = new File(getDataFolder(), "config.yml");
			if (!file.exists()) {
				CoreUtils.log("config.yml not found, creating!");
				saveResource("config.yml", true);
				saveResource("config_comments.yml", true);
			}
			file = new File(getDataFolder(), "messages.yml");
			if (!file.exists()) {
				CoreUtils.log("messages.yml not found, creating!");
				saveResource("messages.yml", true);
			}
			file = new File(getDataFolder(), "fileVersions.yml");
			if (!file.exists()) {
				CoreUtils.log("fileVersions.yml not found, creating!");
				saveResource("fileVersions.yml", true);
			}
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_CHECK_CONFIG).error(exception));
		}
	}

	public void checkConfig() {
		File configFile = new File(getDataFolder(), "config.yml");
		File commentsFile = new File(getDataFolder(), "config_comments.yml");
		File backupDir = new File(getDataFolder(), "backup");
		if (!backupDir.exists()) backupDir.mkdirs();
		File backupConfig = new File(backupDir, "config.yml");
		File backupComments = new File(backupDir, "config_comments.yml");

		String currentVersionStr = getConfig().getString("version", "0.0.1");
		Version currentVersion = new Version(currentVersionStr);

		if (currentVersion.equals(minConfigVersion) && configFile.exists()) {
			CoreUtils.log("config.yml is current (v" + currentVersion + ") — loading...");
			ReloadConfig();
			return;
		}

		CoreUtils.log("config.yml is outdated/missing (v" + currentVersion + " ≠ v" + minConfigVersion + ") — backing up and updating...");

		// 1. Backup current files (overwrite existing backup files)
		if (configFile.exists()) {
			try {
				FileUtils.copyFile(configFile.getAbsolutePath(), backupConfig.getAbsolutePath());
				CoreUtils.log("Backed up current config.yml to backup/config.yml");
			} catch (Exception e) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_COPY_FILE).error(e));
			}
		}


		// 2. Overwrite live files with fresh JAR versions
		CoreUtils.log("Saving fresh config.yml and config_comments.yml from JAR...");
		saveResource("config.yml", true);
		saveResource("config_comments.yml", true);

		// 3. Reload the fresh configs
		ReloadConfig();

		// 4. Load the backup we just made
		YmlConfiguration oldCfg = new YmlConfiguration();
		boolean isOldFormat = false;
		try {
			oldCfg.load(backupConfig);
			isOldFormat = !oldCfg.contains("plugin.debug");
		} catch (Exception e) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(e));
			// If backup load fails, assume old format and skip migration
			return;
		}

		// 5. Decide what to do with the backup
		if (isOldFormat) {
			CoreUtils.log("Backup config is old format (no plugin.debug) — migrating to new structure...");
			migrateOldToNewConfig(oldCfg, config);
		} else {
			CoreUtils.log("Backup config already has new format — copying settings from backup...");
			copyNewToNewConfig(oldCfg, config);
		}

		// 6. Save the migrated/updated config
		try {
			config.save(configFile);
			CoreUtils.log("Config updated and saved.");
		} catch (Exception e) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_SAVE_CONFIG).error(e));
		}

		// Final reload
		ReloadConfig();

	}

	public void ReloadConfig() {
		File configFile = new File(getDataFolder(), "config.yml");

		// If file missing, save default from JAR
		if (!configFile.exists()) {
			CoreUtils.log("config.yml missing — saving default from JAR...");
			saveResource("config.yml", true);
			saveResource("config_comments.yml", true);
		}
		// Always create a new instance
		config = new YmlConfiguration();

		try {
			config.load(configFile);
			CoreUtils.debug("config.yml reloaded successfully");
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
			// Fallback: use an empty config or defaults
			config = new YmlConfiguration();

		}
	}

	private void migrateOldToNewConfig(FileConfiguration oldCfg, FileConfiguration newCfg) {
		boolean changed = false;

		// plugin
		if (oldCfg.isSet("auto_update_check")) newCfg.set("plugin.auto_update_check", oldCfg.getBoolean("auto_update_check", true));
		if (oldCfg.isSet("debug"))             newCfg.set("plugin.debug", oldCfg.getBoolean("debug", false));
		if (oldCfg.isSet("lang"))              newCfg.set("plugin.lang", oldCfg.getString("lang", "en_US"));

		// global
		if (oldCfg.isSet("colorful_console"))           newCfg.set("global.colorful_console", oldCfg.getBoolean("colorful_console", true));
		if (oldCfg.isSet("console.longpluginname"))     newCfg.set("global.long_plugin_name", oldCfg.getBoolean("console.longpluginname", true));
		if (oldCfg.isSet("broadcast_per_world"))        newCfg.set("global.broadcast_per_world", oldCfg.getBoolean("broadcast_per_world", true));
		if (oldCfg.isSet("ignoreVanishedPlayers"))      newCfg.set("global.ignore_vanished", oldCfg.getBoolean("ignoreVanishedPlayers", true));
		if (oldCfg.isSet("randomsleepmsgs"))            newCfg.set("global.random_messages", oldCfg.getBoolean("randomsleepmsgs", true));
		if (oldCfg.isSet("cancelcolor"))                newCfg.set("global.cancel_color", oldCfg.getString("cancelcolor", "RED"));
		if (oldCfg.isSet("cancelbracketcolor"))         newCfg.set("global.cancel_bracket_color", oldCfg.getString("cancelbracketcolor", "YELLOW"));
		if (oldCfg.isSet("sleepmsgcolor") || oldCfg.isSet("skip_color")) {
			String color = oldCfg.getString("sleepmsgcolor", oldCfg.getString("skip_color", "WHITE"));
			newCfg.set("global.message_color", color);
		}
		if (oldCfg.isSet("playernamecolor"))            newCfg.set("global.player_name_color", oldCfg.getString("playernamecolor", "WHITE"));
		if (oldCfg.isSet("nickname.usedisplayname"))    newCfg.set("global.nickname_use_displayname", oldCfg.getBoolean("nickname.usedisplayname", true));

		// limits
		if (oldCfg.isSet("sleeplimit")) {
			long val = oldCfg.getLong("sleeplimit", 30);
			newCfg.set("global.limits.use_cooldown", val);
			newCfg.set("global.limits.cancel_cooldown", oldCfg.getLong("cancellimit", val));
		}

		// sleep
		if (oldCfg.isSet("autoFixSleepingPercentage"))  newCfg.set("sleep.auto_fix_sleeping_percentage", oldCfg.getBoolean("autoFixSleepingPercentage", true));
		if (oldCfg.isSet("blacklist.sleep"))            newCfg.set("sleep.blacklist", oldCfg.getString("blacklist.sleep"));
		if (oldCfg.isSet("sleepdelay"))                 newCfg.set("sleep.delay", oldCfg.getInt("sleepdelay", 10));
		if (oldCfg.isSet("unrestrictedsleep"))          newCfg.set("sleep.unrestricted", oldCfg.getBoolean("unrestrictedsleep", false));
		if (oldCfg.isSet("waketime"))                   newCfg.set("sleep.waketime", oldCfg.getString("waketime", "NORMAL"));
		if (oldCfg.isSet("reset_insomnia"))             newCfg.set("sleep.reset_insomnia", oldCfg.getBoolean("reset_insomnia", false));
		if (oldCfg.isSet("clearrain_enabled"))          newCfg.set("sleep.clear_rain", oldCfg.getBoolean("clearrain_enabled", false));
		if (oldCfg.isSet("notifymustbenight"))          newCfg.set("sleep.notify_must_be_night", oldCfg.getBoolean("notifymustbenight", false));
		if (oldCfg.isSet("display_cancel"))             newCfg.set("sleep.messages.display_cancel", oldCfg.getBoolean("display_cancel", true));
		if (oldCfg.isSet("cancelbroadcast"))            newCfg.set("sleep.messages.broadcast_cancel", oldCfg.getBoolean("cancelbroadcast", true));
		if (oldCfg.isSet("exitbedcancel"))              newCfg.set("sleep.messages.exit_bed_cancels", oldCfg.getBoolean("exitbedcancel", false));

		// dayskip
		if (oldCfg.isSet("enabledayskipper"))           newCfg.set("dayskip.enabled", oldCfg.getBoolean("enabledayskipper", false));
		if (oldCfg.isSet("blacklist.dayskip"))          newCfg.set("dayskip.blacklist", oldCfg.getString("blacklist.dayskip"));
		if (oldCfg.isSet("dayskipdelay"))               newCfg.set("dayskip.delay", oldCfg.getInt("dayskipdelay", 10));
		if (oldCfg.isSet("unrestricteddayskipper"))     newCfg.set("dayskip.unrestricted", oldCfg.getBoolean("unrestricteddayskipper", false));
		if (oldCfg.isSet("dayskipperitemrequired"))     newCfg.set("dayskip.item_required", oldCfg.getBoolean("dayskipperitemrequired", true));
		if (oldCfg.isSet("display_cancel"))             newCfg.set("dayskip.messages.display_cancel", oldCfg.getBoolean("display_cancel", true));
		if (oldCfg.isSet("cancelbroadcast"))            newCfg.set("dayskip.messages.broadcast_cancel", oldCfg.getBoolean("cancelbroadcast", true));

		// Optional: set version explicitly after migration
		newCfg.set("version", "1.1.0");
	}

	private void copyNewToNewConfig(FileConfiguration oldCfg, FileConfiguration newCfg) {
		// Only copy if value exists in backup (oldCfg) and is different from new defaults
		// This is useful if someone manually edited the old file after migration

		// plugin
		if (oldCfg.isSet("plugin.auto_update_check")) newCfg.set("plugin.auto_update_check", oldCfg.get("plugin.auto_update_check"));
		if (oldCfg.isSet("plugin.debug"))             newCfg.set("plugin.debug", oldCfg.get("plugin.debug"));
		if (oldCfg.isSet("plugin.lang"))              newCfg.set("plugin.lang", oldCfg.get("plugin.lang"));

		// global
		if (oldCfg.isSet("global.colorful_console"))           newCfg.set("global.colorful_console", oldCfg.get("global.colorful_console"));
		if (oldCfg.isSet("global.long_plugin_name"))           newCfg.set("global.long_plugin_name", oldCfg.get("global.long_plugin_name"));
		if (oldCfg.isSet("global.console_default_world"))      newCfg.set("global.console_default_world", oldCfg.get("global.console_default_world"));
		if (oldCfg.isSet("global.random_messages"))            newCfg.set("global.random_messages", oldCfg.get("global.random_messages"));
		if (oldCfg.isSet("global.broadcast_per_world"))        newCfg.set("global.broadcast_per_world", oldCfg.get("global.broadcast_per_world"));
		if (oldCfg.isSet("global.ignore_vanished"))            newCfg.set("global.ignore_vanished", oldCfg.get("global.ignore_vanished"));
		if (oldCfg.isSet("global.message_color"))              newCfg.set("global.message_color", oldCfg.get("global.message_color"));
		if (oldCfg.isSet("global.player_name_color"))          newCfg.set("global.player_name_color", oldCfg.get("global.player_name_color"));
		if (oldCfg.isSet("global.cancel_color"))               newCfg.set("global.cancel_color", oldCfg.get("global.cancel_color"));
		if (oldCfg.isSet("global.cancel_bracket_color"))       newCfg.set("global.cancel_bracket_color", oldCfg.get("global.cancel_bracket_color"));
		if (oldCfg.isSet("global.limits.use_cooldown"))        newCfg.set("global.limits.use_cooldown", oldCfg.get("global.limits.use_cooldown"));
		if (oldCfg.isSet("global.limits.cancel_cooldown"))     newCfg.set("global.limits.cancel_cooldown", oldCfg.get("global.limits.cancel_cooldown"));
		if (oldCfg.isSet("global.nickname_use_displayname"))   newCfg.set("global.nickname_use_displayname", oldCfg.get("global.nickname_use_displayname"));

		// sleep & dayskip (copy any custom values)
		if (oldCfg.isSet("sleep")) {
			for (String key : oldCfg.getConfigurationSection("sleep").getKeys(true)) {
				newCfg.set("sleep." + key, oldCfg.get("sleep." + key));
			}
		}
		if (oldCfg.isSet("dayskip")) {
			for (String key : oldCfg.getConfigurationSection("dayskip").getKeys(true)) {
				newCfg.set("dayskip." + key, oldCfg.get("dayskip." + key));
			}
		}
	}

	public void checkMessages() {
		// Message file check
		Version curMessagesVersion = new Version(fileVersions.getString("messages", "0.0.1"));
		if(curMessagesVersion.compareTo(minMessagesVersion) < 0) {
			CoreUtils.log("messages.yml is outdated backing up...");
			try {
				FileUtils.copyFile(getDataFolder() + "" + File.separatorChar + "messages.yml", getDataFolder() + "" + File.separatorChar + "backup" + File.separatorChar + "messages.yml");
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_MESSAGES_COPY_ERROR).error(exception));
			}
			CoreUtils.log("Saving new messages.yml...");
			saveResource("messages.yml", true);
			CoreUtils.log("Copying values from backup" + File.separatorChar + "messages.yml...");

			try {
				updateMessages(new File(getDataFolder() + "" + File.separatorChar + "backup" + File.separatorChar + "messages.yml"),
						new File(getDataFolder() + "" + File.separatorChar + "messages.yml"), "sleep");
				updateMessages(new File(getDataFolder() + "" + File.separatorChar + "backup" + File.separatorChar + "messages.yml"),
						new File(getDataFolder() + "" + File.separatorChar + "messages.yml"), "sleep_canceled");
				updateMessages(new File(getDataFolder() + "" + File.separatorChar + "backup" + File.separatorChar + "messages.yml"),
						new File(getDataFolder() + "" + File.separatorChar + "messages.yml"), "dayskip");
				updateMessages(new File(getDataFolder() + "" + File.separatorChar + "backup" + File.separatorChar + "messages.yml"),
						new File(getDataFolder() + "" + File.separatorChar + "messages.yml"), "dayskip_canceled");
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_MESSAGES_LOAD_ERROR).error(exception));
			}

            messages = new YmlConfiguration();
			oldMessages = null;
			CoreUtils.log("Update complete messages.yml...");
		}
		CoreUtils.log("Loading messages file...");
		try {
			messages.load(new File(getDataFolder() + "" + File.separatorChar + "messages.yml"));
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_MESSAGES_LOAD_ERROR).error(exception));
		}
	}

	public void updateMessages(File oldFile, File newFile, String sectionName) {
		FileConfiguration oldConfig = null;
		FileConfiguration newConfig = null;
		FileConfiguration defaultConfig = null;

		try {
			oldConfig = YmlConfiguration.loadConfiguration(oldFile);
			newConfig = YmlConfiguration.loadConfiguration(newFile);

			String path = "messages." + sectionName;
			ConfigurationSection oldSection = oldConfig.getConfigurationSection(path);
			ConfigurationSection newSection = newConfig.getConfigurationSection(path);

			if (oldSection == null || newSection == null) {
				return;
			}

			int preservedCount = 0;
			int addedDefaultCount = 0;

			// 1. Collect ALL user's original messages (preserve exact text + order)
			List<String> userMessages = new ArrayList<>();
			for (String key : oldSection.getKeys(false)) {
				if (key.startsWith("message_")) {
					String msg = oldSection.getString(key);
					if (msg != null && !msg.isEmpty()) {
						userMessages.add(msg);
					}
				}
			}
			preservedCount = userMessages.size();

			// 2. Normalized set for duplicate detection
			Set<String> userNormalized = userMessages.stream()
					.map(coreUtils::fixColors)
					.collect(Collectors.toCollection(LinkedHashSet::new));

			// 3. Clear the current section in new file
			newSection.getKeys(false).forEach(k -> newSection.set(k, null));

			int nextIndex = 1;

			// 4. Add back ALL user's messages first
			for (String msg : userMessages) {
				newSection.set("message_" + nextIndex, msg);
				nextIndex++;
			}

			// 5. Load fresh defaults and append any that aren't duplicates
			try {
				defaultConfig = YmlConfiguration.loadConfiguration(newFile); // fresh copy of new defaults
				ConfigurationSection defaultSection = defaultConfig.getConfigurationSection(path);

				if (defaultSection != null) {
					for (String key : defaultSection.getKeys(false)) {
						if (key.startsWith("message_")) {
							String msg = defaultSection.getString(key);
							if (msg != null && !msg.isEmpty()) {
								String normalized = coreUtils.fixColors(msg);
								if (!userNormalized.contains(normalized)) {
									newSection.set("message_" + nextIndex, msg);
									nextIndex++;
									addedDefaultCount++;
									CoreUtils.debug("Appended new default " + sectionName + " message: " + msg);
								} else {
									CoreUtils.debug("Skipped duplicate default " + sectionName + " message: " + msg);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_MESSAGES_LOAD_ERROR).error(e));
			}

			// 6. Update count
			newSection.set("count", nextIndex - 1);

			// 7. Save with safety
			try {
				newConfig.save(newFile);
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_OLDMESSAGES_SAVE_ERROR).error(exception));
			}

			// 8. Summary log
			CoreUtils.log("Messages update for '" + sectionName + "': Preserved " + preservedCount +
					" user message" + (preservedCount == 1 ? "" : "s") +
					", added " + addedDefaultCount + " new default" + (addedDefaultCount == 1 ? "" : "s"));

		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_MESSAGES_LOAD_ERROR).error(exception));
		}
	}

	/**
	 * Checks if a player is vanished by comparing Bukkit's online players list
	 * with the plugin's tracked players.
	 *
	 * @param player The player to check.
	 * @return True if the player is vanished, false otherwise.
	 */
	public boolean isVanished(Player player) {
		boolean isVanished = !Bukkit.getOnlinePlayers().contains(player) && onlinePlayers.containsValue(player);
		if (isVanished) {
			CoreUtils.debug(player.getName() + " is vanished (not in Bukkit.getOnlinePlayers).");
		}
		return isVanished;
	}

	public synchronized boolean isSleepInProgress() {
		return isSleepInProgress;
	}

	public synchronized void setSleepInProgress(boolean inProgress) {
		this.isSleepInProgress = inProgress;
		if (inProgress) {
			this.lastSleepTime = System.currentTimeMillis() / 1000; // Update timestamp
		}
	}

	public synchronized boolean isDaySkipInProgress() {
		return isDaySkipInProgress;
	}

	public synchronized void setDaySkipInProgress(boolean inProgress) {
		this.isDaySkipInProgress = inProgress;
		if (inProgress) {
			this.lastDaySkipTime = System.currentTimeMillis() / 1000; // Update timestamp
		}
	}

	public synchronized int getTransitionTask() {
		return transitionTask;
	}

	public synchronized void setTransitionTask(int task) {
		this.transitionTask = task;
	}

	public synchronized int getTransitionTaskUnrestricted() {
		return transitionTaskUnrestricted;
	}

	public synchronized void setTransitionTaskUnrestricted(int task) {
		this.transitionTaskUnrestricted = task;
	}

	public synchronized int getDaySkipTask() {
		return dayskipTask;
	}

	public synchronized void setDaySkipTask(int task) {
		this.dayskipTask = task;
	}

	public synchronized long getLastSleepTime() {
		return lastSleepTime;
	}

	public synchronized long getLastDaySkipTime() {
		return lastDaySkipTime;
	}

	public synchronized void cancelSleepTasks() {
		if (transitionTask != -1) {
			getServer().getScheduler().cancelTask(transitionTask);
			setTransitionTask(-1);
			setSleepInProgress(false);
		}
		if (transitionTaskUnrestricted != -1) {
			getServer().getScheduler().cancelTask(transitionTaskUnrestricted);
			setTransitionTaskUnrestricted(-1);
			setSleepInProgress(false);
		}
		if (dayskipTask != -1) {
			getServer().getScheduler().cancelTask(dayskipTask);
			setDaySkipTask(-1);
			setDaySkipInProgress(false);
		}
	}

	public synchronized boolean isSleepTaskStale() {
		if (lastSleepTime == 0) {
			// Never properly started — treat as stale so we can recover
			CoreUtils.debug("iSTS: lastSleepTime == 0 → treating as stale");
			return true;
		}
		long currentTime = System.currentTimeMillis() / 1000;
		long sleepdelay = config.getLong("sleepdelay", 10) + 1; // +1 for safety
		boolean result = (currentTime - lastSleepTime) > sleepdelay;
		CoreUtils.debug("iSTS: current=" + currentTime +
				", last=" + lastSleepTime +
				", delay=" + sleepdelay +
				" → elapsed=" + (currentTime - lastSleepTime) +
				" > " + sleepdelay + " = " + result);
		return result;
	}

	public synchronized boolean isDaySkipTaskStale() {
		if (lastDaySkipTime == 0) {
			CoreUtils.debug("iDSTS: lastDaySkipTime == 0 → treating as stale");
			return true;
		}
		long currentTime = System.currentTimeMillis() / 1000;
		long dayskipdelay = config.getLong("dayskipdelay", 10) + 1;
		boolean result = (currentTime - lastDaySkipTime) > dayskipdelay;
		CoreUtils.debug("iSTS: current=" + currentTime +
				", last=" + lastSleepTime +
				", delay=" + dayskipdelay +
				" → elapsed=" + (currentTime - lastSleepTime) +
				" > " + dayskipdelay + " = " + result);
		return result;
	}
}
