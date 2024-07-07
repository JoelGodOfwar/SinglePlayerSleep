package com.github.joelgodofwar.sps.common;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;

import com.github.joelgodofwar.sps.common.error.BasicErrorReporter;
import com.github.joelgodofwar.sps.common.error.ErrorReporter;
import com.github.joelgodofwar.sps.common.error.ReportType;
import com.google.common.collect.ImmutableList;

public class PluginLibrary {

	/**
	 * The minimum version MoreMobHeads has been tested with.
	 */
	public static final String MINIMUM_MINECRAFT_VERSION = "1.14";

	/**
	 * The maximum version MoreMobHeads has been tested with.
	 */
	public static final String MAXIMUM_MINECRAFT_VERSION = "1.21";

	/**
	 * The date (with ISO 8601 or YYYY-MM-DD) when the most recent version (1.20.4) was released.
	 */
	public static final String MINECRAFT_LAST_RELEASE_DATE = "2023-12-07";

	/**
	 * Plugins that are currently incompatible with MoreMobHeads.
	 */
	public static final List<String> INCOMPATIBLE = ImmutableList.of("");

	private static Plugin plugin;

	private static boolean updatesDisabled;
	private static boolean initialized;
	private static ErrorReporter reporter = new BasicErrorReporter();

	protected static void init(Plugin plugin, ErrorReporter reporter) {
		Validate.isTrue(!initialized, "MoreMobHeads has already been initialized.");
		PluginLibrary.plugin = plugin;
		PluginLibrary.reporter = reporter;

		initialized = true;
	}

	public static final ReportType REPORT_CANNOT_DELETE_CONFIG = new ReportType("Cannot delete old SinglePlayerSleep configuration.");
	public static final ReportType REPORT_CANNOT_COPY_FILE = new ReportType("Cannot copy file.");

	public static final ReportType REPORT_PLUGIN_LOAD_ERROR = new ReportType("Cannot load SinglePlayerSleep.");
	public static final ReportType REPORT_CANNOT_LOAD_CONFIG = new ReportType("Cannot load configuration");
	public static final ReportType REPORT_CANNOT_CHECK_CONFIG = new ReportType("Cannot check configuration");
	public static final ReportType REPORT_CANNOT_SAVE_CONFIG = new ReportType("Cannot save configuration");
	public static final ReportType REPORT_PLUGIN_ENABLE_ERROR = new ReportType("Cannot enable SinglePlayerSleep.");
	public static final ReportType REPORT_PLUGIN_UNKNOWN_ERROR = new ReportType("Unknown Error");

	public static final ReportType REPORT_MESSAGES_LOAD_ERROR = new ReportType("Could not load messages.yml");
	public static final ReportType REPORT_MESSAGES_COPY_ERROR = new ReportType("Could not copy messages.yml to old_messages.yml");
	public static final ReportType REPORT_OLDMESSAGES_LOAD_ERROR = new ReportType("Could not load old_messages.yml");
	public static final ReportType REPORT_OLDMESSAGES_SAVE_ERROR = new ReportType("Could not save old messages to messages.yml");
	public static final ReportType REPORT_PLAYERHEAD_LOAD_ERROR = new ReportType("Could not load player_heads.yml");
	public static final ReportType REPORT_CUSTOM_LOAD_ERROR = new ReportType("Could not load custom_trades.yml");
	public static final ReportType REPORT_CHANCE_LOAD_ERROR = new ReportType("Could not load chance_config.yml");
	public static final ReportType REPORT_CHANCE_SAVE_ERROR = new ReportType("Could not save chance_config.yml");
	public static final ReportType REPORT_CHANCE_COPY_ERROR = new ReportType("Could not copy chance_config.yml to old_chance_config.yml");
	public static final ReportType REPORT_MOBNAMES_LOAD_ERROR = new ReportType("Could not load lang_mobnames file");

	public static final ReportType REPORT_PIE_LOAD_ERROR = new ReportType("Error with naming mob.");
	public static final ReportType REPORT_HEAD_URL_ERROR = new ReportType("Malformed URL Error");
	public static final ReportType REPORT_BLOCKHEAD_LOAD_ERROR = new ReportType("Cannot load BlockHeads file");

	public static final ReportType REPORT_METRICS_IO_ERROR = new ReportType("Unable to enable metrics due to network problems.");
	public static final ReportType REPORT_METRICS_GENERIC_ERROR = new ReportType("Unable to enable metrics due to network problems.");

	public static final ReportType REPORT_CANNOT_PARSE_MINECRAFT_VERSION = new ReportType("Unable to retrieve current Minecraft version. Assuming %s");
	public static final ReportType REPORT_CANNOT_DETECT_CONFLICTING_PLUGINS = new ReportType("Unable to detect conflicting plugin versions.");
	public static final ReportType REPORT_CANNOT_REGISTER_COMMAND = new ReportType("Cannot register command %s: %s");

