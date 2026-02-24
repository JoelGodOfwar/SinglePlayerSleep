package com.github.joelgodofwar.sps.enums;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum Perms {
    HERMITS("hermits"),
    UNRESTRICTED("unrestricted"),
    DOWNFALL("downfall"),
    THUNDER("thunder"),
    CANCEL("cancel"),
    COMMAND("command"),
    UPDATE("update"),
    OP("op"),
    SHOW_UPDATE_AVAILABLE("showUpdateAvailable"),
    DAYSKIPPER("dayskipper"),
    DAYSKIPCOMMAND("dayskipcommand"),
    SHOW_CANCELLED_MSG("showCancelledMsg");

    private static final String PREFIX = "sps.";
    private final String permission;

    Perms(String permission) {
        this.permission = PREFIX + permission;
    }

    /**
     * Returns the full permission string (e.g., "sps.op").
     * @return the permission string.
     */
    @Override
    public String toString() {
        return permission;
    }

    /**
     * Checks if a CommandSender has this specific permission.
     * Always returns true for the console.
     * @param sender The CommandSender to check.
     * @return true if the sender is the console or has the permission, false otherwise.
     */
    public boolean hasPermission(CommandSender sender) {
        return !(sender instanceof Player) || sender.hasPermission(permission);
    }

    /**
     * Checks if a CommandSender has this permission or is an operator.
     * Always returns true for the console.
     * @param sender The CommandSender to check.
     * @return true if the sender is the console, has the permission, or is an op, false otherwise.
     */
    public boolean hasPermissionOrOp(CommandSender sender) {
        return !(sender instanceof Player) || (sender.isOp() || sender.hasPermission(permission));
    }

    /**
     * Checks if a Player has this specific permission.
     * @param player The Player to check.
     * @return true if the player has the permission, false otherwise.
     */
    public boolean hasPermission(Player player) {
        return player.hasPermission(permission);
    }

    /**
     * Checks if a Player has this permission or is an operator.
     * @param player The Player to check.
     * @return true if the player has the permission or is an op, false otherwise.
     */
    public boolean hasPermissionOrOp(Player player) {
        return player.isOp() || player.hasPermission(permission);
    }
}