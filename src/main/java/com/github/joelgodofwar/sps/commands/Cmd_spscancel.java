package com.github.joelgodofwar.sps.commands;

import com.github.joelgodofwar.sps.SinglePlayerSleep;
import com.github.joelgodofwar.sps.common.PluginLibrary;
import com.github.joelgodofwar.sps.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.sps.common.error.Report;
import com.github.joelgodofwar.sps.enums.Perms;
import lib.github.joelgodofwar.coreutils.CoreUtils;
import lib.github.joelgodofwar.coreutils.util.JsonConverter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.github.joelgodofwar.sps.util.SPSUtil.*;


public class Cmd_spscancel {
	private static SinglePlayerSleep plugin;
	public static String THIS_NAME;
	public static  String THIS_VERSION;
	private static DetailedErrorReporter reporter;
	private final CoreUtils coreUtils;
	private final JsonConverter messageConverter;

	@SuppressWarnings("static-access")
	public Cmd_spscancel(SinglePlayerSleep plugin) {
		this.plugin = plugin;
		this.THIS_NAME = plugin.THIS_NAME;
		this.THIS_VERSION = plugin.getDescription().getVersion();
		this.reporter = plugin.reporter;
		this.coreUtils = plugin.coreUtils;
		this.messageConverter = plugin.messageConverter;
	}

	public static String get(String key, String... defaultValue) {
		return plugin.get(key, defaultValue);
	}

