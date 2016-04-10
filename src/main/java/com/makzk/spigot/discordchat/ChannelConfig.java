package com.makzk.spigot.discordchat;

/**
 * Object that represents an Channel from the configuration
 */
public class ChannelConfig {
    private final String id;
    private final String prefix;
    private String topicSeparator;
    private boolean discordListen = false;
    private boolean minecraftListen = false;
    private Boolean filterFactionChat = false;
    private Boolean filterEMuted = false;

    public ChannelConfig(String id, String prefix, boolean discordListen, boolean minecraftListen) {
        this.id = id;
        this.prefix = prefix;
        this.discordListen = discordListen;
        this.minecraftListen = minecraftListen;
    }

    public String getId() {
        return id;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getTopicSeparator() {
        return topicSeparator;
    }

    public void setTopicSeparator(String topicSeparator) {
        this.topicSeparator = topicSeparator;
    }

    public boolean isMinecraftListen() {
        return minecraftListen;
    }

    public boolean isDiscordListen() {
        return discordListen;
    }

    public void setFilterEMuted(Boolean filterEMuted) {
        this.filterEMuted = filterEMuted;
    }

    public Boolean isFilterFactionChat() {
        return filterFactionChat;
    }

    public Boolean isFilterEMuted() {
        return filterEMuted;
    }

}
