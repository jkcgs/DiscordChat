package com.makzk.spigot.discordchat;

import nz.co.lolnet.james137137.FactionChat.FactionChatAPI;
import com.earth2me.essentials.Essentials;

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
import java.util.Optional;

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
        MessageType type = MessageType.MESSAGE_MC_NORMAL;
        String finalMsg = "";
        if(event instanceof PlayerJoinEvent) {
            finalMsg = plugin.lang("discord-player-login", ((PlayerJoinEvent) event).getPlayer().getName());
            updateChannelTopic();
        }
        if(event instanceof PlayerQuitEvent) {
            finalMsg = plugin.lang("discord-player-logout", ((PlayerQuitEvent) event).getPlayer().getName());
            updateChannelTopic();
        }

        if(event instanceof AsyncPlayerChatEvent) {
            AsyncPlayerChatEvent e = (AsyncPlayerChatEvent) event;

            String msg = e.getMessage();
            msg = "**" + DiscordChat.escape(e.getPlayer().getName()) + "**: " + DiscordChat.escape(msg);

            // Filter or format the message if it's a FactionChat message
            if(FactionChatAPI.isFactionChatMessage(e)) {
                String fName = FactionChatAPI.getFactionName(e.getPlayer());
                String fChatMode = FactionChatAPI.getChatMode(e.getPlayer());
                msg = String.format("[%s %s] %s", fName, fChatMode, msg);
                type = MessageType.MESSAGE_MC_FACTION;
            }

            // Handle muted players
            if(plugin.isEssEnabled()) {
                Essentials ess = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
                if(ess.getUser(e.getPlayer().getName()).isMuted()) {
                    msg = "[" + plugin.lang("muted") + "] " + msg;
                    type = MessageType.MESSAGE_MC_MUTED;
                }
            }

            finalMsg = msg;
        }

        // Finnally, broadcast message to Minecraft-listen enabled channels
        if(!finalMsg.isEmpty()) wrapper.channelBroadcast(finalMsg, type);
    }

    /**
     * Updates the channels (if they are setup to do it), adding the online players on the server.
     */
    public void updateChannelTopic() {
        String gSeparator = plugin.getConfig().getString("online-players-topic-separator");
        Map<String, ChannelConfig> channels = wrapper.getChannels();
        for(Map.Entry channelEntry: channels.entrySet()) {
            ChannelConfig cc = (ChannelConfig)channelEntry.getValue();
            IChannel channel = wrapper.getClient().getChannelByID(cc.getId());
            if(channel == null) return;

            String separator = cc.getTopicSeparator();
            separator = separator == null ? gSeparator : separator;
            String topic = channel.getTopic();
            String newTopic;

            // Determine string to append
            int online = plugin.getServer().getOnlinePlayers().size();
            String sepOnline = online + " players online";

            // Determine new topic string
            if(topic.isEmpty()) {
                newTopic = sepOnline;
            } else {
                int sepPos = topic.indexOf(separator);
                String prevTopic = sepPos != -1 ? topic.substring(0, sepPos) : topic;
                newTopic = prevTopic + separator + sepOnline;
            }

            // Attempt to change the topic
            try {
                channel.edit(Optional.empty(), Optional.empty(), Optional.of(newTopic));
            } catch (DiscordException | HTTP429Exception e) {
                plugin.getLogger().warning(plugin.lang("error-discord-topic", channel.getName(), e.getMessage()));
            } catch (MissingPermissionsException e) {
                plugin.getLogger().warning(plugin.lang("error-discord-topic-perm", channel.getName()));
            } catch (Exception e) {
                plugin.getLogger().severe(plugin.lang("error-discord-unknown",
                        "ClientWrapper#updateChannelTopic: " + e.getClass().getName() + ": " + e.getMessage()));
            }
        }
    }
}
