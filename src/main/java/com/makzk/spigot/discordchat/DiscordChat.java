package com.makzk.spigot.discordchat;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import sx.blah.discord.api.DiscordException;

public class DiscordChat extends JavaPlugin {
    private FileConfiguration f = getConfig();
    private ClientWrapper wrapper = null;

    @Override
    public void onEnable() {
        f.options().copyDefaults(true);
        saveConfig();

        try {
            wrapper = new ClientWrapper(this);
        } catch (DiscordPluginException | DiscordException e) {
            getLogger().warning("WrapperInit: " + e.getMessage());
            unloadPlugin();
            return;
        }

        getServer().getScheduler().runTaskAsynchronously(this, new LoginTask(this));
        getServer().getPluginManager().registerEvents(new MinecraftListener(this, wrapper), this);
    }

    @Override
    public void onDisable() {
        wrapper.unload();
        getLogger().info("Plugin disabled!");
    }

    public void unloadPlugin() {
        getPluginLoader().disablePlugin(this);
    }

    public ClientWrapper getWrapper() {
        return wrapper;
    }
}