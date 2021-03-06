package com.makzk.spigot.discordchat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscordChat extends JavaPlugin {
    private static DiscordChat instance;

    private final FileConfiguration f = getConfig();
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
        } catch (DiscordPluginException e) {
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

    public String lang(String path, String... format) {
        //noinspection ConfusingArgumentToVarargsMethod
        return String.format(lang(path), format);
    }

    public void unloadPlugin() {
        getPluginLoader().disablePlugin(this);
    }

    public ClientWrapper getWrapper() {
        return wrapper;
    }

    public boolean isEssEnabled() {
        return essEnabled;
    }

    public static DiscordChat getInstance() {
        return instance;
    }

    /**
     * Escapes text with format to Discord
     * @param str The string to escape
     * @return The escaped string
     */
    public static String escape(String str) {
        str = str.replace("*", "*****"); // Escape '*' (workaround)
        str = str.replace("_", "_____"); // Escape '_' (yet another workaround)
        str = str.replace("`", "'"); // Escape ` as it's parsed by Discord, like previous characters
        return str;
    }
}
