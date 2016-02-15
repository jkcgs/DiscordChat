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
                throw new DiscordException("No further information given");
            } else {
                plugin.getLogger().info("Successfully connected to Discord");
            }
        } catch (DiscordException e) {
            plugin.getLogger().severe("Could not login to Discord: " + e.getMessage());
        }
    }
}