package de.kaleidox.dangobot.bot;

import de.kaleidox.dangobot.DangoBot;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public enum EmbedLibrary {
    HELP(DangoBot.getBasicEmbed()
            .addField("Report Bugs over here:", "https://github.com/Kaleidox00/Steve/issues")
    ),
    INFO(DangoBot.getBasicEmbed()
            .addField("Report Bugs over here:", "https://github.com/Kaleidox00/Steve/issues")
    ),
    BUGREPORT(DangoBot.getBasicEmbed()
            .addField("Report Bugs over here:", "https://github.com/Kaleidox00/Steve/issues")
    ),
    INVITE(DangoBot.getBasicEmbed()
            .addField("The Invitation for the Poker Bot:", DangoBot.INVITE_LINK)
    ),
    DISCORD(DangoBot.getBasicEmbed()
            .addField("Our Discord Server:", DangoBot.DISCORD_LINK)
    ),
    DONATE(DangoBot.getBasicEmbed()
            .addField("You can Donate for this Bot here, if you want:", "http://donate.kaleidox.de/")
    );

    private EmbedBuilder eb;

    EmbedLibrary(EmbedBuilder eb) {
        this.eb = eb;
    }

    public EmbedBuilder getEmbed() {
        return eb;
    }
}
