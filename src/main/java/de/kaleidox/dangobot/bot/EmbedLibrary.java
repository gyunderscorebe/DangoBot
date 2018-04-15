package de.kaleidox.dangobot.bot;

import de.kaleidox.dangobot.Nub;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public enum EmbedLibrary {
    HELP(Nub.getBasicEmbed()
            .addField("Report Bugs over here:", "https://github.com/Kaleidox00/Steve/issues")
    ),
    INFO(Nub.getBasicEmbed()
            .addField("Report Bugs over here:", "https://github.com/Kaleidox00/Steve/issues")
    ),
    BUGREPORT(Nub.getBasicEmbed()
            .addField("Report Bugs over here:", "https://github.com/Kaleidox00/Steve/issues")
    ),
    INVITE(Nub.getBasicEmbed()
            .addField("The Invitation for the Poker Bot:", Nub.INVITE_LINK)
    ),
    DISCORD(Nub.getBasicEmbed()
            .addField("Our Discord Server:", Nub.DISCORD_LINK)
    ),
    DONATE(Nub.getBasicEmbed()
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
