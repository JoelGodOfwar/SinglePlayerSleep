package com.github.joelgodofwar.sps.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.joelgodofwar.sps.SinglePlayerSleep;
import com.github.joelgodofwar.sps.api.ChatColorUtils;
import com.github.joelgodofwar.sps.common.PluginLibrary;
import com.github.joelgodofwar.sps.common.PluginLogger;
import com.github.joelgodofwar.sps.common.error.Report;
import com.github.joelgodofwar.sps.util.StrUtils;

public class Cmd_dayskip {
	private static SinglePlayerSleep plugin;
	public static PluginLogger LOGGER;
	public static String THIS_NAME;
	public static  String THIS_VERSION;

	@SuppressWarnings("static-access")
	public Cmd_dayskip(SinglePlayerSleep plugin) {
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
			if(plugin.getConfig().getBoolean("enabledayskipper", false)){
				World world;
				if(sender instanceof Player){
					Player player = (Player) sender;
					world = player.getWorld();
					if(!sender.hasPermission("sps.op")){ // TODO: DaySkip Command Blacklist Check
						if((plugin.blacklist_dayskip != null)&&!plugin.blacklist_dayskip.isEmpty()){
							if(StrUtils.stringContains(plugin.blacklist_dayskip, world.getName().toString())){
								LOGGER.log("EDE - World - On blacklist.");
								return false;
							}
						}
					}
				}else{
					world = Bukkit.getWorlds().get(0);
				}
				List<World> worlds = Bukkit.getWorlds();
				//World w = ((Entity) sender).getWorld();
				if(!plugin.IsDay(worlds.get(0))){
					sender.sendMessage(ChatColorUtils.setColors("" + get("sps.message.mustbeday")));
					return false;
				}
				if(sender.hasPermission("sps.dayskipcommand")||sender.hasPermission("sps.op")){
					LOGGER.debug(" DS Has perm or is op. ...");
					String CancelBracketColor = ChatColorUtils.setColorsByName(plugin.getConfig().getString("cancelbracketcolor", "YELLOW"));
					// OK they have the perm, now lets notify the server and schedule the runnable
					String damsg = "[\"\",{\"text\":\"sleepmsg " + CancelBracketColor + "[\"},{\"text\":\"dacancel\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}},{\"text\":\"" + CancelBracketColor + "]\"}]";
					String msgcolor = ChatColorUtils.setColorsByName(plugin.getConfig().getString("sleepmsgcolor", "YELLOW"));
					LOGGER.debug(" DS ... msgcolor=" + msgcolor);
					String sleepmsg = "" + get("sps.message.dayskipmsgcommand","<player> wants to sleep the day away...<command>");
					if(plugin.is116){
						sleepmsg = ChatColorUtils.setNametoRGB(sleepmsg);
						sleepmsg = StrUtils.parseRGBNameColors(sleepmsg);
					}else{
						sleepmsg = StrUtils.stripRGBColors(sleepmsg);// strip RGBHEX TODO: stripRGBColors
						sleepmsg = ChatColorUtils.setColors(sleepmsg);// SetColorsByName
					}
					damsg = damsg.replace("sleepmsg", sleepmsg);
					//damsg = ChatColorUtils.setColors(damsg);

					//String playercolor = ChatColorUtils.setColorsByName(plugin.getConfig().getString("playernamecolor"));
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
					LOGGER.debug(" DS ... playercolor=" + playercolor);
					damsg = damsg.replace("<player>", /**playercolor +*/ nickName);
					String cancelcolor = ChatColorUtils.setColorsByName(plugin.getConfig().getString("cancelcolor"));
					LOGGER.debug(" DS ... cancelcolor=" + cancelcolor);
					damsg = damsg.replace("dacancel", cancelcolor + get("sps.message.dayskipcancel"));
					//change cancel color based on config
					damsg = damsg.replace("tooltip", "" + get("sps.message.dayskipclickcancel")).replace("\"]\"", "\"" + msgcolor + "]\"");
					LOGGER.debug(" DS string processed. ...");

					if(plugin.getConfig().getBoolean("broadcast_per_world", true)){
						plugin.sendJson(world, damsg, "");
					}else{
						plugin.sendJson(damsg, "");
					}

					//SendJsonMessages.SendAllJsonMessage(damsg, "", world);
					LOGGER.debug(" DS SendAllJsonMessage. ...");
					if(!plugin.isDSCanceled){
						//final World world = worlds.get(0);
						int dayskipdelay = plugin.getConfig().getInt("dayskipdelay", 10);
						LOGGER.debug(" DS !isDSCanceled. ...");
						plugin.dayskipTask = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

							@Override public void run() {
								int timeoffset = 10000;
								long Relative_Time = (24000 - world.getTime()) - timeoffset;
								world.setFullTime(world.getFullTime() + Relative_Time);
								LOGGER.debug("" + get("sps.message.dayskipsettime") + "...");
							}

						}, dayskipdelay * 20);

					}else{

						plugin.isDSCanceled = false;
					}
				}
			}
		}catch(Exception exception){
			plugin.reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_PARSING_DAYSKIP_COMMAND).error(exception));
		}
		LOGGER.debug("dayskip returned default.");
		return false;
	}
}