	public static final ReportType REPORT_CANNOT_CREATE_TIMEOUT_TASK = new ReportType("Unable to create packet timeout task.");
	public static final ReportType REPORT_CANNOT_UPDATE_PLUGIN = new ReportType("Cannot check for updates.");

	public static final ReportType REPORT_CANNOT_GET_HEXNICK = new ReportType("Cannot get Nick from HexNicks.");

	public static final ReportType REPORT_METRICS_LOAD_ERROR = new ReportType("Cannot load bStats Metrics.");

	public static final ReportType ERROR_PARSING_COMMAND = new ReportType("Error parsing Command.");
	public static final ReportType REPORT_TAB_COMPLETE_ERROR = new ReportType("Error parsing Tab Complete.");
	public static final ReportType UNHANDLED_COMMAND_ERROR = new ReportType("Command had an Unhandled exception.");
	public static final ReportType REPORT_BOOK_SETUP_ERROR = new ReportType("Error creating settings book.");
	public static final ReportType ERROR_HANDLING_ENDERMAN_GRIEF = new ReportType("Error handling Enderman Grief.");
	public static final ReportType ERROR_HANDLING_CREEPER_GRIEF = new ReportType("Error handling Creeper Grief.");
	public static final ReportType ERROR_HANDLING_GHAST_GRIEF = new ReportType("Error handling Ghast Grief.");
	public static final ReportType ERROR_HANDLING_SKELETON_HORSE_GRIEF = new ReportType("Error handling Skeleton_Horse Grief.");
	public static final ReportType ERROR_HANDLING_WANDERING_TRADER_GRIEF = new ReportType("Error handling Wandering Trader Grief.");
	public static final ReportType ERROR_HANDLING_PHANTOM_GRIEF = new ReportType("Error handling Phantom Grief.");
	public static final ReportType ERROR_HANDLING_PILLAGER_PATROL_GRIEF = new ReportType("Error handling Pillager Patrol Grief.");
	public static final ReportType ERROR_PLAYER_INTERACTENTITY_EVENT = new ReportType("Unhandled PlayerInteractEntityEvent Exception.");
	public static final ReportType ERROR_ENTITY_DEATH_EVENT = new ReportType("Unhandled EntityDeathEvent Exception.");
	public static final ReportType ERROR_CHUNK_UNLOAD_EVENT = new ReportType("Unhandled ChunkUnloadEvent Exception.");
	public static final ReportType ERROR_CREATURE_SPAWN_EVENT = new ReportType("Unhandled CreatureSpawnEvent Exception.");
	public static final ReportType ERROR_CSE_VILLAGER = new ReportType("Unable to parse infected/cured villager.");
	public static final ReportType ERROR_CSE_DRAGON = new ReportType("Unable to parse EnderDragon.");

	public static final ReportType ERROR_ENTER_BED_EVENT = new ReportType("Unable to process PlayerBedEnterEvent.");
	public static final ReportType ERROR_LEAVE_BED_EVENT = new ReportType("Unable to process PlayerBedLeaveEvent.");
	public static final ReportType ERROR_RESET_INSOMNIA = new ReportType("Unable to reset insomnia.");
	public static final ReportType ERROR_PLAYER_NOT_FOUND = new ReportType("Unable to find Player.");
	public static final ReportType ERROR_SEARCHING_FILE = new ReportType("Unable to search file for text.");
	public static final ReportType ERROR_CHECKING_BLOODMOON = new ReportType("Unable to check for bloodmon.");
	public static final ReportType ERROR_PARSING_NICKNAME = new ReportType("Error parsing Nickname.");
	public static final ReportType ERROR_PARSING_DAYSKIP_COMMAND = new ReportType("Error parsing Dayskip Command.");
	public static final ReportType ERROR_PARSING_SLEEP_COMMAND = new ReportType("Error parsing Sleep Command.");
	public static final ReportType ERROR_PARSING_SPSCANCEL_COMMAND = new ReportType("Error parsing Cancel Command.");
	public static final ReportType ERROR_PARSING_UPDATE_COMMAND = new ReportType("Error parsing Update Command.");



	/**
	 * Gets the MoreMobHeads plugin instance.
	 * @return The plugin instance
	 */
	public static Plugin getPlugin() {
		return plugin;
	}

	/**
	 * Disables the MoreMobHeads update checker.
	 */
	public static void disableUpdates() {
		updatesDisabled = true;
	}

	/**
	 * Retrieve the current error reporter.
	 * @return Current error reporter.
	 */
	public static ErrorReporter getErrorReporter() {
		return reporter;
	}

	/**
	 * Whether updates are currently disabled.
	 * @return True if it is, false if not
	 */
	public static boolean updatesDisabled() {
		return updatesDisabled;
	}
}