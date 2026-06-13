# DEVELOPMENT
This is a development branch, it will be closest to what I am currently working on within a few Dev versions.
Dev versions are designated as 1.16_2.13.44.D5 which means it is the 5th version of the dev of 1.16_2.13.44

# SinglePlayerSleep

Download Links

https://www.spigotmc.org/resources/single-player-sleep.68139/

https://dev.bukkit.org/projects/singleplayersleep2


## Introduction

First Off, I watch several Hermitcrafters, and after seeing them have issues with their Single Player Sleep command block, rain messes it up. I decided to make this simple plugin.

## Description

When a player right clicks on a bed, the plugin broadcasts `<player> is sleeping [CANCEL]`, if no one clicks on Cancel, then after 10 seconds the plugin will advance the time ahead to the next morning. Thus the day and difficulty is not affected. The plugin also checks for storms, and if the player has the permissions, it will clear them. There is also a permission for the Cancel command.

## Permissions

```
sps.update - description: Allows the player to check for updates.
    
sps.unrestricted - Allows the player to sleep uninterrupted. Other players will not be able to cancel it.

sps.hermits - Allows player to sleep when other players are online.

sps.downfall - Allows player to clear downfall.

sps.thunder - Allows player to clear thunderstorm.

sps.cancel - Allows player to cancel SPS of other players
    
sp.command - Allows player to use /sleep command, instead of a bed.

sps.* - Admin, grants all sps permissions.
```

If the player is the only player online, then the bed functions as normal, since the sleep function works before the 10 second delay.

\*/sleep still requires it to be night or storming to use. ie - after 13187

## Contributing

### Building the jar
Thanks to maven, building the jar is easy.
However, most of this project's dependencies are not available on a maven repository.
As such, their jars have to be downloaded individually and added to the local maven repository.
Due to potential licencing conflicts, spigot is not included for the time being; you'll have to build that yourself.

Fortunately, all of this can be pretty easy if you do the following.
(Note that if you're using an IDE there's an option somewhere to run maven commands so you don't have to use a shell.)
1. Add the spigot server jar to the `lib` folder.
   * ***NOTE:*** You'll need to go to their website and follow the instructions for using "buildtools.java" to create it.
1. Run this maven command in your project directory: `mvn validate`
   * *What does this do?* Validate will install these jars into your local maven repository so when it comes time to package everything maven knows where to look. (You can find your local repository in your user directory, under `.m2`.)
1. Run this maven command in your project directory: `mvn package`
1. Ta-da! You're done!