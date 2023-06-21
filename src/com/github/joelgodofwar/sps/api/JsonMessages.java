package com.github.joelgodofwar.sps.api;


import net.minecraft.server.v1_16_R1.ChatMessageType;
import net.minecraft.server.v1_16_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_16_R1.PacketPlayOutChat;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

//made by Gamecube762
//Simple way to send Json Chat messages to players
//V1.0

public class JsonMessages {

    public static PacketPlayOutChat createPacketPlayOutChat(String s){return new PacketPlayOutChat(ChatSerializer.a(s), ChatMessageType.CHAT, null);}

    public static void SendJsonMessage(Player p, String s){( (CraftPlayer)p ).getHandle().playerConnection.sendPacket( createPacketPlayOutChat(s) );}

    public static void SendPlayerListJsonMessage(Player[] players, String s){
        PacketPlayOutChat a = createPacketPlayOutChat(s);
        for (Player p: players)
            ((CraftPlayer)p).getHandle().playerConnection.sendPacket(a);
    }

    public static void SendAllJsonMessage(String s){
        PacketPlayOutChat a = createPacketPlayOutChat(s);
        for (Player p: Bukkit.getOnlinePlayers())
            ((CraftPlayer)p).getHandle().playerConnection.sendPacket(a);
    }

}