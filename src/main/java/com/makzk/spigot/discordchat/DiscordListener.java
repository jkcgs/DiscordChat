package com.makzk.spigot.discordchat;

import sx.blah.discord.handle.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IMessage;

public class DiscordListener {
    ClientWrapper wrapper = null;
    DiscordChat plugin = null;

    public DiscordListener(DiscordChat plugin, ClientWrapper wrapper) {
        this.plugin = plugin;
        this.wrapper = wrapper;
    }

    @EventSubscriber
    public void onReadyEvent(ReadyEvent event) { //This method is called when the ReadyEvent is dispatched
        plugin.getLogger().info("Ready!");
    }

    @EventSubscriber
    public void onMessageReceivedEvent(MessageReceivedEvent event) { //This method is NOT called because it doesn't have the @EventSubscriber annotation
        IMessage m = event.getMessage();
        ChannelConfig cc = wrapper.getChannel(m.getChannel().getID());

        // Don't listen to this event if the channel is not configured, or
        // we are not listening to the channel
        if(cc == null || !cc.isDiscordListen()) {
            return;
        }

        // Not listening to our messages
        if(m.getAuthor().getID().equals(wrapper.getClient().getOurUser().getID())) {
            return;
        }

        // Send message to Minecraft
        String msgLog = String.format("[%s] %s: %s", cc.getTag(), m.getAuthor().getName(), m.getContent());
        plugin.getServer().broadcastMessage(msgLog);
    }
}
