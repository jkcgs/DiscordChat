package com.makzk.spigot.discordchat;

/**
 * Async task to login to the Discord services
 */
class LoginTask implements Runnable {
    DiscordChat plugin = null;

    public LoginTask(DiscordChat plugin) {
        this.plugin = plugin;
    }

    public void run() {
        if(!plugin.getWrapper().init(true) || !plugin.getWrapper().isConnected()) {
            plugin.getLogger().severe(plugin.lang("error-discord-login", plugin.lang("error-no-further-info")));
        } else {
            plugin.getLogger().info(plugin.lang("discord-logged"));
        }
    }
}