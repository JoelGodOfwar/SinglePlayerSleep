package com.github.joelgodofwar.sps.events;

import com.github.joelgodofwar.sps.SinglePlayerSleep;
import com.github.joelgodofwar.sps.common.PluginLibrary;

import com.github.joelgodofwar.sps.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.sps.common.error.Report;
import com.github.joelgodofwar.sps.enums.Perms;
import lib.github.joelgodofwar.coreutils.CoreUtils;
import lib.github.joelgodofwar.coreutils.util.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.github.joelgodofwar.sps.util.SPSUtil.*;
import static com.github.joelgodofwar.sps.util.SPSUtil.RandomNumber;

public class PlayerBedEnterHandler implements Listener {
    private final SinglePlayerSleep plugin;
    private final YmlConfiguration config;
    private final YmlConfiguration messages;
    private final CoreUtils coreUtils;
    private final JsonConverter messageConverter;
    private final HashMap<UUID, Long> sleeplimit;
    private final String blacklist_sleep;
    private final String blacklist_dayskip;
    private boolean isCanceled;
    private boolean isDSCanceled;
    private int transitionTask;
    private int transitionTaskUnrestricted;
    private int dayskipTask;
    private static DetailedErrorReporter reporter;

    public PlayerBedEnterHandler(SinglePlayerSleep plugin) {
        this.plugin = plugin;
        this.config = plugin.config;
        this.messages = plugin.messages; // Assuming messages is a YmlConfiguration field in SinglePlayerSleep
        this.coreUtils = plugin.coreUtils; // Assuming coreUtils is a field in SinglePlayerSleep
        this.messageConverter = plugin.messageConverter; // Assuming messageConverter is a field
        this.sleeplimit = plugin.sleeplimit; // Assuming sleeplimit is a HashMap<UUID, Long>
        this.blacklist_sleep = plugin.blacklist_sleep; // Assuming blacklist_sleep is a List<String>
        this.blacklist_dayskip = plugin.blacklist_dayskip; // Assuming blacklist_dayskip is a List<String>
        this.isCanceled = plugin.isCanceled; // Assuming isCanceled is a boolean field
        this.isDSCanceled = plugin.isDSCanceled; // Assuming isDSCanceled is a boolean field
        this.transitionTask = plugin.transitionTask; // Assuming transitionTask is an int field
        this.transitionTaskUnrestricted = plugin.transitionTaskUnrestricted; // Assuming transitionTaskUnrestricted is an int
        this.dayskipTask = plugin.dayskipTask; // Assuming dayskipTask is an int field
        reporter = SinglePlayerSleep.reporter;
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) throws InterruptedException{
        try {
            CoreUtils.debug(ChatColor.RED + "** Start PlayerBedEnterEvent **");
            List<World> worlds = Bukkit.getWorlds();
            final Player player = event.getPlayer();
            CoreUtils.debug("PIS player set. ...");
            final World world = player.getWorld();
            CoreUtils.debug("PIS world set. ... " + world.getName());
            int sleepdelay = config.getInt("sleep.delay", 10);
            int dayskipdelay = config.getInt("dayskip.delay", 10);
            PlayerBedEnterEvent.BedEnterResult theResult = event.getBedEnterResult();

            CoreUtils.debug("PIS world time=" + world.getTime());
            CoreUtils.debug("PIS lastSleepTime=" + plugin.lastSleepTime);
            CoreUtils.debug("PIS getWorld()=" + player.getWorld().getName());
            CoreUtils.debug("PIS isThundering()=" + player.getWorld().isThundering());
            CoreUtils.debug("PIS IsNight=" + IsNight(player.getWorld()) );

            CoreUtils.debug("PIS perm essentials.sleepingignored=" + player.hasPermission("essentials.sleepingignored"));
            if((plugin.getServer().getPluginManager().getPlugin("EssentialsX") != null)||(plugin.getServer().getPluginManager().getPlugin("Essentials") != null)){
                if(player.hasPermission("essentials.sleepingignored") && !player.isOp()){
                    player.sendMessage(ChatColor.RED + "WARNING! " + ChatColor.YELLOW + " you have the permission (" + ChatColor.GOLD +
                            "essentials.sleepingignored" + ChatColor.YELLOW +
                            ") which is conflicting with SinglePlaySleep. Please ask for it to be removed. " + ChatColor.RED + "WARNING! ");
                    CoreUtils.warn("PIS Player " + player.getName() + "has the permission " + "essentials.sleepingignored" + " which is known to conflict with SinglePlayerSleep.");
                    return;
                }
            }
            String damsg1 = "Player has the following permissions: " +
                    "sps.hermits=" + Perms.HERMITS.hasPermission(player) + ", " + "sps.cancel=" + Perms.CANCEL.hasPermission(player) + ", " +
                    ChatColor.RED + "sps.unrestricted=" + Perms.UNRESTRICTED.hasPermission(player) + ChatColor.RESET + ", " + "sps.downfall=" + Perms.DOWNFALL.hasPermission(player) + ", " +
                    "sps.thunder=" + Perms.THUNDER.hasPermission(player) + ", " + "sps.command=" + Perms.COMMAND.hasPermission(player) + ", " +
                    "sps.update=" + Perms.UPDATE.hasPermission(player) + ", " + "sps.op=" + Perms.OP.hasPermission(player) + ", " +
                    "sps.showUpdateAvailable=" + Perms.SHOW_UPDATE_AVAILABLE.hasPermission(player) + ", " +
                    "sps.dayskipper=" + Perms.DAYSKIPPER.hasPermission(player) + ", " + "sps.dayskipcommand=" + Perms.DAYSKIPCOMMAND.hasPermission(player) ;
            CoreUtils.debug(damsg1.replace("=true,", "=" + ChatColor.GREEN + "true" + ChatColor.RESET + ",").replace("=false,", "=" + ChatColor.RED + "false" + ChatColor.RESET + ","));

            // 1. Check playersSleepingPercentage
            int sleepPercentage = world.getGameRuleValue(GameRule.PLAYERS_SLEEPING_PERCENTAGE);
            boolean isInvalid = sleepPercentage < 75; // Warn if < 75

            if (isInvalid || sleepPercentage == 0) {
                if (isInvalid && config.getBoolean("sleep.autoFixSleepingPercentage", true)) {
                    world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 100);
                    CoreUtils.warn("playersSleepingPercentage was " + sleepPercentage + ". Set to 100 to prevent vanilla interference.");
                } else {
                    CoreUtils.warn("playersSleepingPercentage is " + sleepPercentage + ". Recommend: /gamerule playersSleepingPercentage 100 to prevent vanilla interference.");
                    if (sleepPercentage == 0) {
                        CoreUtils.warn("playersSleepingPercentage is 0, which causes instant night skips. Refusing to proceed.");
                        return; // Refuse to proceed if 0
                    }
                    return; // Exit for other invalid values if not auto-fixed
                }
            }

            // Check if player is vanished and ignoreVanishedPlayers is true
            boolean ignoreVanishedPlayers = config.getBoolean("global.ignore_vanished", false);
            if (ignoreVanishedPlayers && plugin.isVanished(player)) {
                player.sendMessage("You appear to be vanished, ignoring sleep.");
                CoreUtils.log("Ignoring sleep event for vanished player: " + player.getName());
                return;
            }
            boolean isThundering = player.getWorld().isThundering();
            boolean isNightTime = IsNight(player.getWorld());
            if( config.getBoolean("dayskip.enabled", false) ){ // TODO: Dayskip
                /* Check if it's Day for DaySkipper */
                if(IsDay(player.getWorld())){
                    CoreUtils.debug("PIS DS it is Day");
                    /* OK it's day check if it's a Black bed. */
                    if(!Perms.OP.hasPermission(player)){ // TODO: Dayskip blacklist Check
                        if((blacklist_dayskip != null)&&!blacklist_dayskip.isEmpty()){
                            if(StrUtils.stringContains(blacklist_dayskip, world.getName())){
                                CoreUtils.log("PIS DS - World - On blacklist.");
                                return;
                            }
                        }
                    }
                    ItemStack[] inv = player.getInventory().getContents();
                    CoreUtils.debug("PIS DS got inventory");
                    boolean itmDaySkipper = false;
                    CoreUtils.debug("PIS DS itemdayskipper initilized");
                    if( config.getBoolean("dayskip.item_required", true) ){
                        for(ItemStack item:inv){
                            if(!(item == null)){
                                CoreUtils.debug("PIS DS item=" + item.getType().name());
                                if(Objects.requireNonNull(item.getItemMeta()).getDisplayName().equalsIgnoreCase("DaySkipper")){
                                    itmDaySkipper = true;
                                    CoreUtils.debug("PIS DS found the item");
                                    break;
                                }
                            }
                        }
                        CoreUtils.debug("PIS DS inventory iterator finished.");
                    }else {
                        CoreUtils.debug("PIS DS Item not required");
                        itmDaySkipper = true;
                    }
                    CoreUtils.debug("PIS DS dayskipperitemrequired = " + config.getBoolean("dayskip.item_required"));
                    if(itmDaySkipper){
                        CoreUtils.debug("PIS DS item DaySkipper is in inventory.");

                        Block block = event.getBed();
                        CoreUtils.debug("PIS DS block.material = " + block.getType());
                        CoreUtils.debug("PIS DS isBed(block) = " + plugin.isBed(block));
                        if ( plugin.isBed(block) ){
                            CoreUtils.debug("PIS DS the block is a bed.");
                            /* OK, they have the DaySkipper item, now check for the permission*/
                            if( Perms.DAYSKIPPER.hasPermissionOrOp(player) ){
                                CoreUtils.debug("PIS DS Has perm or is op. ...");

                                // Colors from config
                                String cancelBracketColor = config.getString("global.cancel_bracket_color", "YELLOW").toLowerCase();
                                CoreUtils.debug("PIS DS ... CancelBracketColor=<" + cancelBracketColor + ">");
                                String cancelcolor = config.getString("global.cancel_color", "RED").toLowerCase();
                                CoreUtils.debug("PIS DS ... cancelcolor=<" + cancelcolor + ">");

                                // Convert config colors to JSON-compatible colors
                                String jsonCancelBracketColor = messageConverter.convertToJsonColor(cancelBracketColor);
                                String jsonCancelColor = messageConverter.convertToJsonColor(cancelcolor);

                                String dayskipmsg;
                                if ( config.getBoolean("global.random_messages", true) ) {  // adjust config key name as needed
                                    ConfigurationSection dayskipSection = messages.getConfigurationSection("messages.dayskip");
                                    int messageCount = 0;

                                    if (dayskipSection != null) {
                                        messageCount = dayskipSection.getKeys(false).size();  // counts message_1, message_2, etc.
                                    }
                                    CoreUtils.debug("PIS DS ... actual messageCount under messages.dayskip = " + messageCount);

                                    int randomnumber = RandomNumber(messageCount);

                                    if (messageCount > 0) {
                                        dayskipmsg = messages.getString("messages.dayskip.message_" + randomnumber,
                                                "<#FFFFFF><player> <gradient:#000000:#FFFF00>wants to sleep the day away</gradient>...");

                                        if (dayskipmsg == null || dayskipmsg.trim().isEmpty()) {
                                            dayskipmsg = "<#FFFFFF><player> <gradient:#000000:#FFFF00>wants to sleep the day away</gradient>...";
                                            CoreUtils.warn("PIS DS Dayskip message #" + randomnumber + " was empty/null");
                                        }
                                    } else {
                                        dayskipmsg = "<#FFFFFF><player> <gradient:#000000:#FFFF00>wants to sleep the day away</gradient>...";
                                        CoreUtils.warn("PIS DS ... no dayskip messages configured (count=" + messageCount + ") - using default");
                                    }
                                } else {
                                    dayskipmsg = "<#FFFFFF><player> <gradient:#000000:#FFFF00>wants to sleep the day away</gradient>...";
                                    CoreUtils.debug("PIS DS ... randomdayskipmsgs=false");
                                }

                                dayskipmsg = dayskipmsg.replace("<colon>", ":");  // keep if you still use this placeholder

                                if (dayskipmsg.trim().length() < 13) {
                                    dayskipmsg = "<#FFFFFF><player> <gradient:#000000:#FFFF00>wants to sleep the day away</gradient>...";
                                    CoreUtils.warn("PIS DS Final dayskipmsg too short/empty - using default");
                                }

                                CoreUtils.debug("PIS DS dayskipmsg=" + dayskipmsg);
                                dayskipmsg = coreUtils.fixColors(dayskipmsg);
                                CoreUtils.debug("PIS DS fixHexCodes dayskipmsg=" + dayskipmsg);

                                /* nickname parser */
                                String nickName = plugin.getNickname(player);
                                if(!nickName.contains("§")){
                                    CoreUtils.debug("PIS DS nickName !contain §");
                                }else{
                                    nickName = coreUtils.fixColors(nickName);
                                    CoreUtils.debug("PIS DS nick contains §" );
                                    CoreUtils.debug("PIS DS nickName AfterParse = " + nickName );
                                }
                                /* end nickname parser */

                                dayskipmsg = dayskipmsg.replace("<player>", nickName);
                                CoreUtils.debug("PIS DS ... dayskipmsg=" + dayskipmsg);

                                // Convert to JSON
                                String jsonDayskipmsg = messageConverter.convert(dayskipmsg, nickName);
                                CoreUtils.debug("PIS DS jsonDayskipmsg=" + jsonDayskipmsg);

                                // Construct cancel message as JSON
                                String jsonCanmsg = "[{\"text\":\"[\",\"color\":\"" + jsonCancelBracketColor + "\"}," +
                                        "{\"text\":\"" + plugin.get("sps.message.dayskipcancel") + "\",\"color\":\"" + jsonCancelColor + "\"," +
                                        "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"}," +
                                        "\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"" + plugin.get("sps.message.dayskipclickcancel", "Click to cancel Dayskip") + "\"}}," +
                                        "{\"text\":\"]\",\"color\":\"" + jsonCancelBracketColor + "\"}]";
                                CoreUtils.debug("PIS DS jsonCanmsg=" + jsonCanmsg);

                                // Broadcast message
                                SinglePlayerSleep.DisplayCancel = SinglePlayerSleep.dayskipDisplayCancel;
                                if ( config.getBoolean("global.broadcast_per_world", true) ) {
                                    plugin.sendJson(player.getWorld(), jsonDayskipmsg, jsonCanmsg);
                                } else {
                                    plugin.sendJson(jsonDayskipmsg, jsonCanmsg);
                                }
                                CoreUtils.debug("PIS DS SendAllJsonMessage. ...");
                                if(!isDSCanceled){
                                    CoreUtils.debug("PIS DS !isDSCanceled. ...");
                                    plugin.setDaySkipInProgress(true);

                                    plugin.setDaySkipTask(dayskipTask = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                                        @Override public void run() {
                                            plugin.setDStime(player, world);
                                            setTasksOff();
                                            CoreUtils.debug("PIS DS setDStime has run. ...");
                                        }
                                    }, dayskipdelay * 20L));
                                    CoreUtils.debug("PIS DS Scheduled day skip task ID=" + plugin.getDaySkipTask());

                                }else{
                                    isDSCanceled = false;
                                }
                                return;
                            }else{
                                player.sendMessage(ChatColor.YELLOW + plugin.get("sps.message.noperm"));
                            }
                        }else{
                            CoreUtils.debug("PIS DS block is not a Bed");
                            player.sendMessage(ChatColor.YELLOW + plugin.get("sps.message.dayskipblackbed"));/* NOT A BLACK BED */
                            return;
                        }
                    }else {
                        CoreUtils.log("PIS DS it is Day, Item is required, Item is not in inventory.");
                        isCanceled =  false;
                        if(!isThundering){
                            CoreUtils.debug(ChatColor.RED + "** End PlayerBedEnterEvent DS **");
                            return;
                        }
                    }
                } else {
                    CoreUtils.debug("PIS DS isDay=false");
                }
            } else {
                CoreUtils.debug("PIS DS enabledayskipper=false");
            }
            if( !plugin.isBloodmoonInprogress( player.getWorld() ) ){
                if(event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK){
                    // Check it's night or if storm
                    String worldName = player.getWorld().getName();

                    long worldTime = player.getWorld().getTime();  // 0-23999 ticks
                    CoreUtils.debug("PIS NC [{}] Is thundering? {} | Is night? {} | Time: {}",
                            worldName, isThundering, isNightTime, worldTime);

                    if (isNightTime || isThundering) {
                        // TODO: Sleep Blacklist Check
                        if(!Perms.OP.hasPermission(player)){
                            if((blacklist_sleep != null)&&!blacklist_sleep.isEmpty()){
                                if(StrUtils.stringContains(blacklist_sleep, world.getName())){
                                    CoreUtils.log("PIS IN - World - On blacklist.");
                                    return;
                                }
                            }
                        }

                        // Set the default timer for when the player has never slept before
                        long timer = 0;
                        CoreUtils.debug("PIS IN... " + player.getName() + " is sleeping.");
                        long time = System.currentTimeMillis() / 1000;
                        if(sleeplimit.get(player.getUniqueId()) == null){
                            CoreUtils.debug("PIS IN sleeplimit UUID=null");
                            // Check if player has sps.unrestricted
                            if (!Perms.UNRESTRICTED.hasPermission(player)) {
                                // Set player's time in HashMap
                                sleeplimit.put(player.getUniqueId(), time);
                                CoreUtils.debug("PIS IN... " + player.getDisplayName() + " added to playersSlept");
                            }
                        }else{
                            CoreUtils.debug("PIS IN sleeplimit UUID !null");
                            // Player is on the list.
                            timer = sleeplimit.get(player.getUniqueId());
                            CoreUtils.debug("PIS IN time=" + time);
                            CoreUtils.debug("PIS IN timer=" + timer);
                            CoreUtils.debug("PIS IN time - timer=" +  (time - timer));
                            CoreUtils.debug("PIS IN sleeplimit=" + config.getLong("global.limits.use_attempts", 30));
                            // if !time - timer > limit
                            if(!((time - timer) > config.getLong("global.limits.use_attempts", 30))){
                                long length = config.getLong("global.limits.use_attempts", 30) - (time - timer) ;
                                String sleeplimit = plugin.get("sps.message.sleeplimit").replace("<length>", "" + length);
                                player.sendMessage(ChatColor.YELLOW + sleeplimit);
                                CoreUtils.debug("PIS IN... use_attempts: " + sleeplimit);
                                //player.sendMessage("You can not do that for " + length + " seconds");
                                event.setCancelled(true);
                                return;
                            }else if((time - timer) > config.getLong("global.limits.use_attempts", 30)){
                                CoreUtils.debug("PIS IN time - timer > use_attempts");
                                sleeplimit.replace(player.getUniqueId(), time);
                            }
                        }

                        //Check if players can sleep without the ability for others to cancel it
                        if ( config.getBoolean("sleep.unrestricted", false) ) {
                            CoreUtils.debug("PIS IN U unrestrictedsleep=true");
                            String dastring = plugin.get("sps.message.issleep");
                            dastring = dastring.replace("<player>", plugin.getNickname(player));
                            plugin.DisplayCancel = plugin.sleepDisplayCancel;
                            plugin.broadcast(dastring, world);
                            long startTime = System.currentTimeMillis();
                            plugin.setSleepInProgress(true);
                            plugin.setTransitionTaskUnrestricted(transitionTaskUnrestricted = this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override public void run() {
                                    plugin.setDatime(player, world);
                                    plugin.resetPlayersRestStat(world);
                                    String elapsedTime = coreUtils.LoadTime(startTime); // Reuse your existing method
                                    setTasksOff();
                                    CoreUtils.debug("PIS IN U setDatime has run. Elapsed: " + elapsedTime);
                                }
                            }, sleepdelay * 20L));
                            CoreUtils.debug("PIS: Scheduled unrestricted sleep task ID=" + plugin.getTransitionTaskUnrestricted());
                        }else //Don't show a cancel option if a player has unrestricted sleep perm
                            if ( Perms.UNRESTRICTED.hasPermission(player) ) { //TODO: Unrestricted Broadcast
                                CoreUtils.debug("PIS IN U Has unrestricted perm. ...");

                                String CancelBracketColor = "<" + config.getString("global.cancel_bracket_color", "YELLOW") + ">";
                                CoreUtils.debug("PIS IN U ... cancel_bracket_color=" + CancelBracketColor);
                                String canmsg = "";
                                String sleepmsg;
                                if ( config.getBoolean("global.random_messages", true) ){
                                    ConfigurationSection sleepSection = messages.getConfigurationSection("messages.sleep");
                                    int messageCount = 0;
                                    if (sleepSection != null) {
                                        messageCount = sleepSection.getKeys(false).size();  // Counts keys like message_1, message_2, etc.
                                    }
                                    CoreUtils.debug("PIS IN U ... actual messageCount under messages.sleep = " + messageCount);

                                    int randomnumber = RandomNumber(messageCount);
                                    if (messageCount > 0) {
                                        sleepmsg = messages.getString("messages.sleep.message_" + randomnumber, "<#FFFFFF><player> <#FFAA00>went to bed. Sweet Dreams!");
                                        if (sleepmsg == null || sleepmsg.trim().isEmpty()) {
                                            sleepmsg = "<#FF0000><player> <#FFAA00>went to bed. Sweet Dreams!";
                                            CoreUtils.warn("PIS IN U Sleep message " + randomnumber + " was empty/null");
                                        }
                                    } else {
                                        sleepmsg = "<#FFFFFF><player> <#FFAA00>went to bed. Sweet Dreams!";
                                        CoreUtils.warn("PIS IN U ... no sleep messages configured (count=" + messageCount + ") - using default");
                                    }
                                    sleepmsg = sleepmsg.replace("<colon>", ":");
                                }else{
                                    sleepmsg = "<#FFFFFF><player> <#FFAA00>went to bed. Sweet Dreams!";
                                    CoreUtils.debug("PIS IN U ... randomsleepmsgs=false");
                                }
                                if (sleepmsg.trim().length() < 13) {
                                    sleepmsg = "<#FFFFFF><player> <#FFAA00>went to bed. Sweet Dreams!";
                                    CoreUtils.warn("PIS IN U Final sleepmsg too short/empty - using default");
                                }
                                CoreUtils.debug("PIS IN U sleepmsg=" + sleepmsg);
                                sleepmsg = coreUtils.fixColors(sleepmsg);
                                CoreUtils.debug("PIS IN U fixHexCodes sleepmsg=" + sleepmsg);

                                // nickname parser
                                String nickName = plugin.getNickname(player);
                                if(!nickName.contains("§")){
                                    CoreUtils.debug("PIS IN U nickName !contain §");
                                }else{
                                    nickName = coreUtils.fixColors(nickName);
                                    CoreUtils.debug("PIS IN U nick contains §" );
                                    CoreUtils.debug("PIS IN U nickName AfterParse = " + nickName );
                                }

                                // Convert to JSON
                                String jsonSleepmsg = messageConverter.convert(sleepmsg, nickName);
                                CoreUtils.debug("PIS IN U jsonSleepmsg=" + jsonSleepmsg);
                                SinglePlayerSleep.DisplayCancel = SinglePlayerSleep.sleepDisplayCancel;
                                if( config.getBoolean("global.broadcast_per_world", true) ){
                                    plugin.sendJson(player.getWorld(), jsonSleepmsg, canmsg);
                                }else{
                                    plugin.sendJson(jsonSleepmsg, canmsg);
                                }
                                CoreUtils.debug("PIS IN U SendAllJsonMessage. ...");

                                long startTime = System.currentTimeMillis();
                                plugin.setSleepInProgress(true);
                                plugin.setTransitionTaskUnrestricted(transitionTaskUnrestricted = this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                    @Override public void run() {
                                        plugin.setDatime(player, world);
                                        plugin.resetPlayersRestStat(world);
                                        String elapsedTime = coreUtils.LoadTime(startTime); // Reuse your existing method
                                        setTasksOff();
                                        CoreUtils.debug("PIS IN U setDatime has run. Elapsed: " + elapsedTime);
                                    }
                                }, sleepdelay * 20L));
                                CoreUtils.debug("PIS IN U Scheduled unrestricted sleep task ID=" + plugin.getTransitionTaskUnrestricted());

                            } else if(!isCanceled&&!event.isCancelled()){ // TODO: Normal Sleep
                                if ( Perms.HERMITS.hasPermissionOrOp(player) ) {
                                    CoreUtils.debug("PIS IN Has perm or is op.");

                                    // Colors from config
                                    String cancelBracketColor = config.getString("global.cancel_bracket_color", "YELLOW").toLowerCase();
                                    CoreUtils.debug("PIS IN ... cancel_bracket_color=<" + cancelBracketColor + ">");
                                    String cancelcolor = config.getString("global.cancel_color", "RED").toLowerCase();
                                    CoreUtils.debug("PIS IN ... cancel_color=<" + cancelcolor + ">");

                                    // Convert config colors to JSON-compatible colors
                                    String jsonCancelBracketColor = messageConverter.convertToJsonColor(cancelBracketColor);
                                    String jsonCancelColor = messageConverter.convertToJsonColor(cancelcolor);

                                    // Construct a sleep message
                                    String sleepmsg;
                                    if ( config.getBoolean("global.random_messages", true) ){
                                        ConfigurationSection sleepSection = messages.getConfigurationSection("messages.sleep");
                                        int messageCount = 0;
                                        if (sleepSection != null) {
                                            messageCount = sleepSection.getKeys(false).size();  // Counts keys like message_1, message_2, etc.
                                        }
                                        CoreUtils.debug("PIS IN ... actual messageCount under messages.sleep = " + messageCount);

                                        int randomnumber = RandomNumber(messageCount);
                                        if (messageCount > 0) {
                                            sleepmsg = messages.getString("messages.sleep.message_" + randomnumber, "<#FFFFFF><player> <#FFAA00>went to bed. Sweet Dreams!");
                                            if (sleepmsg == null || sleepmsg.trim().isEmpty()) {
                                                sleepmsg = "<#FF0000><player> <#FFAA00>went to bed. Sweet Dreams!";
                                                CoreUtils.warn("PIS IN Sleep message " + randomnumber + " was empty/null");
                                            }
                                        } else {
                                            sleepmsg = "<#FFFFFF><player> <#FFAA00>went to bed. Sweet Dreams!";
                                            CoreUtils.warn("PIS IN ... no sleep messages configured (count=" + messageCount + ") - using default");
                                        }
                                        sleepmsg = sleepmsg.replace("<colon>", ":");
                                    }else{
                                        sleepmsg = "<#FFFFFF><player> <#FFAA00>went to bed. Sweet Dreams!";
                                        CoreUtils.debug("PIS IN ... randomsleepmsgs=false");
                                    }
                                    if (sleepmsg.trim().length() < 13) {
                                        sleepmsg = "<#FFFFFF><player> <#FFAA00>went to bed. Sweet Dreams!";
                                        CoreUtils.warn("PIS IN Final sleepmsg too short/empty - using default");
                                    }
                                    CoreUtils.debug("PIS IN sleepmsg=" + sleepmsg);

                                    // Fix old hex codes
                                    sleepmsg = coreUtils.fixColors(sleepmsg);
                                    CoreUtils.debug("PIS IN fixHexCodes sleepmsg=" + sleepmsg);

                                    // Nickname parser
                                    String nickName = plugin.getNickname(player);
                                    if (nickName.contains("§") || nickName.contains("&")) {
                                        nickName = coreUtils.fixColors(nickName);
                                        CoreUtils.debug("PIS IN nick contains § or &");
                                        CoreUtils.debug("PIS IN nickName AfterParse = " + nickName);
                                    } else {
                                        CoreUtils.debug("PIS IN nickName !contain § or &");
                                    }

                                    // Convert to JSON
                                    String jsonSleepmsg = messageConverter.convert(sleepmsg, nickName);
                                    CoreUtils.debug("PIS IN jsonSleepmsg=" + jsonSleepmsg);

                                    // Construct cancel message as JSON
                                    String jsonCanmsg = "[{\"text\":\"[\",\"color\":\"" + jsonCancelBracketColor + "\"}," +
                                            "{\"text\":\"" + plugin.get("sps.message.cancel") + "\",\"color\":\"" + jsonCancelColor + "\"," +
                                            "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"}," +
                                            "\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"" + plugin.get("sps.message.clickcancel", "Click to cancel sleep") + "\"}}," +
                                            "{\"text\":\"]\",\"color\":\"" + jsonCancelBracketColor + "\"}]";
                                    CoreUtils.debug("PIS IN jsonCanmsg=" + jsonCanmsg);

                                    // Broadcast message
                                    SinglePlayerSleep.DisplayCancel = SinglePlayerSleep.sleepDisplayCancel;
                                    if ( config.getBoolean("global.broadcast_per_world", true) ) {
                                        plugin.sendJson(player.getWorld(), jsonSleepmsg, jsonCanmsg);
                                    } else {
                                        plugin.sendJson(jsonSleepmsg, jsonCanmsg);
                                    }
                                    CoreUtils.debug("PIS IN SendAllJsonMessage.");

                                    // Schedule time transition
                                    if (!isCanceled && !event.isCancelled()) {
                                        CoreUtils.debug("PIS IN !isCanceled.");
                                        long startTime = System.currentTimeMillis();
                                        plugin.setSleepInProgress(true);
                                        plugin.setTransitionTask(transitionTask = this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                            // Check if it's already daytime (0 to 12000 ticks is roughly daytime)
                                            long currentTime = world.getTime();
                                            if ( currentTime >= 0 && currentTime < 12000 && !isThundering ) {
                                                CoreUtils.log("Time change canceled, already day (time: " + currentTime + ") and not Thunderstorm");
                                                return; // Skip time change
                                            }

                                            // Proceed with time change
                                            plugin.setDatime(player, world);
                                            plugin.resetPlayersRestStat(world);
                                            String elapsedTime = coreUtils.LoadTime(startTime); // Reuse your existing method
                                            setTasksOff();
                                            CoreUtils.debug("PIS IN setDatime has run. Elapsed: " + elapsedTime);
                                        }, sleepdelay * 20L));
                                        CoreUtils.debug("PIS: Scheduled normal sleep task ID=" + plugin.getTransitionTask());
                                    } else {
                                        if (isCanceled) {
                                            CoreUtils.debug("PIS IN isCanceled=" + true);
                                        }
                                        if (event.isCancelled()) {
                                            CoreUtils.debug("PIS IN event.isCanceled=" + event.isCancelled());
                                        }
                                        isCanceled = false;
                                    }
                                } else {
                                    player.sendMessage(ChatColor.YELLOW + plugin.get("sps.message.noperm"));
                                }
                            }else{
                                if(isCanceled){CoreUtils.debug("PIS IN isCanceled=" + isCanceled);}
                                isCanceled = false;
                                if(event.isCancelled()){CoreUtils.debug("PIS event.isCanceled=" + event.isCancelled());}
                            }
                    }else{ // It is not Night or Storming, so tell the player
                        if( config.getBoolean("sleep.notify_must_be_night", true) ){
                            player.sendMessage(ChatColorUtils.setColors(plugin.get("sps.message.nightorstorm")));
                            CoreUtils.debug("PIS IN it was not night||thundering and player was notified. ...");
                        }else{
                            CoreUtils.debug("PIS IN it was not night||thundering and player was NOT notified. ...");
                        }
                        //if(debug){logDebug("getBedSpawnLocation=" + player.getBedSpawnLocation());}
                        //if(debug){logDebug("getBed=" + event.getBed().getLocation());}
                        //player.getBedSpawnLocation().equals(event.getBed().getLocation()
                        //String sv = serverVersion();
                        Version sVersion = Version.getCurrentVersion();
                        if(!sVersion.isAtLeast(new Version("1.15"))){
                            Block bed = event.getBed();
                            Location bedSpawn = player.getBedSpawnLocation();
                            if(bedSpawn != null){
                                boolean isSameBed = plugin.checkradius(bedSpawn, event.getBed().getLocation(), 5);
                                if (!isSameBed||player.getBedSpawnLocation().equals(null)) {
                                    if(player.getBedSpawnLocation().equals(null)){
                                        CoreUtils.debug("PIS !IN bedspawn=null");
                                    }else if(!isSameBed){
                                        CoreUtils.debug("PIS !IN bedspawn!=bed");
                                    }
                                    player.setBedSpawnLocation(event.getBed().getLocation());
                                    player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + plugin.get("sps.message.respawnpointmsg").replace("<x>", "" + bed.getX()).replace("<z>", "" + bed.getZ()));
                                    CoreUtils.debug("PIS !IN bedspawn was set for player " + ChatColor.GREEN + player.getDisplayName() + ChatColor.RESET + " ...");
                                }
                            }else{
                                player.setBedSpawnLocation(event.getBed().getLocation());
                                player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + plugin.get("sps.message.respawnpointmsg").replace("<x>", "" + bed.getX()).replace("<z>", "" + bed.getZ()));
                                CoreUtils.debug("PIS !IN bedspawn was set for player " + ChatColor.GREEN + player.getDisplayName() + ChatColor.RESET + " ...");
                            }
                        } else {CoreUtils.debug("PIS !IN Server is 1.15+");
                        }
                    }
                }else{
                    CoreUtils.debug("PIS getBedEnterResult()=" + event.getBedEnterResult().toString() );
                }
            }else{
                player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + plugin.get("sps.message.bloodmoon", "You can not sleep during a bloodmoon."));
                CoreUtils.debug("PIS Canceled due to BloodMoon");
                event.setCancelled(true);
            }
            CoreUtils.debug(ChatColor.RED + "** End PlayerBedEnterEvent IN **");
            isCanceled =  false;
        } catch (Exception exception) {
            reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_ENTER_BED_EVENT).error(exception));
        }
    }
    public void setTasksOff(){
        plugin.dayskipTask = -1;
        plugin.transitionTaskUnrestricted = -1;
        plugin.transitionTask = -1;
    }

}
