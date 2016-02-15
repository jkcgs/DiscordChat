# DiscordChat

This is a Spigot plugin that connects a Discord client with a Minecraft server, by showing the Discord messages sent to
a channel to the Minecraft chat, and vise versa.

## Installation

Put the plugin jar into the plugins server, and, before starting the server, create the configuration file with the
following example.

## Configuration

The configuration goes in the plugins/DiscordChat folder, named config.yml.

```yml
discord-email: 'Your Discord email'
discord-password: 'Your Discord password'

channels:
  channel-id: # The channel ID obtainable from the URL (e.g.: https://discordapp.com/channels/{server-id}/{channel-id}
    tag: Tag # Tag to show on Minecraft chat
    discord-listen: true # Enable Discord -> Minecraft connection
    minecraft-listen: true # Enable Minecraft -> Discord connection
```

## Commands and permission nodes

There are no commands and permissions required at the moment.