package de.kaleidox.dangobot.bot.specific;

import de.kaleidox.dangobot.util.Debugger;
import de.kaleidox.dangobot.util.Emoji;
import de.kaleidox.dangobot.util.Mapper;
import de.kaleidox.dangobot.util.serializer.PropertiesMapper;
import de.kaleidox.dangobot.util.serializer.SelectedPropertiesMapper;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class DangoProcessor {
    private Debugger log;
    private static final ConcurrentHashMap<Long, DangoProcessor> selfMap = new ConcurrentHashMap<>();
    private Server myServer;
    private long serverId;
    private int counter, counterMax;
    private SelectedPropertiesMapper settings;
    private PropertiesMapper rankings;
    public PropertiesMapper actions;
    private Emoji emoji;
    private AtomicReference<User> lastDango = new AtomicReference<>();

    private DangoProcessor(Server server) {
        this.myServer = server;

        serverId = myServer.getId();

        this.settings = new SelectedPropertiesMapper(new File("props/dangoProcessorSettings.properties"), ';', serverId);

        File ranks = new File("props/rankings/" + serverId + ".properties");

        if (!ranks.exists()) {
            try {
                ranks.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.rankings = new PropertiesMapper(ranks, ';');

        File action = new File("props/actions/" + serverId + ".properties");

        if (!action.exists()) {
            try {
                action.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.actions = new PropertiesMapper(action, ';');

        this.counterMax = Integer.parseInt(settings.softGet(0, 100));
        this.emoji = new Emoji(settings.softGet(1, "\uD83C\uDF61"));

        log = new Debugger(DangoProcessor.class.getName(), server.getName());

        Mapper.safePut(selfMap, serverId, this);
    }

    /*
    This Object's "Constructor".
    Looks for an already existing instance of the required Object or creates a new one.

    @returns The adequate Instance of this Object.
     */
    public static DangoProcessor softGet(Server server) {
        return (selfMap.containsKey(server.getId()) ? selfMap.get(server.getId()) : selfMap.put(server.getId(), new DangoProcessor(server)));
    }

    public void increment(Message msg) {
        MessageAuthor userAuthor = msg.getAuthor();
        ServerTextChannel stc = msg.getServerTextChannel().get();

        if (userAuthor.isUser() && !userAuthor.isYourself()) {
            User usr = userAuthor.asUser().get();

            counter++;

            if (counter >= counterMax) {
                giveDango(usr, stc);
            }
        }
    }

    public void giveDango(User user, ServerTextChannel inChannel) {
        rankings.set(user.getId(), 0, rankings.softGet(user.getId(), 0, 0) + 1);
        rankings.write();

        inChannel.sendMessage(emoji.getPrintable()).thenRun(() -> counter = 0);
    }

    public int getCounterMax() {
        return counterMax;
    }

    public void setCounterMax(int counterMax) {
        this.counterMax = counterMax;
        settings.set(0, counter);
        settings.write();
    }

    public Emoji getEmoji() {
        return emoji;
    }

    public void setEmoji(Emoji emoji) {
        this.emoji = emoji;

        settings.set(1, emoji.getPrintable());
        settings.write();
    }

    public void addAction(int level, String actionTitle, Role role) {
        actions.add(level, actionTitle);
        actions.add(level, role.getId());

        log.put(actions.getAll(level));

        actions.write();
    }
}
