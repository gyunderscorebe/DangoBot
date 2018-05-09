package de.kaleidox.dangobot.bot.specific;

import de.kaleidox.dangobot.DangoBot;
import de.kaleidox.dangobot.Main;
import de.kaleidox.util.CustomCollectors;
import de.kaleidox.util.Debugger;
import de.kaleidox.util.Emoji;
import de.kaleidox.util.ServerPreferences;
import de.kaleidox.util.SuccessState;
import de.kaleidox.util.Utils;
import de.kaleidox.util.discord.ui.UniqueMessage;
import de.kaleidox.util.serializer.PropertiesMapper;
import de.kaleidox.util.serializer.SelectedPropertiesMapper;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.logging.ExceptionLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DangoProcessor {
    private static final ConcurrentHashMap<Long, DangoProcessor> selfMap = new ConcurrentHashMap<>();
    public PropertiesMapper actions;
    private Debugger log;
    private Server myServer;
    private long serverId;
    private AtomicInteger counter = new AtomicInteger();
    private AtomicInteger counterMax = new AtomicInteger();
    private SelectedPropertiesMapper settings;
    private PropertiesMapper rankings;
    private Emoji emoji;
    private LastDango lastDango;
    private ServerPreferences preferences;
    private UniqueMessage leaderboard = null;

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

        this.counterMax.set(Integer.parseInt(settings.softGet(0, 100)));
        this.emoji = new Emoji(settings.softGet(1, "\uD83C\uDF61"));
        this.counter.set(Integer.parseInt(settings.softGet(2, 0)));

        log = new Debugger(DangoProcessor.class.getName(), server.getName());
        preferences = ServerPreferences.softGet(myServer);

        Utils.safePut(selfMap, serverId, this);
    }

    /*
    This Object's "Constructor".
    Looks for an already existing instance of the required Object or creates a new one.

    @returns The adequate Instance of this Object.
     */
    public final static DangoProcessor softGet(Server server) {
        return (selfMap.containsKey(server.getId()) ? selfMap.get(server.getId()) : selfMap.put(server.getId(), new DangoProcessor(server)));
    }

    public static void updateScoreboards() {
        selfMap
                .forEach((key, value) -> {
                    value.updateScoreboard();
                });
    }

    public void increment(Message msg) {
        MessageAuthor userAuthor = msg.getAuthor();
        ServerTextChannel stc = msg.getServerTextChannel().get();
        UserRecordProcessor userRecordProcessor = UserRecordProcessor.softGet(msg.getServer().get());

        if (!userAuthor.asUser().get().isBot() && !userAuthor.isYourself()) {
            User usr = userAuthor.asUser().get();

            counter.incrementAndGet();
            settings.set(2, counter);
            settings.write();

            if (counter.get() >= counterMax.get()) {
                if (userRecordProcessor.decideDango(usr, stc, lastDango)) {
                    giveDango(usr, stc);
                }
            }
        }
    }

    public void giveDango(User user, ServerTextChannel inChannel) {
        giveDango(user, inChannel, 1);

        if (preferences.get(ServerPreferences.Variable.ADVANCED_LEADERBOARD).asBoolean()) {
            updateScoreboard();
        }
    }

    public void giveDango(User user, ServerTextChannel inChannel, int amount) {
        rankings.set(user.getId(), 0, Integer.parseInt(rankings.softGet(user.getId(), 0, 0)) + amount);
        rankings.write();
        counter.set(0);
        settings.set(2, counter);
        settings.write();
        int newLevel = Integer.parseInt(rankings.get(user.getId(), 0));

        Utils.everyOfList(2, actions.getAll(newLevel))
                .forEach(action -> {
                    switch (action.get(0)) {
                        case "applyrole":
                            Main.API.getRoleById(action.get(1))
                                    .ifPresent(user::addRole);
                            break;
                        case "removerole":
                            Main.API.getRoleById(action.get(1))
                                    .ifPresent(user::removeRole);
                            break;
                        default:
                            break;
                    }
                });

        lastDango = new LastDango(user, inChannel, inChannel.sendMessage(emoji.getPrintable()).join());
    }

    public void removeDango(User user, ServerTextChannel inChannel, int amount) {
        int currentDangos = Integer.parseInt(rankings.softGet(user.getId(), 0, 0));
        int newAmount = currentDangos - amount;

        if (currentDangos > 0) {
            log.put(newAmount);
            rankings.set(user.getId(), 0, (newAmount < 0 ? 0 : newAmount));
            rankings.write();
        }

        inChannel.sendMessage("Dango Removed from User: " + user
                .getNickname(inChannel.getServer()).orElseGet(user::getName) + "\n" +
                "They now have " + (newAmount < 0 ? 0 : newAmount) + " ")
                .thenAccept(msg -> {
                    // nothing to see here
                }).exceptionally(ExceptionLogger.get());
    }

    public void revokeDango() {
        if (lastDango != null) {
            if (!lastDango.isEmpty()) {
                User usr = lastDango.user;
                ServerTextChannel stc = lastDango.serverTextChannel;
                Message msg = lastDango.message;

                removeDango(usr, stc, 1);
            }
        }
    }

    public int getCounterMax() {
        return counterMax.get();
    }

    public void setCounterMax(int counterMax) {
        this.counterMax.set(counterMax);
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

    public void updateScoreboard() {
        if (leaderboard != null) {
            leaderboard.refresh();
        }
    }

    public SuccessState sendScoreboard(ServerTextChannel stc, boolean editOld) {
        AtomicReference<SuccessState> val = new AtomicReference<>(SuccessState.NOT_RUN);

        leaderboard = UniqueMessage.get(stc, () -> {
            StringBuilder give = new StringBuilder();

            TreeMap<Integer, ArrayList<User>> resultList = new TreeMap<>();
            Server srv = stc.getServer();
            DangoProcessor dangoProcessor = DangoProcessor.softGet(srv);

            for (Map.Entry<String, List<String>> entry : rankings.getValues().entrySet()) {
                String key = entry.getKey();
                List<String> value = entry.getValue();
                int thisLevel = Integer.parseInt(value.get(0));

                try {
                    log.put("Find user: [" + key + "]", true);

                    User user = Main.API
                            .getUserById(key)
                            .get(10, TimeUnit.SECONDS);

                    if (resultList.containsKey(thisLevel)) {
                        resultList.get(thisLevel).add(user);
                    } else {
                        ArrayList<User> newList = new ArrayList<>();

                        newList.add(user);
                        if (thisLevel > 0) {
                            resultList.put(thisLevel, newList);
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.put(e.getMessage());

                    val.set(SuccessState.ERRORED.withMessage("There was an error finding the User [" + key + "].\n" +
                            "Please Contact the bot author " + DangoBot.OWNER_TAG + "."));
                } catch (TimeoutException e) {
                    log.put("Could not find User by ID: " + key);

                    val.set(SuccessState.ERRORED.withMessage("Could not find User by ID [" + key + "] within the timeout."));
                }
            }

            AtomicInteger maxRuntime = new AtomicInteger(resultList.size());
            AtomicInteger lastKey = new AtomicInteger(-1);
            AtomicInteger place = new AtomicInteger(0);
            StringBuilder message = new StringBuilder();
            int totalDangos = 0, thisAmount = 0;

            for (Map.Entry<String, String> entry : rankings.getMap().entrySet()) {
                thisAmount = Integer.parseInt(entry.getValue());

                totalDangos = totalDangos + thisAmount;
            }

            message
                    .append(emoji.getPrintable())
                    .append(emoji.getPrintable())
                    .append("\t")
                    .append("__")
                    .append("**Scores for ")
                    .append(srv.getName())
                    .append(":**")
                    .append("__")
                    .append("\t")
                    .append(emoji.getPrintable())
                    .append(emoji.getPrintable())
                    .append("\n")
                    .append("\n")
                    .append("**Total ")
                    .append(emoji.getPrintable())
                    .append(": ")
                    .append(totalDangos)
                    .append("**")
                    .append("\n")
                    .append("Giving a new ")
                    .append(emoji.getPrintable())
                    .append(" every ")
                    .append(dangoProcessor.counterMax)
                    .append(" Messages.")
                    .append("\n")
                    .append("\n");

            if (resultList.size() != 0) {
                resultList.descendingMap()
                        .forEach((level, users) -> {
                            if (lastKey.get() == -1 || lastKey.get() > level) {
                                lastKey.set(level);
                                place.incrementAndGet();
                            }

                            if (lastKey.get() == level) {
                                message.append("**`")
                                        .append(place)
                                        .append(".` ")
                                        .append(level)
                                        .append("x ")
                                        .append(emoji.getPrintable())
                                        .append("**: ");
                            }

                            users.forEach(user -> {
                                message.append(user.getNickname(srv)
                                        .orElseGet(user::getName)
                                )
                                        .append(", ");
                            });

                            message.reverse()
                                    .delete(1, 2)
                                    .reverse()
                                    .append("\n");

                            if (preferences.get(ServerPreferences.Variable.ADVANCED_LEADERBOARD).asBoolean()) {
                                // TODO Use Pinning and Unpinning
                                if (maxRuntime.decrementAndGet() == 0) {
                                    give.append(message.toString());
                                }
                            } else {
                                give.append(message.toString());
                            }
                        });
            } else {
                message.append("**Oops!**")
                        .append("\n")
                        .append("\n")
                        .append("There are no Scores for this Server, get the chatter going!");

                give.append(message.toString());
            }

            return give.toString();
        });

        return val.get();
    }

    public void sendUserScore(ServerTextChannel stc, User usr) {
        stc.sendMessage(DangoBot.getBasicEmbed()
                .addField(usr.getNickname(stc.getServer()).orElseGet(usr::getName) + "'s Score:", rankings.softGet(usr.getId(), 0, 0))
        );
    }

    public void clearAll() {
        rankings.clearAll();

        rankings.write();
    }

    class LastDango {
        public User user;
        public ServerTextChannel serverTextChannel;
        public Message message;

        LastDango(User user, ServerTextChannel serverTextChannel, Message message) {
            this.user = user;
            this.serverTextChannel = serverTextChannel;
            this.message = message;
        }

        boolean isEmpty() {
            if (user == null || serverTextChannel == null || message == null) {
                return true;
            } else {
                return false;
            }
        }
    }
}
