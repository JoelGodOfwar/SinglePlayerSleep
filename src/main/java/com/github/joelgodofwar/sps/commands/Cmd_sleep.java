package com.github.joelgodofwar.sps.commands;

import com.github.joelgodofwar.sps.SinglePlayerSleep;
import com.github.joelgodofwar.sps.common.PluginLibrary;
import com.github.joelgodofwar.sps.common.error.Report;
import com.github.joelgodofwar.sps.enums.Perms;
import lib.github.joelgodofwar.coreutils.CoreUtils;
import lib.github.joelgodofwar.coreutils.util.ChatColorUtils;
import lib.github.joelgodofwar.coreutils.util.JsonConverter;
import lib.github.joelgodofwar.coreutils.util.StrUtils;
import lib.github.joelgodofwar.coreutils.util.YmlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.github.joelgodofwar.sps.util.SPSUtil.IsNight;
import static com.github.joelgodofwar.sps.util.SPSUtil.RandomNumber;

public class Cmd_sleep {
	private static SinglePlayerSleep plugin;
	public static String THIS_NAME;
	public static  String THIS_VERSION;
	private final YmlConfiguration messages;
	private final CoreUtils coreUtils;
	private final JsonConverter messageConverter;

	@SuppressWarnings("static-access")
	public Cmd_sleep(SinglePlayerSleep plugin) {
		this.plugin = plugin;
		this.THIS_NAME = plugin.THIS_NAME;
		this.THIS_VERSION = plugin.getDescription().getVersion();
		this.messages = plugin.messages; // Assuming messages is a YmlConfiguration field in SinglePlayerSleep
		this.coreUtils = plugin.coreUtils; // Assuming coreUtils is a field in SinglePlayerSleep
		this.messageConverter = plugin.messageConverter;
	}

	public static String get(String key, String... defaultValue) {
		return plugin.get(key, defaultValue);
	}

	@SuppressWarnings({ "unused", "static-access" })
	public boolean execute(CommandSender sender, String[] args) {
		try {
			if (!Perms.COMMAND.hasPermissionOrOp(sender)) {
				sender.sendMessage(ChatColor.RED + get("sps.message.noperm"));
				return false;
			}

			final boolean isPlayer = sender instanceof Player;
			Player player = isPlayer ? (Player) sender : null;
			World world = null;

				//final Player player1 = (Player) sender;
				final CommandSender daSender = sender;
				if(isPlayer){
					world = player.getWorld();
					if(!IsNight(world) && !world.hasStorm()){
						sender.sendMessage(ChatColorUtils.setColors(get("sps.message.nightorstorm")));
						return false;
					}
					//Set default timer for when the player has never slept before
					long timer = 0;
					CoreUtils.debug("SC " + player.getName() + " is sleeping.");
					long time = System.currentTimeMillis() / 1000;
					if(plugin.sleeplimit.get(player.getUniqueId()) == null){
						CoreUtils.debug("SC null - player not in sleeplimit");
						// Check if player has sps.unrestricted
						if (!Perms.UNRESTRICTED.hasPermission(player)) {
							// Set player's time in HashMap
							plugin.sleeplimit.put(player.getUniqueId(), time);
							CoreUtils.debug("SC " + player.getName() + " added to playersSlept");
						}
					}else{
						CoreUtils.debug("SC not null - player in sleeplimit");
						// Player is on the list.
						timer = plugin.sleeplimit.get(player.getUniqueId());
						CoreUtils.debug("SC time=" + time);
						CoreUtils.debug("SC timer=" + timer);
						CoreUtils.debug("SC time - timer=" +  (time - timer));
						CoreUtils.debug("SC sleeplimit=" + plugin.config.getLong("global.limits.use_cooldown", 30));
						// if !time - timer > limit
						if(!((time - timer) > plugin.config.getLong("global.limits.use_cooldown", 30))){
							long length = plugin.config.getLong("global.limits.use_cooldown", 30) - (time - timer) ;
							String sleeplimit = get("sps.message.sleeplimit").replace("<length>", "" + length);
							player.sendMessage(ChatColor.YELLOW + sleeplimit);
							CoreUtils.debug("SC sleeplimit: " + sleeplimit);
							//player.sendMessage("You can not do that for " + length + " seconds");

							return false;
						}else if((time - timer) > plugin.config.getLong("global.limits.use_cooldown", 30)){
							CoreUtils.debug("SC time - timer > sleeplimit");
							plugin.sleeplimit.replace(player.getUniqueId(), time);
						}
					}
					if(!plugin.isBloodmoonInprogress(player.getWorld())){
						CoreUtils.debug("SC isbloodmoon=false");
					}else{
						player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + get("sps.message.bloodmoon", "You can not sleep during a bloodmoon."));
						return false;
					}
				}else {
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

					if (!IsNight(world) && !world.hasStorm()) {
						sender.sendMessage(ChatColorUtils.setColors(get("sps.message.nightorstorm")));
						return false;
					}
				}
			if(!Perms.OP.hasPermissionOrOp(sender)){
				if((plugin.blacklist_sleep != null)&&!plugin.blacklist_sleep.isEmpty()){
					if(StrUtils.stringContains(plugin.blacklist_sleep, world.getName())){
						CoreUtils.log("SC - World - On blacklist: " + world.getName());
						sender.sendMessage(ChatColor.RED + "Sleep is disabled in this world.");
						return false;
					}
				}
			}

				CoreUtils.debug("SC Has perm or is op. ...");

				// Colors from config
				String cancelBracketColor = plugin.config.getString("global.cancel_bracket_color", "YELLOW").toLowerCase();
				CoreUtils.debug("SC ... CancelBracketColor=<" + cancelBracketColor + ">");
				String cancelcolor = plugin.config.getString("global.cancel_color", "RED").toLowerCase();
				CoreUtils.debug("SC ... cancelcolor=<" + cancelcolor + ">");

				// Convert config colors to JSON-compatible colors
				String jsonCancelBracketColor = messageConverter.convertToJsonColor(cancelBracketColor);
				String jsonCancelColor = messageConverter.convertToJsonColor(cancelcolor);

				// Construct a sleep message
				String sleepmsg;
				if (plugin.config.getBoolean("global.random_messages")) {
					int maxmsgs = messages.getInt("messages.sleep.count");
					int randomnumber = RandomNumber(maxmsgs);
					CoreUtils.debug("SC ... maxmsgs=" + maxmsgs);
					CoreUtils.debug("SC ... randomnumber=" + randomnumber);
					if (randomnumber != 0) {
						sleepmsg = messages.getString("messages.sleep.message_" + randomnumber, "<#FFFFFF><player> <#FFAA00>went to bed. Sweet Dreams!");
					} else {
						sleepmsg = "<#FFFFFF><player> <gradient:#FF0000:#FFFFFF>error selecting random message</gradient>...";
					}
					sleepmsg = sleepmsg.replace("<colon>", ":");
				} else {
					sleepmsg = messages.getString("messages.sleep.message_2", "<#FFFFFF><player> <#FFAA00>went to bed. Sweet Dreams!");
					CoreUtils.debug("SC ... randomsleepmsgs=false");
				}
				CoreUtils.debug("SC sleepmsg=" + sleepmsg);

				// Fix old hex codes
				sleepmsg = coreUtils.fixColors(sleepmsg);
				CoreUtils.debug("SC fixHexCodes sleepmsg=" + sleepmsg);

				// Nickname parser
				String nickName = plugin.getNickname(sender);
				if (nickName.contains("§") || nickName.contains("&")) {
					nickName = coreUtils.fixColors(nickName);
					CoreUtils.debug("SC nick contains § or &");
					CoreUtils.debug("SC nickName AfterParse = " + nickName);
				} else {
					CoreUtils.debug("SC nickName !contain § or &");
				}

				// Convert to JSON
				String jsonSleepmsg = messageConverter.convert(sleepmsg, nickName);
				CoreUtils.debug("SC jsonSleepmsg=" + jsonSleepmsg);

				// Construct cancel message as JSON
				String jsonCanmsg = "[{\"text\":\"[\",\"color\":\"" + jsonCancelBracketColor + "\"}," +
						"{\"text\":\"" + plugin.get("sps.message.cancel") + "\",\"color\":\"" + jsonCancelColor + "\"," +
						"\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"}," +
						"\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"" + plugin.get("sps.message.clickcancel", "Click to cancel sleep") + "\"}}," +
						"{\"text\":\"]\",\"color\":\"" + jsonCancelBracketColor + "\"}]";
				CoreUtils.debug("SC jsonCanmsg=" + jsonCanmsg);

