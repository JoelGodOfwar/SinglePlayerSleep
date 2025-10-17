package com.github.joelgodofwar.sps.commands;

import java.util.List;

import com.github.joelgodofwar.sps.enums.Perms;
import lib.github.joelgodofwar.coreutils.util.StrUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.joelgodofwar.sps.SinglePlayerSleep;
import lib.github.joelgodofwar.coreutils.util.ChatColorUtils;
import com.github.joelgodofwar.sps.common.PluginLibrary;
import com.github.joelgodofwar.sps.common.PluginLogger;
import com.github.joelgodofwar.sps.common.error.Report;


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

	@SuppressWarnings({ "static-access" })
	public boolean execute(CommandSender sender, String[] args) {
		try {
			if(plugin.getConfig().getBoolean("enabledayskipper", false)){
				World world;
				if(sender instanceof Player){
					Player player = (Player) sender;
					world = player.getWorld();
					if(!Perms.OP.hasPermission(sender)){ // TODO: DaySkip Command Blacklist Check
						if((plugin.blacklist_dayskip != null)&&!plugin.blacklist_dayskip.isEmpty()){
							if(StrUtils.stringContains(plugin.blacklist_dayskip, world.getName())){
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
					sender.sendMessage(ChatColorUtils.setColors(get("sps.message.mustbeday")));
					return false;
				}
				if(Perms.DAYSKIPCOMMAND.hasPermission(sender)||Perms.OP.hasPermission(sender)){
					LOGGER.debug("PIS DS Has perm or is op. ...");
					String CancelBracketColor = "<" + plugin.getConfig().getString("cancelbracketcolor", "YELLOW") + ">";
					/* OK they have the perm, now lets notify the server and schedule the runnable */
					String canmsg = "<msgcolor>[<cancelcolor><click:run_command:'/spscancel'><hover:show_text:'tooltip'>dacancel</hover></click><msgcolor>]";
					String msgcolor = ChatColorUtils.setColorsByName(plugin.getConfig().getString("sleepmsgcolor", "YELLOW"));
					LOGGER.debug("PIS DS ... msgcolor=" + msgcolor);
					//String sleepmsg = "" + get("sps.message.dayskipmsg","<player> wants to sleep the day away...");
					int maxmsgs = plugin.messages.getInt("messages.dayskip.count");
					int randomnumber = plugin.RandomNumber(maxmsgs);
					String sleepmsg = plugin.messages.getString("messages.dayskip.message_" + randomnumber, "<#FFFFFF><player> <gradient:#000000:#FFFF00:#000000:#FFFF00>wants to sleep the day away</gradient>...");

					LOGGER.debug("PIS DS maxmsgs=" + maxmsgs);
					LOGGER.debug("PIS DS randomnumber=" + randomnumber);

					LOGGER.debug("PIS IN U sleepmsg=" + sleepmsg);
					sleepmsg = plugin.coreUtils.fixColors(sleepmsg);
					LOGGER.debug("PIS DS fixHexCodes sleepmsg=" + sleepmsg);

					sleepmsg = sleepmsg.replace("<colon>", ":");

					/* nickname parser */
					String nickName = plugin.getNickname(sender);
					if(!nickName.contains("§")){
						LOGGER.debug("PIS DS nickName !contain §");
					}else{
						nickName = plugin.coreUtils.fixColors(nickName);
						LOGGER.debug("PIS DS nick contains §" );
						LOGGER.debug("PIS DS nickName AfterParse = " + nickName );
					}
					/* end nickname parser */

					sleepmsg = sleepmsg.replace("<player>", nickName);
					String cancelcolor = "<" + plugin.getConfig().getString("cancelcolor") + ">";
					LOGGER.debug("PIS DS ... cancelcolor=" + cancelcolor);
					canmsg = canmsg.replace("dacancel", get("sps.message.cancel") );
					canmsg = canmsg.replace("<cancelcolor>", cancelcolor);
					canmsg = canmsg.replace("<msgcolor>", CancelBracketColor);
					canmsg = canmsg.replace("tooltip",  get("sps.message.clickcancel"));
					LOGGER.debug("PIS DS string processed. ...");

					if(plugin.getConfig().getBoolean("broadcast_per_world", true)){
						plugin.sendJson(world, sleepmsg, canmsg);
					}else{
						plugin.sendJson(sleepmsg, canmsg);
					}
					LOGGER.debug("PIS DS SendAllJsonMessage. ...");

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
								LOGGER.debug(get("sps.message.dayskipsettime") + "...");
							}

						}, dayskipdelay * 20L);

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