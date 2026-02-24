package com.github.joelgodofwar.sps.commands;

import com.github.joelgodofwar.sps.enums.Perms;
import lib.github.joelgodofwar.coreutils.CoreUtils;
import lib.github.joelgodofwar.coreutils.util.JsonConverter;
import lib.github.joelgodofwar.coreutils.util.StrUtils;
import lib.github.joelgodofwar.coreutils.util.YmlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.joelgodofwar.sps.SinglePlayerSleep;
import lib.github.joelgodofwar.coreutils.util.ChatColorUtils;
import com.github.joelgodofwar.sps.common.PluginLibrary;

import com.github.joelgodofwar.sps.common.error.Report;

import static com.github.joelgodofwar.sps.util.SPSUtil.*;


public class Cmd_dayskip {
	private static SinglePlayerSleep plugin;
	public static String THIS_NAME;
	public static  String THIS_VERSION;
	private final YmlConfiguration messages;
	private final CoreUtils coreUtils;
	private final JsonConverter messageConverter;

	@SuppressWarnings("static-access")
	public Cmd_dayskip(SinglePlayerSleep plugin) {
		this.plugin = plugin.getInstance();
		this.THIS_NAME = plugin.THIS_NAME;
		this.THIS_VERSION = plugin.getDescription().getVersion();
		this.messages = plugin.messages; // Assuming messages is a YmlConfiguration field in SinglePlayerSleep
		this.coreUtils = plugin.coreUtils; // Assuming coreUtils is a field in SinglePlayerSleep
		this.messageConverter = plugin.messageConverter;
	}

	public static String get(String key, String... defaultValue) {
		return plugin.get(key, defaultValue);
	}

