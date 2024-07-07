package com.github.joelgodofwar.sps.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.joelgodofwar.sps.SinglePlayerSleep;
import com.github.joelgodofwar.sps.api.ChatColorUtils;
import com.github.joelgodofwar.sps.common.PluginLibrary;
import com.github.joelgodofwar.sps.common.PluginLogger;
import com.github.joelgodofwar.sps.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.sps.common.error.Report;
import com.github.joelgodofwar.sps.util.StrUtils;

public class Cmd_spscancel {
	private static SinglePlayerSleep plugin;
	public static PluginLogger LOGGER;
	public static String THIS_NAME;
	public static  String THIS_VERSION;
	private static DetailedErrorReporter reporter;

	@SuppressWarnings("static-access")
	public Cmd_spscancel(SinglePlayerSleep plugin) {
		this.plugin = plugin;
		this.LOGGER = plugin.LOGGER;
		this.THIS_NAME = plugin.THIS_NAME;
		this.THIS_VERSION = plugin.getDescription().getVersion();
		this.reporter = plugin.reporter;
	}

	public static String get(String key, String... defaultValue) {
		return plugin.get(key, defaultValue);
	}

	@SuppressWarnings("unused")
	public boolean execute(CommandSender sender, String[] args) {
		try {
			LOGGER.debug("command Can command cancel selected");
			World world;
			Player player;
			List<World> worlds = Bukkit.getWorlds();

			if(sender.hasPermission("sps.cancel") || sender.hasPermission("sps.op")) {
				if(sender.hasPermission("sps.cancel")){LOGGER.debug("command Can " + sender.getName() + " has sps.cancel");}
				if(sender.hasPermission("sps.op")){LOGGER.debug("command Can " + sender.getName() + " has sps.op");}
				if(sender instanceof Player){
					player = (Player) sender;
					world = player.getWorld();

					//Set default timer for when the player has never slept before
					long timer = 0;
					LOGGER.debug("command Can... " + player.getName() + " is sleeping.");
					long time = System.currentTimeMillis() / 1000;
					if(plugin.cancellimit.get(player.getUniqueId()) == null){
						LOGGER.debug("null - player is not in cancellimit");
						// Check if player has sps.unrestricted
						if (!player.hasPermission("sps.unrestricted")) {
							// Set player's time in HashMap
							plugin.cancellimit.put(player.getUniqueId(), time);
							LOGGER.debug("command Can " + player.getName() + " added to playersCancelled");
						}
					}else{
						LOGGER.debug("not null - player is in cancellimit");
						// Player is on the list.
						timer = plugin.cancellimit.get(player.getUniqueId());
						LOGGER.debug("time=" + time);
						LOGGER.debug("timer=" + timer);
						LOGGER.debug("time - timer=" +  (time - timer));
						LOGGER.debug("cancellimit=" + plugin.getConfig().getLong("cancellimit", 60));
						// if !time - timer > limit
						if(!((time - timer) > plugin.getConfig().getLong("cancellimit", 60))){
							long length = plugin.getConfig().getLong("cancellimit", 60) - (time - timer) ;
							String sleeplimit = "" + get("sps.message.sleeplimit").toString().replace("<length>", "" + length);
							player.sendMessage(ChatColor.YELLOW + sleeplimit);
							LOGGER.debug("command Can... cancellimit: " + sleeplimit);
							//player.sendMessage("You can not do that for " + length + " seconds");
							return false;
						}else if((time - timer) > plugin.getConfig().getLong("cancellimit", 60)){
							LOGGER.debug("time - timer > cancellimit");
							plugin.cancellimit.replace(player.getUniqueId(), time);
						}
					}
				}else{
					world = Bukkit.getWorlds().get(0);
				}

				//* Check if it's Day
				if(plugin.IsDay(world)){
					LOGGER.debug("command Can It is Day");
					if (!plugin.getConfig().getBoolean("unrestricteddayskipper")) {
						LOGGER.debug("command Can !unrestricted DaySkipper");
						LOGGER.debug("command Can isDSrunning() = " + plugin.isDSrunning());
						LOGGER.debug("command Can isDSqueued() = " + plugin.isDSqueued());
						if ( plugin.isDSrunning() || plugin.isDSqueued() ) {
							LOGGER.debug("command Can DS runnable is scheduled");

							LOGGER.debug("command Can DS sleeplimit not reached");
							//Set the time this player cancelled to prevent spam
							//playersCancelled.put(sender.getName().toString(), time);
							//LOGGER.debug("command Can DS added to playersCancelled");}
							//cancel the runnable task
							Bukkit.getScheduler().cancelTask(plugin.dayskipTask);
							LOGGER.debug("command Can DS task cancelled");
							//Broadcast to Server
							LOGGER.debug("cancelbroadcast=" + plugin.getConfig().getBoolean("cancelbroadcast", false));
							if (!(plugin.getConfig().getBoolean("cancelbroadcast", false) == false)) {
								LOGGER.debug("command Can DS is it here?");
								String damsg = "[\"\",{\"text\":\"cancelmsg\"}]";
								//String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
								String msgcolor1 = ChatColorUtils.setColorsByName(plugin.getConfig().getString("sleepmsgcolor", "YELLOW"));
								damsg = damsg.replace("cancelmsg", get("sps.message.dayskipcanceled").toString());
								// nickname parser
								String nickName = plugin.getNickname(sender);
								String playercolor = "";
								if(!nickName.contains("§")){
									//logWarn("nickName ! contain SS");
									playercolor = ChatColorUtils.setColorsByName(plugin.getConfig().getString("playernamecolor"));
								}else{
									nickName = StrUtils.parseRGBNameColors(nickName);
								}
								//** end nickname parser
								damsg = damsg.replace("<player>", nickName + msgcolor1);
								LOGGER.debug("command Can DS damsg=" + damsg);

								if(plugin.getConfig().getBoolean("broadcast_per_world", true)){
									plugin.sendPermJson(world, damsg, "sps.showCancelledMsg");
								}else{
									plugin.sendPermJson(damsg, "sps.showCancelledMsg");
								}

								//SendJsonMessages.SendAllJsonMessage(damsg, "", world);
								LOGGER.debug("command Can DS broadcast sent");
							}else if (plugin.getConfig().getBoolean("cancelbroadcast", false) == false){
								LOGGER.debug("command Can DS broadcast = false");
							}
							plugin.isCanceled = true;
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
				}//

				//Check it's night
				if (plugin.IsNight(world)||world.hasStorm()) {
					if(plugin.IsNight(worlds.get(0))){LOGGER.debug("command Can It is night");}
					if(worlds.get(0).hasStorm()){LOGGER.debug("command Can it is storming");}
					//Bukkit.getServer().getWorld("");
					//Prevent cancelling if unrestricted sleep is enabled
					if (!plugin.getConfig().getBoolean("unrestrictedsleep")) {
						LOGGER.debug("command Can !unrestricted sleep");
						LOGGER.debug("command Can isSleepRunning() = " + plugin.isSleepRunning());
						LOGGER.debug("command Can isDSqueued() = " + plugin.isSleepQueued());
						//Check if this is an unrestricted sleep or not
						if ( plugin.isSleepRunning() || plugin.isSleepQueued() ) {
							LOGGER.debug("command Can sleep runnable is scheduled");

							LOGGER.debug("command Can sleeplimit not reached");
							//Set the time this player cancelled to prevent spam
							//playersCancelled.put(sender.getName().toString(), time);

							//cancel the runnable task
							Bukkit.getScheduler().cancelTask(plugin.transitionTask);
							plugin.isCanceled = false;
							LOGGER.debug("command Can task cancelled");
							//Broadcast to Server

							if (!(plugin.getConfig().getBoolean("cancelbroadcast", false) == false)) {
								LOGGER.debug("command Can is it here?");
								String damsg = "[\"\",{\"text\":\"<player> canceled sleeping.\"}]";
								//String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
								String msgcolor1 = ChatColorUtils.setColorsByName(plugin.getConfig().getString("sleepmsgcolor", "YELLOW"));
								damsg = damsg.replace("<player> canceled sleeping.", get("sps.message.canceledsleep").toString());
								//** nickname parser
								String nickName = plugin.getNickname(sender);
								String playercolor = "";
								if(!nickName.contains("§")){
									//logWarn("nickName ! contain SS");
									playercolor = ChatColorUtils.setColorsByName(plugin.getConfig().getString("playernamecolor"));
								}else{
									nickName = StrUtils.parseRGBNameColors(nickName);
								}
								//** end nickname parser
								damsg = damsg.replace("<player>",  nickName + msgcolor1);
								LOGGER.debug("command Can damsg=" + damsg);

								if(plugin.getConfig().getBoolean("broadcast_per_world", true)){
									plugin.sendPermJson(world, damsg, "sps.showCancelledMsg");
								}else{
									plugin.sendPermJson(damsg, "sps.showCancelledMsg");
								}

								//SendJsonMessages.SendAllJsonMessage(damsg, "", world);
								LOGGER.debug("command Can broadcast sent");
							}else if (plugin.getConfig().getBoolean("cancelbroadcast", false) == false){
								LOGGER.debug("command Can broadcast = false");
							}
							plugin.isCanceled = true;
							//
							double oldHealth;
							GameMode oldGamemode;
							Location location;
							Location bedspawn;

							//Sleep canceled so kick players from beds.
							for (Player p: Bukkit.getOnlinePlayers()){
								player = p;// ((CraftPlayer)p);
								LOGGER.debug("command Can cycling player " + player.getName());

								//LOGGER.debug("command Can cancel player=" + player.getDisplayName());}

								try {
									bedspawn = player.getBedSpawnLocation();
									bedspawn = new Location(bedspawn.getWorld(), bedspawn.getBlockX(),bedspawn.getBlockY(),bedspawn.getBlockZ(),0,0);
									LOGGER.debug("command Can bedspawn=" + bedspawn);
									location = player.getLocation();
									location = new Location(location.getWorld(), location.getBlockX(),location.getBlockY(),location.getBlockZ(),0,0);
									LOGGER.debug("command Can location=" + location);
									boolean inbed = false;

									if (location.equals(bedspawn)){
										inbed = true;
									}
									else{
										if(bedspawn.distance(player.getLocation()) < 2){
											LOGGER.debug("command Can distance < 2 - inbed=true");
											inbed = true;
										}
										location.add(1, 0, 0);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location1=" + location);
											inbed = true;
										}
										location.add(0, 0, 1);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=2" + location);
											inbed = true;
										}
										location.subtract(1, 0, 0);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=3" + location);
											inbed = true;
										}
										location.subtract(1, 0, 0);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=4" + location);
											inbed = true;
										}
										location.subtract(0, 0, 1);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=5" + location);
											inbed = true;
										}
										location.subtract(0, 0, 1);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=6" + location);
											inbed = true;
										}
										location.add(1, 0, 0);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=7" + location);
											inbed = true;
										}
										location.add(1, 0, 0);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location8=" + location);
											inbed = true;
										}
										location.add(1, 0, 0);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location9=" + location);
											inbed = true;
										}
										location.add(0, 0, 1);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=10" + location);
											inbed = true;
										}
										location.add(0, 0, 1);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=11" + location);
											inbed = true;
										}
										location.add(0, 0, 1);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=12" + location);
											inbed = true;
										}
										location.subtract(1, 0, 0);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=13" + location);
											inbed = true;
										}
										location.subtract(1, 0, 0);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=14" + location);
											inbed = true;
										}
										location.subtract(1, 0, 0);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=15" + location);
											inbed = true;
										}
										location.subtract(1, 0, 0);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=16" + location);
											inbed = true;
										}
										location.subtract(0, 0, 1);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=17" + location);
											inbed = true;
										}
										location.subtract(0, 0, 1);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=18" + location);
											inbed = true;
										}
										location.subtract(0, 0, 1);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=19" + location);
											inbed = true;
										}
										location.subtract(0, 0, 1);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=20" + location);
											inbed = true;
										}
										location.add(1, 0, 0);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=21" + location);
											inbed = true;
										}
										location.add(1, 0, 0);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location22=" + location);
											inbed = true;
										}
										location.add(1, 0, 0);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location=23" + location);
											inbed = true;
										}
										location.add(1, 0, 0);
										if(location.equals(bedspawn)&&(inbed != true)){
											LOGGER.debug("command Can location24=" + location);
											inbed = true;
										}

									}

									if(inbed){
										oldGamemode = player.getGameMode();
										oldHealth = player.getHealth();
										LOGGER.debug("command Can oldHEalth=" + oldHealth);
										LOGGER.debug("command Can GameMode=" + oldGamemode.toString());
										if(oldGamemode != GameMode.SURVIVAL){
											player.setGameMode(GameMode.SURVIVAL);
											LOGGER.debug("command Can GameMode set to SURVIVAL");
											//log("survival");
										}
										if(!(oldHealth <= 1)){
											player.damage(1);//.getHandle().a(true,DamageSource.CACTUS);
											LOGGER.debug("command Can damage=" + player.getHealth());
											player.setHealth(oldHealth);
											//player.wakeup(true);
										}else{
											player.setHealth(oldHealth + 1);
											player.damage(1);//.getHandle().a(true,DamageSource.CACTUS);
											LOGGER.debug("command Can damage=" + player.getHealth());
											player.setHealth(oldHealth);
											//player.wakeup(true);
										}
										player.setGameMode(oldGamemode);
										LOGGER.debug("command Can GameMode set to " + oldGamemode.toString());
										//if(player.isSleeping()){
										//player.wakeup(true);
									}
								}catch (Exception e){
									LOGGER.warn("[Exception] " + player.getDisplayName() + " has never slept before.");
									// Failed to submit the stats
								}
								if(plugin.isCanceled){
									LOGGER.debug("command Can... isCanceled set to false");
									plugin.isCanceled = false;
								}
								return true;
								//}//
							}
						} else { //tell player they can't cancel sleep
							sender.sendMessage(ChatColor.YELLOW + "" + get("sps.message.nocancel"));
							LOGGER.debug("command Can sleep runnable is NOT scheduled");
						}

					} else { //unrestricted sleep is on tell the player
						sender.sendMessage(ChatColor.YELLOW + "" + get("sps.message.cancelunrestricted"));
					}

				} else if(plugin.getConfig().getBoolean("notifymustbenight")){
					sender.sendMessage(ChatColor.YELLOW + "" + get("sps.message.mustbenight"));
				}
			}else { //Player doesn't have permission so let's tell them
				sender.sendMessage(ChatColor.RED + "" + get("sps.message.noperm"));
			}//*/
			if(plugin.isCanceled){
				plugin.isCanceled = false;
			}
		}catch(Exception exception){
			reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.ERROR_PARSING_SPSCANCEL_COMMAND).error(exception));
		}
		LOGGER.debug("SPScancel returned default.");
		return true;
	}
}