				// Broadcast message
				plugin.DisplayCancel = plugin.sleepDisplayCancel;
				if ( plugin.config.getBoolean("global.broadcast_per_world", true) ) {
					if(!(isPlayer)){
						plugin.sendJson(jsonSleepmsg, jsonCanmsg);
					}else {
						plugin.sendJson(player.getWorld(), jsonSleepmsg, jsonCanmsg);
					}
				} else {
					plugin.sendJson(jsonSleepmsg, jsonCanmsg);
				}
				CoreUtils.debug("SC SendAllJsonMessage.");

				boolean worldhasstorm = world.hasStorm();

					if(!plugin.isCanceled){
						int sleepdelay = plugin.config.getInt("sleep.delay", 10);
						World finalWorld = world;
						plugin.transitionTask = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

							@Override public void run() {
								//getCoreUtils().info("runnable");
								//setDatime(sender, world);
								if(finalWorld.hasStorm()){
									if(Perms.DOWNFALL.hasPermissionOrOp(daSender)){
										finalWorld.setStorm(false);
										CoreUtils.debug(get("sps.message.setdownfall") + "...");
									}
								}
								if(finalWorld.isThundering()){
									if(Perms.THUNDER.hasPermissionOrOp(daSender)){
										finalWorld.setThundering(false);
										CoreUtils.debug(get("sps.message.setthunder") + "...");
									}
								}
								long Relative_Time = 24000 - finalWorld.getTime();
								finalWorld.setFullTime(finalWorld.getFullTime() + Relative_Time);
								CoreUtils.debug(get("sps.message.settime") + "...");
								plugin.resetPlayersRestStat(finalWorld);
							}

						}, sleepdelay * 20L);
					}else{
						plugin.isCanceled = false;
					}
		}catch(Exception exception){
			plugin.reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_PARSING_SLEEP_COMMAND).error(exception));
			return false;
		}
		CoreUtils.debug("sleep returned default.");
		return true;
	}
}