	@SuppressWarnings({ "static-access" })
	public boolean execute(CommandSender sender, String[] args) {
		try {
			if(plugin.config.getBoolean("dayskip.enabled", false)){
				if (!Perms.DAYSKIPCOMMAND.hasPermissionOrOp(sender)) {
					sender.sendMessage(ChatColor.RED + get("sps.message.noperm"));
					return false;
				}
				final boolean isPlayer = sender instanceof Player;
				Player player = isPlayer ? (Player) sender : null;
				World world = null;

				final CommandSender daSender = sender;
				if( isPlayer ){
					world = player.getWorld();

					if( !IsDay(world) ){
						sender.sendMessage(ChatColorUtils.setColors(get("sps.message.mustbeday")));
						return false;
					}
					//Set default timer for when the player has never slept before
					long timer = 0;
					CoreUtils.debug("DS " + player.getName() + " is sleeping.");
					long time = System.currentTimeMillis() / 1000;
					if(plugin.sleeplimit.get(player.getUniqueId()) == null){
						CoreUtils.debug("DS null - player not in sleeplimit");
						// Check if player has sps.unrestricted
						if (!Perms.UNRESTRICTED.hasPermission(player)) {
							// Set player's time in HashMap
							plugin.sleeplimit.put(player.getUniqueId(), time);
							CoreUtils.debug("DS " + player.getName() + " added to playersSlept");
						}
					}else{
						CoreUtils.debug("DS not null - player in sleeplimit");
						// Player is on the list.
						timer = plugin.sleeplimit.get(player.getUniqueId());
						CoreUtils.debug("DS time=" + time);
						CoreUtils.debug("DS timer=" + timer);
						CoreUtils.debug("DS time - timer=" +  (time - timer));
						CoreUtils.debug("DS use_cooldown=" + plugin.config.getLong("global.limits.use_cooldown", 30));
						// if !time - timer > limit
						if(!((time - timer) > plugin.config.getLong("global.limits.use_cooldown", 30))){
							long length = plugin.config.getLong("global.limits.use_cooldown", 30) - (time - timer) ;
							String sleeplimit = get("sps.message.sleeplimit").replace("<length>", "" + length);
							player.sendMessage(ChatColor.YELLOW + sleeplimit);
							CoreUtils.debug("DS use_cooldown: " + sleeplimit);
							//player.sendMessage("You can not do that for " + length + " seconds");

							return false;
						}else if((time - timer) > plugin.config.getLong("global.limits.use_cooldown", 30)){
							CoreUtils.debug("DS time - timer > use_cooldown");
							plugin.sleeplimit.replace(player.getUniqueId(), time);
						}
					}
				}else{
					// Console sender
					String targetWorldName;

					if (args.length >= 1) {
						// Argument provided → use it (case-insensitive match)
						String arg = args[0].trim();
						targetWorldName = null;

						for (World w : Bukkit.getWorlds()) {
							if (w.getName().equalsIgnoreCase(arg)) {
								targetWorldName = w.getName();
								world = w;
								break;
							}
						}

						if (targetWorldName == null) {
							// Argument world not found → fall back to config
							String configWorld = plugin.config.getString("global.console_default_world", "world");
							sender.sendMessage(ChatColor.YELLOW + "World '" + arg + "' not found. Using configured default: " + configWorld);

							for (World w : Bukkit.getWorlds()) {
								if (w.getName().equalsIgnoreCase(configWorld)) {
									targetWorldName = w.getName();
									world = w;
									break;
								}
							}
						}
					} else {
						// No argument → use config default
						targetWorldName = plugin.config.getString("global.console_default_world", "world");
					}

					// Final fallback if config world is invalid/missing
					if (targetWorldName != null) {
						for (World w : Bukkit.getWorlds()) {
							if (w.getName().equalsIgnoreCase(targetWorldName)) {
								world = w;
								break;
							}
						}
					}

					// Ultimate fallback: first loaded world
					if (world == null) {
						world = Bukkit.getWorlds().get(0);
						sender.sendMessage(ChatColor.YELLOW + "Warning: Configured default world '"
								+ targetWorldName + "' not found. Using first loaded world: " + world.getName());
					}

					if( !IsDay(world) ){
						sender.sendMessage(ChatColorUtils.setColors(get("sps.message.mustbeday")));
						return false;
					}
				}
				if(!Perms.OP.hasPermission(sender)){ // TODO: DaySkip Command Blacklist Check
					if((plugin.blacklist_dayskip != null)&&!plugin.blacklist_dayskip.isEmpty()){
						if(StrUtils.stringContains(plugin.blacklist_dayskip, world.getName())){
							CoreUtils.log("DS - World - On blacklist.");
							sender.sendMessage(ChatColor.RED + "Dayskip is disabled in this world.");
							return false;
						}
					}
				}
				
				CoreUtils.debug("DS Has perm or is op. ...");

				// Colors from config
				String cancelBracketColor = plugin.config.getString("global.cancel_bracket_color", "YELLOW").toLowerCase();
				CoreUtils.debug("DS ... CancelBracketColor=<" + cancelBracketColor + ">");
				String cancelcolor = plugin.config.getString("global.cancel_color", "RED").toLowerCase();
				CoreUtils.debug("DS ... cancelcolor=<" + cancelcolor + ">");

				// Convert config colors to JSON-compatible colors
				String jsonCancelBracketColor = messageConverter.convertToJsonColor(cancelBracketColor);
				String jsonCancelColor = messageConverter.convertToJsonColor(cancelcolor);

				// Construct a sleep message
				String skipmsg;
				if ( plugin.config.getBoolean("global.random_messages") ) {
					int maxmsgs = messages.getInt("messages.dayskip.count");
					int randomnumber = RandomNumber(maxmsgs);
					CoreUtils.debug("DS ... maxmsgs=" + maxmsgs);
					CoreUtils.debug("DS ... randomnumber=" + randomnumber);
					if (randomnumber != 0) {
						skipmsg = messages.getString("messages.dayskip.message_" + randomnumber, "<#FFFFFF><player> <gradient:#000000:#FFFF00>wants to sleep the day away</gradient>...");
					} else {
						skipmsg = "<#FFFFFF><player> <gradient:#FF0000:#FFFFFF>error selecting dayskip message</gradient>...";
					}
					skipmsg = skipmsg.replace("<colon>", ":");
				} else {
					skipmsg = messages.getString("messages.dayskip.message_2", "<#FFFFFF><player> <gradient:#000000:#FFFF00>wants to sleep the day away</gradient>...");
					CoreUtils.debug("DS ... randomskipmsgs=false");
				}
				CoreUtils.debug("DS skipmsg=" + skipmsg);

				// Fix old hex codes
				skipmsg = coreUtils.fixColors(skipmsg);
				CoreUtils.debug("DS fixHexCodes sleepmsg=" + skipmsg);

				// Nickname parser
				String nickName = plugin.getNickname(sender);
				if (nickName.contains("§") || nickName.contains("&")) {
					nickName = coreUtils.fixColors(nickName);
					CoreUtils.debug("DS nick contains § or &");
					CoreUtils.debug("DS nickName AfterParse = " + nickName);
				} else {
					CoreUtils.debug("DS nickName !contain § or &");
				}

				// Convert to JSON
				String jsonSkipmsg = messageConverter.convert(skipmsg, nickName);
				CoreUtils.debug("DS jsonSleepmsg=" + jsonSkipmsg);

				// Construct cancel message as JSON
				String jsonCanmsg = "[{\"text\":\"[\",\"color\":\"" + jsonCancelBracketColor + "\"}," +
						"{\"text\":\"" + plugin.get("sps.message.cancel") + "\",\"color\":\"" + jsonCancelColor + "\"," +
						"\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"}," +
						"\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"" + plugin.get("sps.message.dayskipclickcancel", "Click to cancel dayskip") + "\"}}," +
						"{\"text\":\"]\",\"color\":\"" + jsonCancelBracketColor + "\"}]";
				CoreUtils.debug("DS jsonCanmsg=" + jsonCanmsg);

				// Broadcast message
				plugin.DisplayCancel = plugin.dayskipDisplayCancel;
				if ( plugin.config.getBoolean("global.broadcast_per_world", true) ) {
					if(!(isPlayer)){
						plugin.sendJson(jsonSkipmsg, jsonCanmsg);
					}else {
						plugin.sendJson(player.getWorld(), jsonSkipmsg, jsonCanmsg);
					}
				} else {
					plugin.sendJson(jsonSkipmsg, jsonCanmsg);
				}
				CoreUtils.debug("DS SendAllJsonMessage.");
				
				if(!plugin.isDSCanceled){
					//final World world = worlds.get(0);
					int dayskipdelay = plugin.config.getInt("dayskip.delay", 10);
					CoreUtils.debug(" DS !isDSCanceled. ...");
					World finalWorld = world;
					plugin.dayskipTask = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

						@Override public void run() {
							int timeoffset = 10000;
							long Relative_Time = (24000 - finalWorld.getTime()) - timeoffset;
							finalWorld.setFullTime(finalWorld.getFullTime() + Relative_Time);
							CoreUtils.debug(get("sps.message.dayskipsettime") + "...");
						}

					}, dayskipdelay * 20L);

				}else{
					plugin.isDSCanceled = false;
				}
				CoreUtils.debug("dayskip returned default.");
				return true;
			} else {
				sender.sendMessage(ChatColor.YELLOW + "Dayskipper is disabled in config.");
				return false;
			}
		}catch(Exception exception){
			plugin.reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_PARSING_DAYSKIP_COMMAND).error(exception));
			return false;
		}
	}
}