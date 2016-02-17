package com.makzk.spigot.discordchat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import sx.blah.discord.api.DiscordException;

public class DiscordChat extends JavaPlugin {
    private static DiscordChat instance;

    private FileConfiguration f = getConfig();
    private PluginFile langConfig = null;
    private ClientWrapper wrapper = null;

    @Override
    public void onEnable() {
        instance = this;

        f.options().copyDefaults(true);
        saveConfig();

        String langName = "lang.yml";
        String langFromCfg = f.getString("lang");
        if(getResource("lang_" + langFromCfg + ".yml") != null) {
            langName = "lang_" + langFromCfg + ".yml";
        }

        langConfig = new PluginFile(this, langName, langName);

        try {
            wrapper = new ClientWrapper();
        } catch (DiscordPluginException | DiscordException e) {
            getLogger().warning("WrapperInit: " + e.getMessage());
            unloadPlugin();
            return;
        }

        getServer().getScheduler().runTaskAsynchronously(this, new LoginTask(this));
        getServer().getPluginManager().registerEvents(new MinecraftListener(this, wrapper), this);
        getCommand("dcreload").setExecutor(new DiscordChatCommand());
    }

    @Override
    public void onDisable() {
        if(wrapper != null) {
            wrapper.unload(false);
        }

        getLogger().info(langConfig.getString("plugin-disabled"));
    }

    public String lang(String path) {
        return langConfig.getString(path);
    }

    public String lang(String path, String ...format) {
        return String.format(lang(path), format);
    }

    public void unloadPlugin() {
        getPluginLoader().disablePlugin(this);
    }

    public ClientWrapper getWrapper() {
        return wrapper;
    }

    public static DiscordChat getInstance() {
        return instance;
    }
}
