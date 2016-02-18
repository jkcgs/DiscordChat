package com.makzk.spigot.discordchat;

import sx.blah.discord.handle.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class DiscordListener {
    ClientWrapper wrapper = null;
    DiscordChat plugin = null;

    public DiscordListener(ClientWrapper wrapper) {
        this.plugin = DiscordChat.getInstance();
        this.wrapper = wrapper;
    }

    @EventSubscriber
    public void onReadyEvent(ReadyEvent event) {
        plugin.getLogger().info(plugin.lang("ready"));

        // Determine server IP
        URL ipAddress;
        String ip = plugin.lang("some-server");
        try {
            // This URL returns the external IP of the 'request' client
            ipAddress = new URL("http://myexternalip.com/raw");
            BufferedReader in = new BufferedReader(new InputStreamReader(ipAddress.openStream()));
            ip = in.readLine();
        } catch(IOException e) {
            plugin.getLogger().warning(plugin.lang("error-ext-ip"));
        }

        wrapper.channelBroadcast(plugin.lang("discord-connected-from", ip));
    }

    @EventSubscriber
    public void onMessageReceivedEvent(MessageReceivedEvent event) {
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
