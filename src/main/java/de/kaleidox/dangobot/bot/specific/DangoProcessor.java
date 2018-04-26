package de.kaleidox.dangobot.bot.specific;

import de.kaleidox.dangobot.DangoBot;
import de.kaleidox.dangobot.Main;
import de.kaleidox.dangobot.util.CustomCollectors;
import de.kaleidox.dangobot.util.Debugger;
import de.kaleidox.dangobot.util.Emoji;
import de.kaleidox.dangobot.util.Mapper;
import de.kaleidox.dangobot.util.Utils;
import de.kaleidox.dangobot.util.serializer.PropertiesMapper;
import de.kaleidox.dangobot.util.serializer.SelectedPropertiesMapper;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class DangoProcessor {
    private static final ConcurrentHashMap<Long, DangoProcessor> selfMap = new ConcurrentHashMap<>();
    public PropertiesMapper actions;
    private Debugger log;
    private Server myServer;
    private long serverId;
    private int counter, counterMax;
    private SelectedPropertiesMapper settings;
    private PropertiesMapper rankings;
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
        this.counter = Integer.parseInt(settings.softGet(2, 0));

        log = new Debugger(DangoProcessor.class.getName(), server.getName());

        Mapper.safePut(selfMap, serverId, this);
    }

    /*
    This Object's "Constructor".
    Looks for an already existing instance of the required Object or creates a new one.

    @returns The adequate Instance of this Object.
     */
    public final static DangoProcessor softGet(Server server) {
        return (selfMap.containsKey(server.getId()) ? selfMap.get(server.getId()) : selfMap.put(server.getId(), new DangoProcessor(server)));
    }

    public void increment(Message msg) {
        MessageAuthor userAuthor = msg.getAuthor();
        ServerTextChannel stc = msg.getServerTextChannel().get();

        if (userAuthor.isUser() && !userAuthor.isYourself()) {
            User usr = userAuthor.asUser().get();

            counter++;

            settings.set(2, counter);

            if (counter >= counterMax) {
                giveDango(usr, stc);
            }
        }
    }

    public void giveDango(User user, ServerTextChannel inChannel) {
        rankings.set(user.getId(), 0, Integer.parseInt(rankings.softGet(user.getId(), 0, 0)) + 1);
        rankings.write();

        inChannel.sendMessage(emoji.getPrintable()).thenRun(() -> counter = 0);
    }

    public int getCounterMax() {
        return counterMax;
    }

    public void setCounterMax(int counterMax) {
        this.counterMax = counterMax;
        settings.set(0, counterMax);
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

    public void addRoleAction(int level, String actionTitle, Role role) {
        actions.add(level, actionTitle);
        actions.add(level, role.getId());

        actions.write();
    }

    public void removeAction(int level, String actionTitle) {
        actions.set(level, Utils.everyOfList(2, actions.getAll(level))
                .stream()
                .filter(l -> !l.get(0).equals(actionTitle))
                .collect(CustomCollectors.listMerge())
        );

        actions.write();
    }

    public void removeActions(int level) {
        actions.removeKey(level);

        actions.write();
    }

    public void sendScoreboard(ServerTextChannel stc) {
        HashMap<Integer, ArrayList<User>> resultList = new HashMap<>();

        rankings.getValues()
                .forEach((key, values) -> {
                    int level = Integer.parseInt(values.get(0));
                    User user = Main.API.getUserById(key).join();

                    if (resultList.containsKey(level)) {
                        resultList.get(level).add(user);
                    } else {
                        ArrayList<User> list = new ArrayList<>();
                        list.add(user);
                        resultList.put(level, list);
                    }
                });
        // todo THIS FFS
    }
}
