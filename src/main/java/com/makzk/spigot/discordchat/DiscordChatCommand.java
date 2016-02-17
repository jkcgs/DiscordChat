package com.makzk.spigot.discordchat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Listens to the plugin commands
 */
public class DiscordChatCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        DiscordChat plugin = DiscordChat.getInstance();

        if(cmd.getName().equals("dcreload") || cmd.getAliases().contains(label)) {
            if(!sender.hasPermission("discordchat.reload")) {
                sender.sendMessage("You don't have permission to use this command");
                return true;
            }

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new DiscordChatReloadTask());
        }

        return true;
    }

}

class DiscordChatReloadTask implements Runnable {
    @Override
    public void run() {
        DiscordChat plugin = DiscordChat.getInstance();
        plugin.getLogger().info(plugin.lang("reloading"));
        plugin.getWrapper().init(true);
    }
}