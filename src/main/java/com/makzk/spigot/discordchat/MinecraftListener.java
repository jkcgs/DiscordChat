package com.makzk.spigot.discordchat;

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
