package com.makzk.spigot.discordchat;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.HTTP429Exception;

import java.util.Map;

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

        boolean filter = plugin.getConfig().getBoolean("filter-different-recipients");
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
                    if(!filter || e.getRecipients().size() != plugin.getServer().getOnlinePlayers().size()) {
                        String msg = e.getMessage();
                        msg = msg.replaceAll("[#\\*_\\[`]", "");
                        iChannel.sendMessage("**" + e.getPlayer().getName() + "**: " + msg);
                    }
                }

            } catch (MissingPermissionsException e) {
                plugin.getLogger().severe(plugin.lang("error-discord-missing-perm"));
            } catch (HTTP429Exception | DiscordException e) {
                plugin.getLogger().severe(plugin.lang("error-discord-send-msg", e.getMessage()));
            } catch (Exception e) {
                plugin.getLogger().severe(plugin.lang("error-discord-unknown",
                        e.getClass().getName() + ": " + e.getMessage()));
            }
        }
    }
}
