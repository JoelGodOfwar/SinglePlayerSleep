package com.github.joelgodofwar.sps.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.joelgodofwar.sps.SinglePlayerSleep;
import com.github.joelgodofwar.sps.api.ChatColorUtils;
import com.github.joelgodofwar.sps.common.PluginLibrary;
import com.github.joelgodofwar.sps.common.PluginLogger;
import com.github.joelgodofwar.sps.common.error.Report;
import com.github.joelgodofwar.sps.util.StrUtils;

public class Cmd_sleep {
	private static SinglePlayerSleep plugin;
	public static PluginLogger LOGGER;
	public static String THIS_NAME;
	public static  String THIS_VERSION;

	@SuppressWarnings("static-access")
	public Cmd_sleep(SinglePlayerSleep plugin) {
		this.plugin = plugin.getInstance();
		this.LOGGER = new PluginLogger(plugin.getInstance());
		this.THIS_NAME = plugin.THIS_NAME;
		this.THIS_VERSION = plugin.getDescription().getVersion();
	}

	public static String get(String key, String... defaultValue) {
		return plugin.get(key, defaultValue);
	}

	@SuppressWarnings({ "unused", "static-access" })
	public boolean execute(CommandSender sender, String[] args) {
		try {
			//Player player = (Player) sender;
			List<World> worlds = Bukkit.getWorlds();
			//World w = ((Entity) sender).getWorld();

			if(sender.hasPermission("sps.command")||sender.hasPermission("sps.op")) {
				if(sender instanceof Player){
					if(!sender.hasPermission("sps.op")){
						Player player = (Player) sender;
						if((plugin.blacklist_sleep != null)&&!plugin.blacklist_sleep.isEmpty()){
							if(StrUtils.stringContains(plugin.blacklist_sleep, player.getWorld().getName().toString())){
								LOGGER.log("EDE - World - On blacklist.");
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
					if(!plugin.IsNight(world) && !world.hasStorm()){
						sender.sendMessage(ChatColorUtils.setColors("" + get("sps.message.nightorstorm")));
						return false;
					}
					//Set default timer for when the player has never slept before
					long timer = 0;
					LOGGER.debug("SC " + player.getName() + " is sleeping.");
					long time = System.currentTimeMillis() / 1000;
					if(plugin.sleeplimit.get(player.getUniqueId()) == null){
						LOGGER.debug("SC null - player not in sleeplimit");
						// Check if player has sps.unrestricted
						if (!player.hasPermission("sps.unrestricted")) {
							// Set player's time in HashMap
							plugin.sleeplimit.put(player.getUniqueId(), time);
							LOGGER.debug("SC " + player.getName() + " added to playersSlept");
						}
					}else{
						LOGGER.debug("SC not null - player in sleeplimit");
						// Player is on the list.
						timer = plugin.sleeplimit.get(player.getUniqueId());
						LOGGER.debug("SC time=" + time);
						LOGGER.debug("SC timer=" + timer);
						LOGGER.debug("SC time - timer=" +  (time - timer));
						LOGGER.debug("SC sleeplimit=" + plugin.getConfig().getLong("sleeplimit", 60));
						// if !time - timer > limit
						if(!((time - timer) > plugin.getConfig().getLong("sleeplimit", 60))){
							long length = plugin.getConfig().getLong("sleeplimit", 60) - (time - timer) ;
							String sleeplimit = "" + get("sps.message.sleeplimit").toString().replace("<length>", "" + length);
							player.sendMessage(ChatColor.YELLOW + sleeplimit);
							LOGGER.debug("SC sleeplimit: " + sleeplimit);
							//player.sendMessage("You can not do that for " + length + " seconds");

							return false;
						}else if((time - timer) > plugin.getConfig().getLong("sleeplimit", 60)){
							LOGGER.debug("SC time - timer > sleeplimit");
							plugin.sleeplimit.replace(player.getUniqueId(), time);
						}
					}
					if(!plugin.isBloodmoonInprogress(player.getWorld())){//isBloodmoonInprogress(player.getWorld())//isBloodMoon
						LOGGER.debug("SC isbloodmoon=false");
					}else{
						player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + get("sps.message.bloodmoon", "You can not sleep during a bloodmoon.").toString());
						return false;
					}
				}else{
					world = Bukkit.getWorlds().get(0);
					if(!plugin.IsNight(world) && !world.hasStorm()){
						sender.sendMessage(ChatColorUtils.setColors("" + get("sps.message.nightorstorm")));
						return false;
					}
				}

				//Broadcast to Server
				String sleepmsg;
				if (plugin.getConfig().getBoolean("randomsleepmsgs")){
					int maxmsgs = plugin.getConfig().getInt("numberofsleepmsgs");
					int randomnumber = plugin.RandomNumber(maxmsgs);
					sleepmsg = plugin.getConfig().getString("sleepmsg" + randomnumber, ChatColor.WHITE + "<player> is sleeping");
					sleepmsg = sleepmsg.replace("<colon>", ":");
				}else{
					sleepmsg = plugin.getConfig().getString(ChatColor.WHITE + "<player> is sleeping");
				}
				if(plugin.is116){
					sleepmsg = ChatColorUtils.setNametoRGB(sleepmsg);
					sleepmsg = StrUtils.parseRGBNameColors(sleepmsg);
				}else{
					sleepmsg = StrUtils.stripRGBColors(sleepmsg);// strip RGBHEX TODO: stripRGBColors
					sleepmsg = ChatColorUtils.setColors(sleepmsg);// SetColorsByName
				}
				String msgcolor = ChatColorUtils.setColorsByName(plugin.getConfig().getString("sleepmsgcolor", "YELLOW"));
				//String dastring = "" + get("sleepcommand");
				//dastring = dastring.replace("<player>", "");
				//String damsg = "[\"\",{\"text\":\"player\"},{\"text\":\" is sleeping [\"},{\"text\":\"dacancel\",\"color\":\"red\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"tooltip\"}]}}},{\"text\":\"]\",\"color\":\"none\",\"bold\":false}]";
				String CancelBracketColor = ChatColorUtils.setColorsByName(plugin.getConfig().getString("cancelbracketcolor", "YELLOW"));
				String canmsg = CancelBracketColor + "[\"},{\"text\":\"dacancel\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}},{\"text\":\"" + CancelBracketColor + "]\"}";
				String damsg = "[\"\",{\"text\":\"sleepmsg " + canmsg + "]";
				String msgcolor1 = ChatColorUtils.setColorsByName(plugin.getConfig().getString("sleepmsgcolor", "YELLOW"));
				damsg = damsg.replace("sleepmsg", sleepmsg);
				damsg = ChatColorUtils.setColors(damsg);

				//String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
				// nickname parser
				String nickName = plugin.getNickname(sender);
				String playercolor = "";
				if(!nickName.contains("§")){
					//logWarn("nickName ! contain SS");
					playercolor = ChatColorUtils.setColorsByName(plugin.getConfig().getString("playernamecolor"));
				}else{
					nickName = StrUtils.parseRGBNameColors(nickName);
				}
				// end nickname parser
				damsg = damsg.replace("<player>", /**playercolor +*/ nickName);
				String cancelcolor = ChatColorUtils.setColorsByName(plugin.getConfig().getString("cancelcolor"));
				damsg = damsg.replace("dacancel", cancelcolor + get("sps.message.cancel") + msgcolor1);
				damsg = damsg.replace("tooltip", "" + get("sps.message.clickcancel")).replace("\"]\"", "\"" + msgcolor + "]\"");

				if(plugin.getConfig().getBoolean("broadcast_per_world", true)){
					plugin.sendJson(world, damsg, canmsg);
				}else{
					plugin.sendJson(damsg, canmsg);
				}

				boolean worldhasstorm = world.hasStorm();
				//SendJsonMessages.SendAllJsonMessage(damsg, "", world);
				if(sender.hasPermission("sps.hermits")||sender.hasPermission("sps.*")){
					//Thread.sleep(10000);

					if(!plugin.isCanceled){
						int sleepdelay = plugin.getConfig().getInt("sleepdelay", 10);
						plugin.transitionTask = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

							@Override public void run() {
								//getLogger().info("runnable");
								//setDatime(sender, world);
								if(world.hasStorm()){
									if(daSender.hasPermission("sps.downfall")||daSender.hasPermission("sps.op")||daSender.hasPermission("sps.*")){
										world.setStorm(false);
										LOGGER.debug("" + get("sps.message.setdownfall") + "...");
									}
								}
								if(world.isThundering()){
									if(daSender.hasPermission("sps.thunder")||daSender.hasPermission("sps.op")||daSender.hasPermission("sps.*")){
										world.setThundering(false);
										LOGGER.debug("" + get("sps.message.setthunder") + "...");
									}
								}
								long Relative_Time = 24000 - world.getTime();
								world.setFullTime(world.getFullTime() + Relative_Time);
								LOGGER.debug("" + get("sps.message.settime") + "...");
								plugin.resetPlayersRestStat(world);
							}

						}, sleepdelay * 20);

					}else{

						plugin.isCanceled = false;
					}
					//player.sendMessage(ChatColor.RED + "isCanceled=" + isCanceled);
				}else{
					sender.sendMessage(ChatColor.RED + "" + get("sps.message.noperm"));
				}
			}else{
				sender.sendMessage(ChatColor.RED + "" + get("sps.message.noperm"));
			}
		}catch(Exception exception){
			plugin.reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_PARSING_SLEEP_COMMAND).error(exception));
		}
		LOGGER.debug("sleep returned default.");
		return false;
	}
}