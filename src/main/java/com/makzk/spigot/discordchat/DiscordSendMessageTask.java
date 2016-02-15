package com.makzk.spigot.discordchat;

import org.bukkit.event.Event;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.HTTP429Exception;

import java.util.Map;

public class DiscordSendMessageTask implements Runnable {
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

        Map<String, ChannelConfig> channels = wrapper.getChannels();
        for(Map.Entry<String, ChannelConfig> entry: channels.entrySet()) {
            IChannel iChannel = wrapper.getClient().getChannelByID(entry.getKey());
            ChannelConfig cc = entry.getValue();

            if(iChannel == null || !cc.isMinecraftListen()) {
                continue;
            }

            try {
                if(event instanceof PlayerJoinEvent) {
                    iChannel.sendMessage(plugin.lang("discord-player-login", ((PlayerJoinEvent) event).getPlayer().getName()));
                }
                if(event instanceof PlayerQuitEvent) {
                    iChannel.sendMessage(plugin.lang("discord-player-logout", ((PlayerQuitEvent) event).getPlayer().getName()));
                }

                if(event instanceof AsyncPlayerChatEvent) {
                    AsyncPlayerChatEvent e = (AsyncPlayerChatEvent) event;
                    if(e.getRecipients().size() != plugin.getServer().getOnlinePlayers().size()) {
                        iChannel.sendMessage(e.getPlayer().getName() + ": " + e.getMessage());
                    }

                }

            } catch (MissingPermissionsException e) {
                plugin.getLogger().severe(plugin.lang("error-discord-missing-perm"));
            } catch (HTTP429Exception | DiscordException e) {
                plugin.getLogger().severe(plugin.lang("error-discord-send-msg", e.getMessage()));
            }
        }
    }
}
