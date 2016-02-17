package com.makzk.spigot.discordchat;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.HTTP429Exception;

import java.util.HashMap;
import java.util.Map;

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

            channels.put(c.getId(), c);
        }
    }

    /**
     * Broadcasts a message to the channels
     * @param msg The message to broadcast
     * @param listenMinecraft Send message only to Minecraft-listen-enabled channels
     */
    public void channelBroadcast(String msg, boolean listenMinecraft) {
        if(!connected) return;

        for (Map.Entry<String, ChannelConfig> entry : channels.entrySet()) {
            ChannelConfig c = entry.getValue();
            IChannel chan = client.getChannelByID(c.getId());
            if(chan == null) continue;

            // Broadcast only if channel listens to Minecraft messages, if the flag is enabled
            if(listenMinecraft && !c.isMinecraftListen()) {
                continue;
            }

            try {
                chan.sendMessage(msg);
            } catch (MissingPermissionsException e) {
                plugin.getLogger().warning(plugin.lang("error-discord-broad-perm", chan.getName()));
            } catch (HTTP429Exception | DiscordException e) {
                plugin.getLogger().warning(plugin.lang("error-discord-broad", chan.getName(), e.getMessage()));
            } catch (Exception e) {
                plugin.getLogger().severe(plugin.lang("error-discord-unknown",
                        e.getClass().getName() + ": " + e.getMessage()));
            }
        }
    }

    public void channelBroadcast(String msg) {
        channelBroadcast(msg, false);
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
