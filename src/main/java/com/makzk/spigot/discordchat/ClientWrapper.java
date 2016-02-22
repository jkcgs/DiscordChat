package com.makzk.spigot.discordchat;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.HTTP429Exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

enum MessageType {
    MESSAGE_NORMAL,
    MESSAGE_MC_NORMAL,
    MESSAGE_MC_FACTION,
    MESSAGE_MC_MUTED,
}

/**
 * Connects to Discord with the Discord4J client
 */
public class ClientWrapper {
    private IDiscordClient client = null;
    private DiscordListener listener = null;
    private DiscordChat plugin = null;

    private Map<String, ChannelConfig> channels = null;
    private boolean connected = false;

    public ClientWrapper() throws DiscordPluginException, DiscordException {
        plugin = DiscordChat.getInstance();
        FileConfiguration config = plugin.getConfig();

        if(config.getString("discord-email").isEmpty() || config.getString("discord-password").isEmpty()) {
            throw new DiscordPluginException(plugin.lang("error-login-config"));
        }

        listener = new DiscordListener(this);
    }

    /**
     * Initializes the system, by unloading first
     * @return boolean Depending on the success of the operation
     */
    public boolean init(boolean logout) {
        unload(logout);

        try {
            login();
        } catch (DiscordException e) {
            e.printStackTrace();
            return false;
        }

        loadChannels();
        return true;
    }

    /**
     * Creates Discord client and logins to the service
     * @throws DiscordException
     */
    public void login() throws DiscordException {
        if(connected) {
            return;
        }

        plugin.getLogger().info(plugin.lang("discord-logging-in"));
        FileConfiguration config = plugin.getConfig();

        try {
            // Create ClientBuilder, which returns the client to interact with the Discord uAPI
            ClientBuilder builder = (new ClientBuilder()).withLogin(
                    config.getString("discord-email"), config.getString("discord-password")
            );

            client = builder.login();
            client.getDispatcher().registerListener(listener);
            connected = true;
        } catch (DiscordException e) {
            connected = false;
            throw e;
        }
    }

    /**
     * Unregister messages listener, logouts, and unloads channels
     * @param logout Determines if we have to close session, to avoid cancelling the logout async task
     */
    public void unload(boolean logout) {
        try {
            if(client != null) {
                channelBroadcast(plugin.lang("plugin-disconnected", plugin.getName()));
                client.getDispatcher().unregisterListener(listener);
                if(logout) {
                    plugin.getLogger().info(plugin.lang("discord-logging-out"));
                    client.logout();
                    plugin.getLogger().info(plugin.lang("discord-logout"));
                }
            }
        } catch (HTTP429Exception | DiscordException e) {
            plugin.getLogger().warning("Logout error: " + e.getMessage());
        } finally {
            connected = false;
        }
    }

    public void loadChannels() {
        // Create Channels list from configuration
        FileConfiguration config = plugin.getConfig();
        Map<String, Object> channelsList = config.getConfigurationSection("channels").getValues(false);

        // Import channels as ChannelConfig objects into a map
        channels = new HashMap<>();
        for (Map.Entry<String, Object> entry : channelsList.entrySet()) {
            // Avoid duplicated keys
            if(getChannel(entry.getKey()) != null) continue;

            ConfigurationSection s = (ConfigurationSection) entry.getValue();
            ChannelConfig c = new ChannelConfig(
                    entry.getKey(),
                    s.getString("tag"),
                    s.getBoolean("discord-listen"),
                    s.getBoolean("minecraft-listen")
            );

            // If this flags are not set for the channel, they must remain null to use global value
            Object fFaction = s.get("filter-factionchat");
            Object fMuted = s.get("filter-factionchat");
            c.setFilterEMuted(fFaction == null ? null : (Boolean)fFaction);
            c.setFilterEMuted(fMuted == null ? null : (Boolean)fMuted);
            c.setTopicSeparator(s.getString("online-players-topic-separator"));

            channels.put(c.getId(), c);
        }
    }

    /**
     * Broadcasts a message to the channels
     * @param msg The message to broadcast
     * @param type The type of message, to filter it according to settings
     */
    public void channelBroadcast(String msg, MessageType type) {
        if(!connected) return;
        boolean gFilterFaction = plugin.getConfig().getBoolean("filter-factionchat");
        boolean gFilterMuted = plugin.getConfig().getBoolean("filter-essmute");

        for (Map.Entry<String, ChannelConfig> entry : channels.entrySet()) {
            ChannelConfig c = entry.getValue();
            IChannel chan = client.getChannelByID(c.getId());
            if(chan == null) continue;

            // Not listening to Minecraft messages
            if(type != MessageType.MESSAGE_NORMAL && !c.isMinecraftListen()) {
                return;
            }

            if(type == MessageType.MESSAGE_MC_FACTION) {
                Object idfFaction = c.isFilterFactionChat();

                if((idfFaction == null && gFilterFaction) || plugin.getConfig().getBoolean("filter-factionchat")) {
                    continue;
                }
            }

            if(type == MessageType.MESSAGE_MC_MUTED) {
                Object idfMuted = c.isFilterEMuted();
                if((idfMuted == null && gFilterMuted) || plugin.getConfig().getBoolean("filter-essmute")) {
                    continue;
                }
            }

            try {
                chan.sendMessage(msg);
            } catch (MissingPermissionsException e) {
                plugin.getLogger().warning(plugin.lang("error-discord-broad-perm", chan.getName()));
            } catch (HTTP429Exception | DiscordException e) {
                plugin.getLogger().warning(plugin.lang("error-discord-broad", chan.getName(), e.getMessage()));
            } catch (Exception e) {
                plugin.getLogger().severe(plugin.lang("error-discord-msg-unknown",
                        "ClientWrapper#channelBroadcast: " + e.getClass().getName() + ": " + e.getMessage()));
            }
        }
    }

    /**
     * Updates the channels (if they are setup to do it), adding the online players on the server.
     */
    public void updateChannelsTopic() {
        String gSeparator = plugin.getConfig().getString("online-players-topic-separator");
        for(Map.Entry channelEntry: channels.entrySet()) {
            ChannelConfig cc = (ChannelConfig)channelEntry.getValue();
            IChannel channel = client.getChannelByID(cc.getId());
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

    public void channelBroadcast(String msg) {
        channelBroadcast(msg, MessageType.MESSAGE_NORMAL);
    }

    public boolean isConnected() {
        return connected;
    }

    public Map<String, ChannelConfig> getChannels() {
        return channels;
    }

    public ChannelConfig getChannel(String id) {
        return channels.get(id);
    }

    public IDiscordClient getClient() {
        return client;
    }
}
