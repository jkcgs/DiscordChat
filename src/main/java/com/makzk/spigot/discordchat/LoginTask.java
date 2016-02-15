package com.makzk.spigot.discordchat;

import sx.blah.discord.api.DiscordException;

class LoginTask implements Runnable {
    DiscordChat plugin = null;

    public LoginTask(DiscordChat plugin) {
        this.plugin = plugin;
    }

    public void run() {
        try {
            plugin.getWrapper().login();

            if(!plugin.getWrapper().isConnected()) {
                throw new DiscordException(plugin.lang("error-no-further-info"));
            } else {
                plugin.getLogger().info(plugin.lang("discord-logged"));
            }
        } catch (DiscordException e) {
            plugin.getLogger().severe(plugin.lang("error-discord-login", e.getMessage()));
        }
    }
}