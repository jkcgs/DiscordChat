package com.makzk.spigot.discordchat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import sx.blah.discord.api.DiscordException;

public class DiscordChat extends JavaPlugin {
    private static DiscordChat instance;

    private FileConfiguration f = getConfig();
    private PluginFile langConfig = null;
    private ClientWrapper wrapper = null;

    private boolean factionChatEnabled = false;
    private boolean essEnabled = false;

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

        if(getServer().getPluginManager().isPluginEnabled("FactionChat")) {
            factionChatEnabled = true;
            getLogger().info("Linked to FactionChat!");
        }
        if(getServer().getPluginManager().isPluginEnabled("Essentials")) {
            essEnabled = true;
            getLogger().info("Linked to Essentials!");
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

    public boolean isFactionChatEnabled() {
        return factionChatEnabled;
    }

    public boolean isEssEnabled() {
        return essEnabled;
    }

    public static DiscordChat getInstance() {
        return instance;
    }
}
