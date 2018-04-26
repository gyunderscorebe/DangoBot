package de.kaleidox.dangobot.bot;

import de.kaleidox.dangobot.DangoBot;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public enum HelpLibrary {
    HELP(Command.HELP, DangoBot.getBasicEmbed()
            .addField("", "")
    ),
    INFO(Command.INFO, DangoBot.getBasicEmbed()
            .addField("", "")
    ),
    BUGREPORT(Command.BUGREPORT, DangoBot.getBasicEmbed()
            .addField("", "")
    ),
    INVITE(Command.INVITE, DangoBot.getBasicEmbed()
            .addField("", "")
    ),
    DISCORD(Command.DISCORD, DangoBot.getBasicEmbed()
            .addField("", "")
    ),
    DONATE(Command.DONATE, DangoBot.getBasicEmbed()
            .addField("", "")
    ),
    COUNT_INTERACTION(Command.COUNT_INTERACTION, DangoBot.getBasicEmbed()
            .addField("", "")
    ),
    EMOJI_INTERACTION(Command.EMOJI_INTERACTION, DangoBot.getBasicEmbed()
            .addField("", "")
    ),
    LEVELUP_ACTION_INTERACTION(Command.LEVELUP_ACTION_INTERACTION, DangoBot.getBasicEmbed()
            .addField("", "")
    ),
    ADD_AUTH(Command.ADD_AUTH, DangoBot.getBasicEmbed()
            .addField("", "")
    ),
    REMOVE_AUTH(Command.REMOVE_AUTH, DangoBot.getBasicEmbed()
            .addField("", "")
    ),
    AUTHS(Command.AUTHS, DangoBot.getBasicEmbed()
            .addField("", "")
    );

    private Command cmd;
    private EmbedBuilder embed;

    HelpLibrary(Command cmd, EmbedBuilder embed) {
        this.cmd = cmd;
        this.embed = embed;
    }
}
