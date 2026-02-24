package com.github.joelgodofwar.sps.util;

import org.bukkit.World;

import java.util.Random;

public class SPSUtil {
    private static long mobSpawningStartTime = 12541;//12600;
    //mobs stop spawning at: 22813
    //mobs start to burn at: 23600
    private static long mobSpawningStopTime = 23600;

    public static boolean IsNight(World w){
        long time = (w.getFullTime()) % 24000;
        return (time >= mobSpawningStartTime) && (time < mobSpawningStopTime);
    }

    public static boolean IsDay(World w){
        long time = (w.getFullTime()) % 24000;
        return (time > 0) && (time < 12300);
        //return time >= mobSpawningStartTime && time < mobSpawningStopTime;
    }

    public static int RandomNumber(int maximum){
        Random rand = new Random();
        int min = 1;
        // nextInt as provided by Random is exclusive of the top value so you need to add 1
        int randomNum;
        try {
            randomNum = rand.nextInt((maximum - min) + 1) + min;
        }catch(Exception exception) {
            randomNum = 0;
        }
        return randomNum;
    }
}
