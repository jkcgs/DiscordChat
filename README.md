# DiscordChat

This is a Spigot/Bukkit plugin that connects a Discord client (vía [Discord4J](https://github.com/austinv11/Discord4J)) with a Minecraft server, by showing the Discord messages sent to
a channel to the Minecraft chat, and vise versa.

## Installation

Put the plugin jar into the plugins server, and, before starting the server, create the configuration file with the
following example.

## Configuration

The configuration goes in the plugins/DiscordChat folder, named config.yml.

```yml
discord-email: 'Your Discord email'
discord-password: 'Your Discord password'
lang: en # Switch between available languages
filter-factionchat: true # Toggles display of messages sent via FactionChat plugin
filter-essmute: true # Toggles display of messages sent by Essentials-muted players

channels:
  channel-id: # The channel ID obtainable from the URL (e.g.: https://discordapp.com/channels/{server-id}/{channel-id}
    tag: Tag # Tag to show on Minecraft chat
    discord-listen: true # Enable Discord -> Minecraft connection
    minecraft-listen: true # Enable Minecraft -> Discord connection
```

Note: This plugins links to FactionChat plugin. If you decide to show the messages sent with this plugin, they will
show as "[faction_name chat_mode] player_name: message"

## Commands and permission nodes

* /dcreload (/dcr): Reloads the plugin configuration - discordbot.reload (default: op)

## Languages and translations

You can switch between available languages. Actually, English (en) and Spanish.
You can use custom languages, by putting a file like "lang_{lang}.yml" in plugin folder, and set the "{lang}" in
the config. Use the file [lang.yml](src/main/resources/lang.yml) as base.

## License

This project is under the [MIT License](LICENSE)