	@SuppressWarnings("unused")
	public boolean execute(CommandSender sender, String[] args) {
		try {
			CoreUtils.debug("command Can command cancel selected");
			World world;
			Player player;
			List<World> worlds = Bukkit.getWorlds();

			if(Perms.CANCEL.hasPermissionOrOp(sender)) {
				if(Perms.CANCEL.hasPermission(sender)){CoreUtils.debug("command Can " + sender.getName() + " has sps.cancel");}
				if(Perms.OP.hasPermission(sender)){CoreUtils.debug("command Can " + sender.getName() + " has sps.op");}
				if(sender instanceof Player){
					player = (Player) sender;
					world = player.getWorld();

					//Set default timer for when the player has never slept before
					long timer = 0;
					CoreUtils.debug("command Can... " + player.getName() + " is sleeping.");
					long time = System.currentTimeMillis() / 1000;
					if(plugin.cancellimit.get(player.getUniqueId()) == null){
						CoreUtils.debug("null - player is not in cancellimit");
						// Check if player has sps.unrestricted
						if (!Perms.UNRESTRICTED.hasPermission(player)) {
							// Set player's time in HashMap
							plugin.cancellimit.put(player.getUniqueId(), time);
							CoreUtils.debug("command Can " + player.getName() + " added to playersCancelled");
						}
					}else{
						CoreUtils.debug("not null - player is in cancellimit");
						// Player is on the list.
						timer = plugin.cancellimit.get(player.getUniqueId());
						CoreUtils.debug("time=" + time);
						CoreUtils.debug("timer=" + timer);
						CoreUtils.debug("time - timer=" +  (time - timer));
						CoreUtils.debug("cancel_cooldown=" + plugin.config.getLong("global.limits.cancel_cooldown", 30));
						// if !time - timer > limit
						if(!((time - timer) > plugin.config.getLong("global.limits.cancel_cooldown", 30))){
							long length = plugin.config.getLong("global.limits.cancel_cooldown", 30) - (time - timer) ;
							String sleeplimit = get("sps.message.sleeplimit").replace("<length>", "" + length);
							player.sendMessage(ChatColor.YELLOW + sleeplimit);
							CoreUtils.debug("command Can... cancel_cooldown: " + sleeplimit);
							//player.sendMessage("You can not do that for " + length + " seconds");
							return false;
						}else if((time - timer) > plugin.config.getLong("global.limits.cancel_cooldown", 30)){
							CoreUtils.debug("time - timer > cancel_cooldown");
							plugin.cancellimit.replace(player.getUniqueId(), time);
						}
					}
				}else{
					world = Bukkit.getWorlds().get(0);
				}

				//* Check if it's Day
				if(IsDay(world)){
					CoreUtils.debug("command Can It is Day");
					if ( !plugin.config.getBoolean("dayskip.unrestricted", false) ) {
						CoreUtils.debug("command Can !unrestricted DaySkipper");
						CoreUtils.debug("command Can isDSrunning() = " + plugin.isDSrunning());
						CoreUtils.debug("command Can isDSqueued() = " + plugin.isDSqueued());
						if ( plugin.isDSrunning() || plugin.isDSqueued() ) {
							CoreUtils.debug("command Can DS runnable is scheduled");

							CoreUtils.debug("command Can DS sleeplimit not reached");
							//Set the time this player cancelled to prevent spam
							//playersCancelled.put(sender.getName().toString(), time);
							//CoreUtils.debug("command Can DS added to playersCancelled"); }
							//cancel the runnable task
							Bukkit.getScheduler().cancelTask(plugin.dayskipTask);
							CoreUtils.debug("command Can DS task cancelled");
							//Broadcast to Server
							CoreUtils.debug("cancelbroadcast=" + plugin.config.getBoolean("dayskip.messages.broadcast_cancel", false));
							if (!(plugin.config.getBoolean("dayskip.messages.broadcast_cancel", false) == false)) {
								CoreUtils.debug("command Can DS is it here?");

								String sleepmsg;
								int maxmsgs = plugin.messages.getInt("messages.dayskip_canceled.count");
								int randomnumber = RandomNumber(maxmsgs);
								CoreUtils.debug("CAN DS ... maxmsgs=" + maxmsgs);
								CoreUtils.debug("CAN DS ... randomnumber=" + randomnumber);
								if(randomnumber != 0) {
									sleepmsg = plugin.messages.getString("messages.dayskip_canceled.message_" + randomnumber, "<#FFFFFF><player> canceled dayskip.");
								}else {
									sleepmsg = "<#FFFFFF><player> <gradient:#FF0000:#FFFFFF>error selecting random message</gradient>...";
								}
								sleepmsg = sleepmsg.replace("<colon>", ":");
								CoreUtils.debug("CAN DS sleepmsg=" + sleepmsg);

								// Fix old hex codes
								sleepmsg = coreUtils.fixColors(sleepmsg);
								CoreUtils.debug("CAN DS fixHexCodes sleepmsg=" + sleepmsg);

								// Nickname parser
								String nickName = plugin.getNickname(sender);
								if (nickName.contains("§") || nickName.contains("&")) {
									nickName = coreUtils.fixColors(nickName);
									CoreUtils.debug("CAN DS nick contains § or &");
									CoreUtils.debug("CAN DS nickName AfterParse = " + nickName);
								} else {
									CoreUtils.debug("CAN DS nickName !contain § or &");
								}

								// Convert to JSON
								String jsonSleepmsg = messageConverter.convert(sleepmsg, nickName);
								CoreUtils.debug("CAN DS jsonSleepmsg=" + jsonSleepmsg);

								if(plugin.config.getBoolean("broadcast_per_world", true)){
									plugin.sendPermJson(world, jsonSleepmsg, "sps.showCancelledMsg");
								}else{
									plugin.sendPermJson(jsonSleepmsg, "sps.showCancelledMsg");
								}
								CoreUtils.debug("CAN DS broadcast sent");
							}else if (plugin.config.getBoolean("dayskip.messages.broadcast_cancel", false) == false){
								CoreUtils.debug("command Can DS broadcast = false");
							}
							plugin.cancelSleepTasks();
							plugin.isCanceled = true;
							return true;
							//}//
						} else { //tell player they can't cancel sleep
							sender.sendMessage(ChatColor.YELLOW + get("sps.message.nocancel"));
						}
					} else { //unrestricted sleep is on tell the player
						sender.sendMessage(ChatColor.YELLOW + get("sps.message.cancelunrestricted"));
					}
				}

				//Check it's night
				if (IsNight(world)||world.hasStorm()) {
					if(IsNight(worlds.get(0))){CoreUtils.debug("command Can It is night");}
					if(worlds.get(0).hasStorm()){CoreUtils.debug("command Can it is storming");}
					//Bukkit.getServer().getWorld("");
					//Prevent cancelling if unrestricted sleep is enabled
					if ( !plugin.config.getBoolean("sleep.unrestricted", false) ) {
						CoreUtils.debug("command Can !unrestricted sleep");
						CoreUtils.debug("command Can isSleepRunning() = " + plugin.isSleepRunning());
						CoreUtils.debug("command Can isDSqueued() = " + plugin.isSleepQueued());
						//Check if this is an unrestricted sleep or not
						if ( plugin.isSleepRunning() || plugin.isSleepQueued() ) {
							CoreUtils.debug("command Can sleep runnable is scheduled");

							CoreUtils.debug("command Can sleeplimit not reached");
							//Set the time this player cancelled to prevent spam

							//cancel the runnable task
							Bukkit.getScheduler().cancelTask(plugin.transitionTask);
							plugin.isCanceled = false;
							CoreUtils.debug("command Can task cancelled");
							//Broadcast to Server

							if (!(plugin.config.getBoolean("sleep.messages.broadcast_cancel", false) == false)) {
								CoreUtils.debug("command cancelbroadcast != false");
								String sleepmsg;

								int maxmsgs = plugin.messages.getInt("messages.sleep_canceled.count");
								int randomnumber = RandomNumber(maxmsgs);
								CoreUtils.debug("CAN ... maxmsgs=" + maxmsgs);
								CoreUtils.debug("CAN ... randomnumber=" + randomnumber);
								if(randomnumber != 0) {
									sleepmsg = plugin.messages.getString("messages.sleep_canceled.message_" + randomnumber, "<#FFFFFF><player> <#FF5555>woke up—no dreams for you tonight!");
								}else {
									sleepmsg = "<#FFFFFF><player> <gradient:#FF0000:#FFFFFF>error selecting random message</gradient>...";
								}
								sleepmsg = sleepmsg.replace("<colon>", ":");

								// Fix old hex codes
								sleepmsg = coreUtils.fixColors(sleepmsg);
								CoreUtils.debug("CAN fixHexCodes sleepmsg=" + sleepmsg);

								// Nickname parser
								String nickName = plugin.getNickname(sender);
								if (nickName.contains("§") || nickName.contains("&")) {
									nickName = coreUtils.fixColors(nickName);
									CoreUtils.debug("CAN nick contains § or &");
									CoreUtils.debug("CAN nickName AfterParse = " + nickName);
								} else {
									CoreUtils.debug("CAN nickName !contain § or &");
								}

								// Convert to JSON
								String jsonSleepmsg = messageConverter.convert(sleepmsg, nickName);
								CoreUtils.debug("CAN jsonSleepmsg=" + jsonSleepmsg);

								if(plugin.config.getBoolean("global.broadcast_per_world", true)){
									plugin.sendPermJson(world, jsonSleepmsg, "sps.showCancelledMsg");
								}else{
									plugin.sendPermJson(jsonSleepmsg, "sps.showCancelledMsg");
								}

								//SendJsonMessages.SendAllJsonMessage(damsg, "", world);
								CoreUtils.debug("command Can broadcast sent");
							}else if (plugin.config.getBoolean("sleep.messages.broadcast_cancel", false) == false){
								CoreUtils.debug("command Can broadcast = false");
							}
							plugin.cancelSleepTasks();
							plugin.isCanceled = true;
							cancelSleep(world, "sleep canceled");
						} else {
							//tell player they can't cancel sleep
							sender.sendMessage(ChatColor.YELLOW + get("sps.message.nocancel"));
							CoreUtils.debug("command Can sleep runnable is NOT scheduled");
						}

					} else { //unrestricted sleep is on tell the player
						sender.sendMessage(ChatColor.YELLOW + get("sps.message.cancelunrestricted"));
					}

				} else if( plugin.config.getBoolean("sleep.notify_must_be_night") ){
					sender.sendMessage(ChatColor.YELLOW + get("sps.message.mustbenight"));
				}
			}else { //Player doesn't have permission so let's tell them
				sender.sendMessage(ChatColor.RED + get("sps.message.noperm"));
			}//*/
			if(plugin.isCanceled){
				plugin.isCanceled = false;
			}
		}catch(Exception exception){
			reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.ERROR_PARSING_SPSCANCEL_COMMAND).error(exception));
		}
		CoreUtils.debug("SPScancel returned default.");
		return true;
	}

	private void cancelSleep(World world, String cancelReason) {
		boolean ignoreVanished = plugin.config.getBoolean("global.ignore_vanished", false);
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!player.getWorld().equals(world)) continue;
			if (ignoreVanished && plugin.isVanished(player)) {
				CoreUtils.debug("Skipping vanished player " + player.getName());
				continue;
			}
			CoreUtils.debug("command Can cycling player " + player.getName());
			try {
				if (player.isSleeping()) {
					Location bedspawn = player.getBedSpawnLocation();
					if (bedspawn != null && bedspawn.distance(player.getLocation()) <= 3) {
						// Try to wakeup first
						try {
							player.wakeup(true);
						}catch(IllegalStateException exception){
							CoreUtils.debug("Wakeup failed, " + player.getName() + " was not sleeping.");
						}
						// Check if still sleeping
						if (player.isSleeping()) {
							double oldHealth = player.getHealth();
							player.damage(1);
							player.setHealth(oldHealth);
							CoreUtils.debug("Wakeup failed, kicked " + player.getName() + " from bed with damage.");
						} else {
							CoreUtils.debug("Wakeup succeeded for " + player.getName() + ".");
						}
						if (Perms.COMMAND.hasPermissionOrOp(player)) {
							player.sendTitle("", "§7[SinglePlayerSleep] Sleep canceled: " + cancelReason, 10, 20, 10);
						}
					} else {
						CoreUtils.debug(player.getName() + " is sleeping but not near bed spawn (distance > 3 or null).");
					}
				} else {
					CoreUtils.debug(player.getName() + " is not sleeping.");
				}
			} catch (Exception e) {
				CoreUtils.warn("[Exception] " + player.getDisplayName() + " has invalid bed spawn or other error.");
			}
		}
		plugin.isCanceled = false;
	}
}