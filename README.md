# DiscordChat

This is a Spigot/Bukkit plugin that connects a Discord client (vía [Discord4J](https://github.com/austinv11/Discord4J)) with a Minecraft server, by showing the Discord messages sent to
a channel to the Minecraft chat, and vise versa.

## Installation

Put the plugin's jar into the servers plugin folder. You can either load the plugin and load the default settings, or
use the following template to avoid having to load the plugin twice.

## Configuration

The configuration goes in the `plugins/DiscordChat` folder, named `config.yml`.

```yml
discord-email: 'Your Discord email'
discord-password: 'Your Discord password'

# GLOBAL SWITCHES: the parameters below can be also set for each channel,
# and they will override the global ones.

# Filters messages from FactionChat.
# If false, the messages will be shown as "[Faction ChatType] player: msg"
filter-factionchat: true

# Filters the messages from players muted with Essentials
# If false, the messages will be shown as "[Muted] player: msg"
filter-essmute: true

# Update topic to display online players on Minecraft server
online-players-on-topic: true

# If the topic is "Check this", the final topic would be "Check this - Online players: n"
online-players-topic-separator: ' - '

# Channels config
channels:
  channel-id: # The channel ID is normally a long number
    prefix: '[Tag] ' # Prefix to display messages on Minecraft chat
    discord-listen: true # Listen Discord -> Minecraft
    minecraft-listen: true # Listen Minecraft -> Discord
```

## Commands and permission nodes

* `/dcreload` (`/dcr`): Reloads the plugin configuration - `discordbot.reload` (default: op)

## Languages and translations

You can switch between available languages. Actually, English (en) and Spanish (es).
You can use custom languages, by putting a file like `lang_{lang}.yml` in plugin folder, and set the `{lang}` in
the config. Use the file [lang.yml](src/main/resources/lang.yml) as base.

## License

This project is under the [MIT License](LICENSE)
