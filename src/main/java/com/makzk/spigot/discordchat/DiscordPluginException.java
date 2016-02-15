package com.makzk.spigot.discordchat;

public class DiscordPluginException extends Exception {
    private String message;

    /**
     * @param message The error message
     */
    public DiscordPluginException(String message) {
        super(message);
        this.message = message;
    }

}
