package com.github.joelgodofwar.sps.events;

import com.github.joelgodofwar.sps.SinglePlayerSleep;
import com.github.joelgodofwar.sps.common.PluginLibrary;
import com.github.joelgodofwar.sps.common.error.Report;
import com.github.joelgodofwar.sps.enums.Perms;
import lib.github.joelgodofwar.coreutils.CoreUtils;
import lib.github.joelgodofwar.coreutils.util.YmlConfiguration;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Handles the PlayerJoinEvent to send update notifications and special messages to specific players.
 */
public class PlayerJoinHandler implements Listener {
    private final SinglePlayerSleep plugin;
    private final boolean UpdateAvailable;
    private final String DownloadLink;
    private final String UCnewVers;
    private final String UColdVers;
    private final String THIS_NAME;
    private final String THIS_VERSION;

    /**
     * Constructor for PlayerJoinHandler.
     *
     * @param plugin The main plugin instance, providing access to all necessary fields and methods.
     */
    public PlayerJoinHandler(SinglePlayerSleep plugin) {
        this.plugin = plugin;
        YmlConfiguration config = plugin.config;
        this.UpdateAvailable = plugin.UpdateAvailable;
        this.DownloadLink = plugin.DownloadLink;
        this.UCnewVers = plugin.UCnewVers;
        this.UColdVers = plugin.UColdVers;
        this.THIS_NAME = SinglePlayerSleep.THIS_NAME;
        this.THIS_VERSION = SinglePlayerSleep.THIS_VERSION;
    }

    /**
     * Handles the event when a player joins the server. Sends update notifications to players with
     * appropriate permissions and a special message to specific players.
     *
     * @param event The PlayerJoinEvent triggered when a player joins the server.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.onlinePlayers.put(player.getUniqueId(), player);
        if(player.hasPermission("essentials.sleepingignored")){
            if (!isLuckPermsAvailable()) {
                CoreUtils.warn(ChatColor.RED + "LuckPerms not found, cannot negate essentials.sleepingignored for " + player.getName() + "." + ChatColor.RESET);
                return;
            }

            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);

            // Negate the permission by setting it to false
            user.data().remove(Node.builder("essentials.sleepingignored").build());
            user.data().add(Node.builder("essentials.sleepingignored").value(false).build());
            luckPerms.getUserManager().saveUser(user);
            CoreUtils.log("Negated essentials.sleepingignored for " + player.getName());
        }
        try {
            if (UpdateAvailable && (Perms.SHOW_UPDATE_AVAILABLE.hasPermissionOrOp(player))) {
                String links = "[\"\",{\"text\":\"<Download>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/history\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\" \",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\"| \"},{\"text\":\"<Donate>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://ko-fi.com/joelgodofwar\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Donate_msg>\"}},{\"text\":\" | \"},{\"text\":\"<Notes>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/updates\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Notes_msg>\"}}]";
                links = links.replace("<DownloadLink>", DownloadLink)
                        .replace("<Download>", plugin.get("sps.version.download"))
                        .replace("<Donate>", plugin.get("sps.version.donate"))
                        .replace("<please_update>", plugin.get("sps.version.please_update"))
                        .replace("<Donate_msg>", plugin.get("sps.version.donate.message"))
                        .replace("<Notes>", plugin.get("sps.version.notes"))
                        .replace("<Notes_msg>", plugin.get("sps.version.notes.message"));
                String versions = ChatColor.GRAY + plugin.get("sps.version.new_vers") + ": " + ChatColor.GREEN + "{nVers} | " + plugin.get("sps.version.old_vers") + ": " + ChatColor.RED + "{oVers}";
                player.sendMessage(ChatColor.GRAY + plugin.get("sps.version.message").replace("<MyPlugin>", ChatColor.GOLD + THIS_NAME + ChatColor.GRAY));
                plugin.jsonMessageUtils.sendJsonMessage(player, links);
                player.sendMessage(versions.replace("{nVers}", UCnewVers).replace("{oVers}", UColdVers));
            }
        } catch (Exception exception) {
            SinglePlayerSleep.reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.REPORT_CANNOT_UPDATE_PLUGIN).error(exception));
        }
        if (player.getName().equals("JoelYahwehOfWar") || player.getName().equals("JoelGodOfWar")) {
            player.sendMessage(THIS_NAME + " " + THIS_VERSION + " §x§1§1§F§F§A§AHello §x§A§A§F§F§1§1father!");
            plugin.joelTimeOffset = 18000L;
        }
    }

    private boolean isLuckPermsAvailable() {
        RegisteredServiceProvider<LuckPerms> provider = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
        return provider != null;
    }

}