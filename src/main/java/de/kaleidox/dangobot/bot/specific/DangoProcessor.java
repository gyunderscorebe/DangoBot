package de.kaleidox.dangobot.bot.specific;

import de.kaleidox.dangobot.DangoBot;
import de.kaleidox.dangobot.Main;
import de.kaleidox.dangobot.util.CustomCollectors;
import de.kaleidox.dangobot.util.Debugger;
import de.kaleidox.dangobot.util.Emoji;
import de.kaleidox.dangobot.util.ServerPreferences;
import de.kaleidox.dangobot.util.SuccessState;
import de.kaleidox.dangobot.util.Utils;
import de.kaleidox.dangobot.util.serializer.PropertiesMapper;
import de.kaleidox.dangobot.util.serializer.SelectedPropertiesMapper;
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
    private int counter, counterMax;
    private SelectedPropertiesMapper settings;
    private PropertiesMapper rankings;
    private Emoji emoji;
    private LastDango lastDango;
    private AtomicReference<Message> leaderboard = new AtomicReference<>(null);
    private ServerPreferences preferences;

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
        giveDango(user, inChannel, 1);

        if (preferences.getVariable(ServerPreferences.Variable.ADVANCED_LEADERBOARD).asBoolean()) {
            updateScoreboard();
        }
    }

    public void giveDango(User user, ServerTextChannel inChannel, int amount) {
        rankings.set(user.getId(), 0, Integer.parseInt(rankings.softGet(user.getId(), 0, 0)) + amount);
        rankings.write();

        inChannel.sendMessage(emoji.getPrintable()).thenAccept(msg -> {
            counter = 0;

            int newLevel = Integer.parseInt(rankings.get(user.getId(), 0));

            Utils.everyOfList(2, actions.getAll(newLevel))
                    .forEach(action -> {
                        switch (action.get(0)) {
                            case "applyrole":
                                Main.API.getRoleById(action.get(1))
                                        .ifPresent(user::addRole);

                                SuccessState.SUCCESSFUL
                                        .evaluateForMessage(msg);
                                break;
                            case "removerole":
                                Main.API.getRoleById(action.get(1))
                                        .ifPresent(user::removeRole);

                                SuccessState.SUCCESSFUL
                                        .evaluateForMessage(msg);
                                break;
                            default:
                                SuccessState.UNSUCCESSFUL
                                        .withMessage("Unknown LevelUp-Action: " + action.get(0))
                                        .evaluateForMessage(msg);
                                break;
                        }
                    });


            lastDango = new LastDango(user, inChannel, msg);
        });
    }

    public void removeDango(User user, ServerTextChannel inChannel, int amount) {
        int currentDangos = Integer.parseInt(rankings.softGet(user.getId(), 0, 0));

        if (currentDangos != 0) {
            rankings.set(user.getId(), 0, currentDangos - amount);
            rankings.write();
        }

        inChannel.sendMessage("Dango Removed from User: " + user
                .getNickname(inChannel.getServer()).orElseGet(user::getName))
                .thenAccept(msg -> {
                    // nothing to see here
                }).exceptionally(ExceptionLogger.get());
    }

    public void revokeDango() {
        if (!lastDango.isEmpty()) {
            User usr = lastDango.user;
            ServerTextChannel stc = lastDango.serverTextChannel;
            Message msg = lastDango.message;

            removeDango(usr, stc, 1);
        }
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

    public void updateScoreboard() {
        if (leaderboard.get() != null) {
            ServerTextChannel stc = leaderboard.get().getServerTextChannel().get();

            sendScoreboard(stc, true);
        }
    }

    public SuccessState sendScoreboard(ServerTextChannel stc, boolean editOld) {
        TreeMap<Integer, ArrayList<User>> resultList = new TreeMap<>();
        SuccessState val = SuccessState.NOT_RUN;
        Server srv = stc.getServer();

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
                    resultList.put(thisLevel, newList);
                }
            } catch (InterruptedException | ExecutionException e) {
                log.put(e.getMessage());

                val = SuccessState.ERRORED.withMessage("There was an error finding the User [" + key + "].\n" +
                        "Please Contact the bot author " + DangoBot.OWNER_TAG + ".");
            } catch (TimeoutException e) {
                log.put("Could not find User by ID: " + key);

                val = SuccessState.ERRORED.withMessage("Could not find User by ID [" + key + "] within the timeout.");
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

                        if (preferences.getVariable(ServerPreferences.Variable.ADVANCED_LEADERBOARD).asBoolean()) {
                            // TODO Use Pinning and Unpinning
                            if (maxRuntime.decrementAndGet() == 0) {
                                if (leaderboard.get() != null) {
                                    if (editOld) {
                                        leaderboard.get()
                                                .edit(message.toString());
                                    } else {
                                        leaderboard.get()
                                                .delete("Outdated");

                                        stc.sendMessage(message.toString())
                                                .thenAcceptAsync(leaderboard::set);
                                    }
                                } else {
                                    stc.sendMessage(message.toString())
                                            .thenAcceptAsync(leaderboard::set);
                                }
                            }
                        } else {
                            stc.sendMessage(message.toString())
                                    .thenAcceptAsync(leaderboard::set);
                        }
                    });
        } else {
            message.append("**Oops!**")
                    .append("\n")
                    .append("\n")
                    .append("There are no Scores for this Server, get the chatter going!");

            stc.sendMessage(message.toString());
        }

        return val;
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

    private class LastDango {
        private User user;
        private ServerTextChannel serverTextChannel;
        private Message message;

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
