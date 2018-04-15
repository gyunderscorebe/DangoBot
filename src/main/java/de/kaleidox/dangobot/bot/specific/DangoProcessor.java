package de.kaleidox.dangobot.bot.specific;

import com.vdurmont.emoji.EmojiParser;
import de.kaleidox.dangobot.Main;
import de.kaleidox.dangobot.util.CustomCollectors;
import de.kaleidox.dangobot.util.Emoji;
import de.kaleidox.dangobot.util.Mapper;
import de.kaleidox.dangobot.util.serializer.PropertiesMapper;
import de.kaleidox.dangobot.util.serializer.SelectedPropertiesMapper;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.core.entity.emoji.UnicodeEmojiImpl;

import java.io.File;
import java.io.IOException;

public class DangoProcessor {
    private Server myServer;
    private long serverId;
    private int counter, counterMax;
    private SelectedPropertiesMapper settings;
    private PropertiesMapper rankings;
    private Emoji emoji = new de.kaleidox.dangobot.util.Emoji("\uD83C\uDF61");

    private DangoProcessor(Server server) {
        this.myServer = server;
        this.counterMax = 100;

        serverId = myServer.getId();

        this.settings = new SelectedPropertiesMapper(new File("props/dangoProcessorSettings.properties"), ';', serverId);

        File ranks = new File("props/rankings/"+serverId+".properties");

        if (!ranks.exists()) {
            try {
                ranks.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.rankings = new PropertiesMapper(ranks, ';');

        counterMax = Integer.parseInt(settings.softGet(0, 0));

        Mapper.safePut(Main.dangoProcessorMap, serverId, this);
    }

    /*
    This Object's "Constructor".
    Looks for an already existing instance of the required Object or creates a new one.

    @returns The adequate Instance of this Object.
     */
    public static DangoProcessor softGet(Server server) {
        return (Main.dangoProcessorMap.containsKey(server.getId()) ? Main.dangoProcessorMap.get(server.getId()) : Main.dangoProcessorMap.put(server.getId(), new DangoProcessor(server)));
    }

    public void setCounterMax(int counterMax) {
        this.counterMax = counterMax;
        settings.set(0, counter);
    }

    public void setEmoji(Emoji emoji) {
        this.emoji = emoji;
    }

    public void increment(User user, ServerTextChannel inChannel) {
        counter++;

        if (counter >= counterMax) {
            giveDango(user);

            inChannel.sendMessage(emoji.getPrintable());
        }
    }

    public void giveDango(User user) {

    }
}
