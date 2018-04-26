package de.kaleidox.dangobot.bot;

import de.kaleidox.dangobot.DangoBot;
import org.javacord.api.Javacord;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public enum EmbedLibrary {
    HELP(DangoBot.getBasicEmbed()
            .addField("Sorry!", "" +
                    "No Proper Help here yet.\n" +
                    "A detailed Guide will be implemented later on.\n" +
                    "\n" +
                    "Meanwhile, check out " + DangoBot.BOT_URL + " for a List of Commands.")
    ),
    INFO(DangoBot.getBasicEmbed()
            .addField("About the bot:", "" +
                    "Running on Version " + DangoBot.VERSION_NUMBER + "\n" +
                    "Made with love by " + DangoBot.OWNER_TAG + " in April 2018\n" +
                    "Running on Javacord Version " + Javacord.VERSION + "\n" +
                    "\n" +
                    "Enjoy!")
    ),
    BUGREPORT(DangoBot.getBasicEmbed()
            .addField("Report Bugs over here:", "" +
                    "https://github.com/Kaleidox00/Steve/issues")
    ),
    INVITE(DangoBot.getBasicEmbed()
            .addField("The Invitation for the Bot:", DangoBot.INVITE_LINK)
    ),
    DISCORD(DangoBot.getBasicEmbed()
            .addField("Our Discord Server:", DangoBot.DISCORD_LINK)
    ),
    DONATE(DangoBot.getBasicEmbed()
            .addField("You can Donate for this Bot here, if you want:", "" +
                    "http://donate.kaleidox.de/")
    );

    private EmbedBuilder eb;

    EmbedLibrary(EmbedBuilder eb) {
        this.eb = eb;
    }

    public EmbedBuilder getEmbed() {
        return eb;
    }
}
