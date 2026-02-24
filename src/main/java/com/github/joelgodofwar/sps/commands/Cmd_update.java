package com.github.joelgodofwar.sps.commands;

import com.github.joelgodofwar.sps.SinglePlayerSleep;
import com.github.joelgodofwar.sps.common.PluginLibrary;

import com.github.joelgodofwar.sps.common.error.Report;
import com.github.joelgodofwar.sps.enums.Perms;
import lib.github.joelgodofwar.coreutils.CoreUtils;
import lib.github.joelgodofwar.coreutils.util.VersionChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;


public class Cmd_update {
	private static SinglePlayerSleep plugin;
	public static String THIS_NAME;
	public static  String THIS_VERSION;

	@SuppressWarnings("static-access")
	public Cmd_update(SinglePlayerSleep plugin) {
		this.plugin = plugin.getInstance();
		this.THIS_NAME = plugin.THIS_NAME;
		this.THIS_VERSION = plugin.getDescription().getVersion();
	}

	public static String get(String key, String... defaultValue) {
		return plugin.get(key, defaultValue);
	}

	@SuppressWarnings({ "unused", "static-access" })
	public boolean execute(CommandSender sender, String[] args) {
		try {
			if(!(sender instanceof Player)) {
				// Console
				try {
					CoreUtils.log("Checking for updates...");
					VersionChecker updater = new VersionChecker(plugin.getInstance(), plugin.projectID, plugin.githubURL);
					if(updater.checkForUpdates()) {
						// Update available
						plugin.UpdateAvailable = true; // TODO: Update Checker
						plugin.UColdVers = updater.oldVersion();
						plugin.UCnewVers = updater.newVersion();

						CoreUtils.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
						CoreUtils.log("* " + get("sps.version.message").replace("<MyPlugin>", THIS_NAME) );
						CoreUtils.log("* " + get("sps.version.old_vers") + ChatColor.RED + plugin.UColdVers );
						CoreUtils.log("* " + get("sps.version.new_vers") + ChatColor.GREEN + plugin.UCnewVers );
						CoreUtils.log("*");
						CoreUtils.log("* " + get("sps.version.please_update") );
						CoreUtils.log("*");
						CoreUtils.log("* " + get("sps.version.download") + ": " + plugin.DownloadLink + "/history");
						CoreUtils.log("* " + get("sps.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
						CoreUtils.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
						//Bukkit.getConsoleSender().sendMessage(newVerMsg.replace("{oVer}", UColdVers).replace("{nVer}", UCnewVers));
						//Bukkit.getConsoleSender().sendMessage(Ansi.GREEN + UpdateChecker.getResourceUrl() + Ansi.RESET);
					}else{
						// Up to date
						plugin.UpdateAvailable = false;
						CoreUtils.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
						CoreUtils.log("* " + ChatColor.YELLOW + THIS_NAME + ChatColor.RESET + " " + get("sps.version.curvers") + ChatColor.RESET );
						CoreUtils.log("* " + get("sps.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
						CoreUtils.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					}
				}catch(Exception exception) {
					// Error
					plugin.reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_UPDATE_PLUGIN).error(exception));
				}
				// end update checker
				return true;
			}
			if(Perms.SHOW_UPDATE_AVAILABLE.hasPermissionOrOp(sender)){
				BukkitTask updateTask = plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
					@Override public void run() {
						try {
							Bukkit.getConsoleSender().sendMessage("Checking for updates...");
							VersionChecker updater = new VersionChecker(plugin.getInstance(), plugin.projectID, plugin.githubURL);
							if(updater.checkForUpdates()) {
								plugin.UpdateAvailable = true;
								plugin.UColdVers = updater.oldVersion();
								plugin.UCnewVers = updater.newVersion();
								String links = "[\"\",{\"text\":\"<Download>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/history\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\" \",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\"| \"},{\"text\":\"<Donate>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://ko-fi.com/joelgodofwar\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Donate_msg>\"}},{\"text\":\" | \"},{\"text\":\"<Notes>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/updates\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Notes_msg>.\"}}]";
								links = links.replace("<DownloadLink>", plugin.DownloadLink).replace("<Download>", get("sps.version.download"))
										.replace("<Donate>", get("sps.version.donate")).replace("<please_update>", get("sps.version.please_update"))
										.replace("<Donate_msg>", get("sps.version.donate.message")).replace("<Notes>", get("sps.version.notes"))
										.replace("<Notes_msg>", get("sps.version.notes.message"));
								String versions = ChatColor.GRAY + get("sps.version.new_vers") + ": " + ChatColor.GREEN + "{nVers} | " + get("sps.version.old_vers") + ": " + ChatColor.RED + "{oVers}";
								sender.sendMessage(ChatColor.GRAY + get("sps.version.message").replace("<MyPlugin>", ChatColor.GOLD + THIS_NAME + ChatColor.GRAY) );
								plugin.jsonMessageUtils.sendJsonMessage((Player) sender, links);
								sender.sendMessage(versions.replace("{nVers}", plugin.UCnewVers).replace("{oVers}", plugin.UColdVers));
							}else{
								String links = "{\"text\":\"<Donate>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://ko-fi.com/joelgodofwar\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Donate_msg>\"}}";
								links = links.replace("<Donate>", get("sps.version.donate")).replace("<Donate_msg>", get("sps.version.donate.message"));
								plugin.jsonMessageUtils.sendJsonMessage((Player) sender, links);
								sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.GREEN + " v" + THIS_VERSION + ChatColor.RESET + " " + get("sps.version.curvers") + ChatColor.RESET);
								plugin.UpdateAvailable = false;
							}
						}catch(Exception exception) {
							sender.sendMessage(ChatColor.RED + get("sps.version.update.error"));
							plugin.reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_UPDATE_PLUGIN).error(exception));
						}
					}
				});
				return true;
			}else{
				sender.sendMessage(ChatColor.YELLOW + THIS_NAME + " " + get("sps.message.notop"));
				return false;
			}
		}catch(Exception exception){
			plugin.reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_PARSING_UPDATE_COMMAND).error(exception));
		}
		CoreUtils.debug("update returned default.");
		return true;
	}
}
