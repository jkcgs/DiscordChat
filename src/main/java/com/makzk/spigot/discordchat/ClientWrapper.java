package com.makzk.spigot.discordchat;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.IDiscordClient;

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

    public ClientWrapper(DiscordChat plugin) throws DiscordPluginException, DiscordException {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();

        if(config.getString("discord-email").isEmpty() || config.getString("discord-password").isEmpty()) {
            throw new DiscordPluginException(plugin.lang("error-login-config"));
        }

        // Create ClientBuilder, which returns the client to interact with the Discord uAPI
        ClientBuilder builder = new ClientBuilder();
        builder.withLogin(config.getString("discord-email"), config.getString("discord-password"));
        client = builder.build();
        listener = new DiscordListener(plugin, this);
        client.getDispatcher().registerListener(listener);

        // Create Channels list from configuration
        Map<String, Object> channelsList = config.getConfigurationSection("channels").getValues(false);
        channels = new HashMap<>();
        for (Map.Entry<String, Object> entry : channelsList.entrySet()) {
            ConfigurationSection s = (ConfigurationSection) entry.getValue();
            ChannelConfig c = new ChannelConfig(
                    entry.getKey(),
                    s.getString("tag"),
                    s.getBoolean("discord-listen"),
                    s.getBoolean("minecraft-listen")
            );

            // Avoid duplicated channels
            if(getChannel(entry.getKey()) == null) {
                channels.put(entry.getKey(), c);
            }
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void unload() {
        client.getDispatcher().unregisterListener(listener);
    }

    public void login() throws DiscordException {
        if(connected || client == null) {
            return;
        }

        try {
            client.login();
            connected = true;
        } catch (DiscordException e) {
            connected = false;
            throw e;
        }
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
