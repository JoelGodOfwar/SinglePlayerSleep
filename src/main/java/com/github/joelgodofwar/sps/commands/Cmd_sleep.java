package com.github.joelgodofwar.sps.commands;

import java.util.List;

import com.github.joelgodofwar.sps.enums.Perms;
import lib.github.joelgodofwar.coreutils.util.StrUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.joelgodofwar.sps.SinglePlayerSleep;
import lib.github.joelgodofwar.coreutils.util.ChatColorUtils;
import com.github.joelgodofwar.sps.common.PluginLibrary;
import com.github.joelgodofwar.sps.common.PluginLogger;
import com.github.joelgodofwar.sps.common.error.Report;

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

			if(Perms.COMMAND.hasPermissionOrOp(sender)) {
				if(sender instanceof Player){
					if(!Perms.OP.hasPermissionOrOp(sender)){
						Player player = (Player) sender;
						if((plugin.blacklist_sleep != null)&&!plugin.blacklist_sleep.isEmpty()){
							if(StrUtils.stringContains(plugin.blacklist_sleep, player.getWorld().getName())){
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
						sender.sendMessage(ChatColorUtils.setColors(get("sps.message.nightorstorm")));
						return false;
					}
					//Set default timer for when the player has never slept before
					long timer = 0;
					LOGGER.debug("SC " + player.getName() + " is sleeping.");
					long time = System.currentTimeMillis() / 1000;
					if(plugin.sleeplimit.get(player.getUniqueId()) == null){
						LOGGER.debug("SC null - player not in sleeplimit");
						// Check if player has sps.unrestricted
						if (!Perms.UNRESTRICTED.hasPermission(player)) {
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
							String sleeplimit = get("sps.message.sleeplimit").replace("<length>", "" + length);
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
						player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + get("sps.message.bloodmoon", "You can not sleep during a bloodmoon."));
						return false;
					}
				}else{
					world = Bukkit.getWorlds().get(0);
					if(!plugin.IsNight(world) && !world.hasStorm()){
						sender.sendMessage(ChatColorUtils.setColors(get("sps.message.nightorstorm")));
						return false;
					}
				}

				LOGGER.debug("PIS IN Has perm or is op. ...");

				//Broadcast to Server
				String msgcolor = "<" + plugin.getConfig().getString("sleepmsgcolor", "YELLOW") + ">";
				LOGGER.debug("PIS IN ... msgcolor=" + msgcolor);
				String CancelBracketColor = "<" + plugin.getConfig().getString("cancelbracketcolor", "YELLOW") + ">";
				LOGGER.debug("PIS IN ... CancelBracketColor=" + CancelBracketColor);
				String canmsg = "<msgcolor>[<cancelcolor><click:run_command:'/spscancel'><hover:show_text:'tooltip'>dacancel</hover></click><msgcolor>]";
				String sleepmsg;
				if (plugin.getConfig().getBoolean("randomsleepmsgs")){
					int maxmsgs = plugin.messages.getInt("messages.sleep.count");
					int randomnumber = plugin.RandomNumber(maxmsgs);
					sleepmsg = plugin.messages.getString("messages.sleep.message_" + randomnumber, "<#FFFFFF><player> <#FFAA00>went to bed. Sweet Dreams!");
					sleepmsg = sleepmsg.replace("<colon>", ":");
					LOGGER.debug("PIS IN ... maxmsgs=" + maxmsgs);
					LOGGER.debug("PIS IN ... randomnumber=" + randomnumber);
				}else{
					sleepmsg = ("<#FFFFFF><player> <#FFAA00>went to bed. Sweet Dreams!");
					LOGGER.debug("PIS IN ... randomsleepmsgs=false");
				}
				// Fix old hex codes
				LOGGER.debug("PIS IN sleepmsg=" + sleepmsg);
				sleepmsg = plugin.coreUtils.fixColors(sleepmsg);
				LOGGER.debug("PIS IN fixHexCodes sleepmsg=" + sleepmsg);

				LOGGER.debug("PIS IN sleepmsg=" + sleepmsg);

				/* nickname parser */
				String nickName = plugin.getNickname(sender);
				if(!nickName.contains("§")){
					LOGGER.debug("PIS IN nickName !contain §");
				}else{
					nickName = plugin.coreUtils.fixColors(nickName);
					LOGGER.debug("PIS IN nick contains §" );
					LOGGER.debug("PIS IN nickName AfterParse = " + nickName );
				}
				/* end nickname parser */

				sleepmsg = sleepmsg.replace("<player>", nickName);
				String cancelcolor = "<" + plugin.getConfig().getString("cancelcolor") + ">";
				LOGGER.debug("PIS IN ... cancelcolor=" + cancelcolor);
				canmsg = canmsg.replace("dacancel", get("sps.message.cancel") );
				canmsg = canmsg.replace("<cancelcolor>", cancelcolor);
				canmsg = canmsg.replace("<msgcolor>", CancelBracketColor);
				canmsg = canmsg.replace("tooltip",  get("sps.message.clickcancel"));
				LOGGER.debug("PIS IN string processed. ...");

				if(plugin.getConfig().getBoolean("broadcast_per_world", true)){
					plugin.sendJson(world, sleepmsg, canmsg);
				}else{
					plugin.sendJson(sleepmsg, canmsg);
				}

				boolean worldhasstorm = world.hasStorm();
				//SendJsonMessages.SendAllJsonMessage(damsg, "", world);
				if(Perms.HERMITS.hasPermissionOrOp(sender)){
					//Thread.sleep(10000);

					if(!plugin.isCanceled){
						int sleepdelay = plugin.getConfig().getInt("sleepdelay", 10);
						plugin.transitionTask = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

							@Override public void run() {
								//getLogger().info("runnable");
								//setDatime(sender, world);
								if(world.hasStorm()){
									if(Perms.DOWNFALL.hasPermissionOrOp(daSender)){
										world.setStorm(false);
										LOGGER.debug(get("sps.message.setdownfall") + "...");
									}
								}
								if(world.isThundering()){
									if(Perms.THUNDER.hasPermissionOrOp(daSender)){
										world.setThundering(false);
										LOGGER.debug(get("sps.message.setthunder") + "...");
									}
								}
								long Relative_Time = 24000 - world.getTime();
								world.setFullTime(world.getFullTime() + Relative_Time);
								LOGGER.debug(get("sps.message.settime") + "...");
								plugin.resetPlayersRestStat(world);
							}

						}, sleepdelay * 20L);

					}else{

						plugin.isCanceled = false;
					}
					//player.sendMessage(ChatColor.RED + "isCanceled=" + isCanceled);
				}else{
					sender.sendMessage(ChatColor.RED + get("sps.message.noperm"));
				}
			}else{
				sender.sendMessage(ChatColor.RED + get("sps.message.noperm"));
			}
		}catch(Exception exception){
			plugin.reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_PARSING_SLEEP_COMMAND).error(exception));
		}
		LOGGER.debug("sleep returned default.");
		return false;
	}
}