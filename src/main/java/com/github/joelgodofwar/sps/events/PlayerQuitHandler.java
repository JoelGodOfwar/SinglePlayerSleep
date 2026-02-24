package com.github.joelgodofwar.sps.events;

import com.github.joelgodofwar.sps.SinglePlayerSleep;

import lib.github.joelgodofwar.coreutils.CoreUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles the PlayerQuitEvent to remove players from the onlinePlayers map.
 */
public class PlayerQuitHandler implements Listener {
    private final SinglePlayerSleep plugin;
   

    /**
     * Constructor for PlayerQuitHandler.
     *
     * @param plugin The main plugin instance, providing access to onlinePlayers and CoreUtils.
     */
    public PlayerQuitHandler(SinglePlayerSleep plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the event when a player quits the server. Removes the player from the onlinePlayers map.
     *
     * @param event The PlayerQuitEvent triggered when a player leaves the server.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            plugin.onlinePlayers.remove(event.getPlayer().getUniqueId());
            CoreUtils.debug("Removed " + event.getPlayer().getName() + " from onlinePlayers.");
        } catch (Exception e) {
            CoreUtils.warn("Error removing " + event.getPlayer().getName() + " from onlinePlayers: " + e.getMessage());
        }
    }
}