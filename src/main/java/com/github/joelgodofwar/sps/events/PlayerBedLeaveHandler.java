package com.github.joelgodofwar.sps.events;

import com.github.joelgodofwar.sps.SinglePlayerSleep;

import com.github.joelgodofwar.sps.common.PluginLibrary;
import com.github.joelgodofwar.sps.common.error.Report;
import lib.github.joelgodofwar.coreutils.util.YmlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.Bukkit;

/**
 * Handles the PlayerBedLeaveEvent to execute specific actions when a player leaves a bed.
 */
public class PlayerBedLeaveHandler implements Listener {
    private final YmlConfiguration config;
    private final SinglePlayerSleep plugin;

    /**
     * Constructor for PlayerBedLeaveHandler.
     *
     * @param plugin The main plugin instance.
     */
    public PlayerBedLeaveHandler(SinglePlayerSleep plugin) {
        this.plugin = plugin;
        this.config = plugin.config;
    }

    /**
     * Handles the event when a player leaves a bed. If configured, it triggers the "spscancel" command.
     *
     * @param event The PlayerBedLeaveEvent triggered when a player leaves a bed.
     */
    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        try {
            Player player = event.getPlayer();
            if (config.getBoolean("exitbedcancel", false)) {
                Bukkit.dispatchCommand(player, "spscancel");
            }
        } catch (Exception exception) {
            SinglePlayerSleep.reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.ERROR_LEAVE_BED_EVENT).error(exception));
        }
    }
}