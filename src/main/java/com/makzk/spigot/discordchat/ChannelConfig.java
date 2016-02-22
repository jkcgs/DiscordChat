package com.makzk.spigot.discordchat;

/**
 * Object that represents an Channel from the configuration
 */
public class ChannelConfig {
    private String id;
    private String tag;
    private boolean discordListen = false;
    private boolean minecraftListen = false;
    private Boolean filterFactionChat = false;
    private Boolean filterEMuted = false;

    public ChannelConfig(String id, String tag, boolean discordListen, boolean minecraftListen) {
        this.id = id;
        this.tag = tag;
        this.discordListen = discordListen;
        this.minecraftListen = minecraftListen;
    }

    public String getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public boolean isMinecraftListen() {
        return minecraftListen;
    }

    public boolean isDiscordListen() {
        return discordListen;
    }

    public void setFilterEMuted(boolean filterEMuted) {
        this.filterEMuted = filterEMuted;
    }

    public void setFilterFactionChat(boolean filterFactionChat) {
        this.filterFactionChat = filterFactionChat;
    }

    public Boolean isFilterFactionChat() {
        return filterFactionChat;
    }

    public Boolean isFilterEMuted() {
        return filterEMuted;
    }

}
