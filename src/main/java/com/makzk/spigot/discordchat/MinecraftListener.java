package com.makzk.spigot.discordchat;

import nz.co.lolnet.james137137.FactionChat.FactionChatAPI;
import com.earth2me.essentials.Essentials;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MinecraftListener implements Listener {
    private DiscordChat plugin = null;
    private ClientWrapper wrapper;

    public MinecraftListener(DiscordChat plugin, ClientWrapper wrapper) {
        this.plugin = plugin;
        this.wrapper = wrapper;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getServer().getScheduler().runTaskAsynchronously(
                plugin,
                new DiscordSendMessageTask(plugin, wrapper, event)
        );
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getServer().getScheduler().runTaskAsynchronously(
                plugin,
                new DiscordSendMessageTask(plugin, wrapper, event)
        );
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        plugin.getServer().getScheduler().runTaskAsynchronously(
                plugin,
                new DiscordSendMessageTask(plugin, wrapper, event)
        );
    }
}

class DiscordSendMessageTask implements Runnable {
    private DiscordChat plugin = null;
    private ClientWrapper wrapper = null;
    private Event event = null;

    public DiscordSendMessageTask(DiscordChat plugin, ClientWrapper wrapper, Event event)  {
        this.plugin = plugin;
        this.wrapper = wrapper;
        this.event = event;
    }

    public void run() {
        // Do nothing if client is not connected
        if(!wrapper.isConnected()) {
            return;
        }

        // Process event and determine the message to be sent to the channels
        String finalMsg = "";
        if(event instanceof PlayerJoinEvent) {
            finalMsg = plugin.lang("discord-player-login", ((PlayerJoinEvent) event).getPlayer().getName());
        }
        if(event instanceof PlayerQuitEvent) {
            finalMsg = plugin.lang("discord-player-logout", ((PlayerQuitEvent) event).getPlayer().getName());
        }

        if(event instanceof AsyncPlayerChatEvent) {
            AsyncPlayerChatEvent e = (AsyncPlayerChatEvent) event;
            boolean filterFaction = plugin.getConfig().getBoolean("filter-factionchat") && plugin.isFactionChatEnabled();

            String msg = e.getMessage();
            msg = "**" + DiscordChat.escape(e.getPlayer().getName()) + "**: " + DiscordChat.escape(msg);
            boolean cancelled = false;

            // Filter or format the message if it's a FactionChat message
            if(plugin.isFactionChatEnabled() && FactionChatAPI.isFactionChatMessage(e)) {
                if(filterFaction) {
                    cancelled = true;
                } else {
                    String fName = FactionChatAPI.getFactionName(e.getPlayer());
                    String fChatMode = FactionChatAPI.getChatMode(e.getPlayer());
                    msg = String.format("[%s %s] %s", fName, fChatMode, msg);
                }
            }

            // Handle muted players
            if(!cancelled && plugin.isEssEnabled()) {
                Essentials ess = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
                if(ess.getUser(e.getPlayer().getName()).isMuted()) {
                    if(plugin.getConfig().getBoolean("filter-essmute")) {
                        cancelled = true;
                    } else {
                        msg = "[" + plugin.lang("muted") + "] " + msg;
                    }
                }
            }

            if(!cancelled) finalMsg = msg;
        }

        // Finnally, broadcast message to Minecraft-listen enabled channels
        if(!finalMsg.isEmpty()) wrapper.channelBroadcast(finalMsg, true);
    }
}
