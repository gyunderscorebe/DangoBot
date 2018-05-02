package de.kaleidox.dangobot.bot.libraries;

import de.kaleidox.dangobot.DangoBot;
import de.kaleidox.dangobot.bot.Command;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.util.Arrays;
import java.util.Optional;

public enum HelpLibrary {
    HELP(Command.HELP, DangoBot.getBasicEmbed()
            .addField("Sends a detailed guide for the bots commands.", "" +
                    "Not implementet yet.\n" + // TODO: Implement
                    "Check out " + DangoBot.BOT_URL + " for a detailed list of Commands and what they do.")
            .addField("Did you know?", "" +
                    "When you click on " + DangoBot.BOT_NAME + "'s Reactions on Commands, you get a detailed description on whats going on.")
    ),
    INFO(Command.INFO, EmbedLibrary.INFO.getEmbed()),
    BUGREPORT(Command.BUGREPORT, EmbedLibrary.BUGREPORT.getEmbed()),
    INVITE(Command.INVITE, EmbedLibrary.INVITE.getEmbed()),
    DISCORD(Command.DISCORD, EmbedLibrary.DISCORD.getEmbed()),
    DONATE(Command.DONATE, EmbedLibrary.DONATE.getEmbed()),

    COUNT_INTERACTION(Command.COUNT_INTERACTION, DangoBot.getBasicEmbed()
            .addField("Dango Setup", "" +
                    "With this command, you can either see after how many Messages a Dango is given or set a custom amount.\n" +
                    "The default value is 100.")
            .addField("Usage Examples:", "" +
                    "`dango count` - Sends a message containing the current counter Maximum.\n" +
                    "`dango count 75` - Sets the counter maximum to 75")
            .setDescription("This command requires you to be an Authed User.")
    ),
    EMOJI_INTERACTION(Command.EMOJI_INTERACTION, DangoBot.getBasicEmbed()
            .addField("Dango Setup", "" +
                    "With this command, you can either see the current Emoji or define your own.\n" +
                    "The default emoji is \uD83C\uDF61.")
            .addField("Usage Examples:", "" +
                    "`dango emoji` - Sends a message containing the current Emoji.\n" +
                    "`dango emoji :heart:` - Sets the emoji to ❤")
            .setDescription("This command requires you to be an Authed User.")
    ),
    LEVELUP_ACTION_INTERACTION(Command.LEVELUP_ACTION_INTERACTION, DangoBot.getBasicEmbed()
            .addField("Dango Setup", "" +
                    "With this command, you can define custom Level-Actions, like applying a role to those on Level 10, or removing a role from those on Level 3\n" +
                    "There will be more Actions in the Future, if you want to request an action, use `dango discord` to get into the Bot Owner's Discord.")
            .addField("Usage Examples:", "" +
                    "`dango action` - Lists all currently set Level actions.\n" +
                    "`dango action applyrole 6 @Regular` - Applies the role @Regular for the 6th level.\n" +
                    "`dango action delete 4 ` - Removes the REMOVEROLE Action from Level 4")
            .addField("Be Careful!", "" +
                    "There are some things to note:\n" +
                    "\n" +
                    "- The Bot requires the proper Permissions to add a Role to an User or remove a Role from an User.\n" +
                    "- Be careful with this feature. _You don't usually want a User to get an Administrative Role through this Bot._\n" +
                    "- The Role that you are automatically giving the Users have to be mentionable. Currently you can only define Roles with their Mentions.")
            .setDescription("This command requires you to be an Authed User.")
    ),

    ADD_AUTH(Command.ADD_AUTH, DangoBot.getBasicEmbed()
            .addField("Auth Setup", "" +
                    "Adds one or more Users to this Server's Auth list.\n" +
                    "Auth Users have access to Bot Setup Commands.\n")
            .addField("Note:", "_Administrators and Users with Permission \"Manage Server\" are Auth by Default._")
    ),
    REMOVE_AUTH(Command.REMOVE_AUTH, DangoBot.getBasicEmbed()
            .addField("Auth Setup", "" +
                    "Removes one or more Users to this Server's Auth list.\n")
            .addField("Note:", "_Administrators and Users with Permission \"Manage Server\" are Auth by Default._")
    ),
    AUTHS(Command.AUTHS, DangoBot.getBasicEmbed()
            .addField("Auth Setup", "" +
                    "Sends a list of Authed users from this Server.")
    );

    private Command cmd;
    private EmbedBuilder embed;

    HelpLibrary(Command cmd, EmbedBuilder embed) {
        this.cmd = cmd;
        this.embed = embed;
    }

    public static Optional<HelpLibrary> getHelp(Command forCommand) {
        return Arrays.stream(HelpLibrary.values())
                .filter(c -> forCommand == c.cmd)
                .findAny();
    }

    public EmbedBuilder getEmbed() {
        return embed;
    }
}
