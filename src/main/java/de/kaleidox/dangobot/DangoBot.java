package de.kaleidox.dangobot;

import de.kaleidox.dangobot.util.Utils;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public final class DangoBot {
    public static final String VERSION_NUMBER = "1.5.2";
    public static final boolean isTesting = System.getProperty("os.name").equals("Windows 10");
    public static final Long BOT_ID = 439082176537952267L;
    public static final Long DBL_BOT_ID = BOT_ID;
    public static final String DBL_BOT_TOKEN = readFile("keys/tokenDiscordBotsOrg.txt");
    public static final String BOT_TOKEN = readFile(isTesting ? "keys/tokenTest.txt" : "keys/tokenMain.txt");
    public static final String BOT_NAME = "DangoBot";
    public static final String DISCORD_LINK = "https://discord.gg/9hrde8M";
    public static final String BOT_URL = "http://dangobot.kaleidox.de/";
    public static final String ICON_URL = "http://wppullzone1.epicmatcha.netdna-cdn.com/wp-content/uploads/2016/03/matcha-cookies-hanami-dango.jpg";
    public static final String OWNER_TAG = "@Kaleidox#0001";
    private static final Long PERMISSION_STRING = 470248512L;
    public static final String INVITE_LINK = "https://discordapp.com/oauth2/authorize?client_id=" + BOT_ID + "&scope=bot&permissions=" + PERMISSION_STRING;

    public static EmbedBuilder getBasicEmbed() {
        return new EmbedBuilder()
                .setFooter(BOT_NAME + " by " + OWNER_TAG)
                .setAuthor(BOT_NAME, BOT_URL, ICON_URL)
                .setTimestamp()
                .setUrl(BOT_URL)
                .setColor(Utils.getRandomColor());
    }

    private static String readFile(String name) {
        String give = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader(name));

            give = br.readLine();
        } catch (IOException e) {
        }

        return give;
    }
}
