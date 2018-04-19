package de.kaleidox.dangobot.bot;

import de.kaleidox.dangobot.DangoBot;
import de.kaleidox.dangobot.Main;
import de.kaleidox.dangobot.bot.specific.DangoProcessor;
import de.kaleidox.dangobot.util.Debugger;
import de.kaleidox.dangobot.util.Emoji;
import de.kaleidox.dangobot.util.SuccessState;
import de.kaleidox.dangobot.util.Utils;
import de.kaleidox.dangobot.util.serializer.PropertiesMapper;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public enum Command {
    // Enum Stuff
    HELP("help", true, false, new int[]{0, 0}, msg -> {
        msg.getServerTextChannel().ifPresent(event -> event.sendMessage(EmbedLibrary.HELP.getEmbed()));
    }),
    INFO("info", true, false, new int[]{0, 0}, msg -> {
        msg.getServerTextChannel().ifPresent(event -> event.sendMessage(EmbedLibrary.INFO.getEmbed()));
    }),
    BUGREPORT("bug", true, false, new int[]{0, 0}, msg -> {
        msg.getServerTextChannel().ifPresent(event -> event.sendMessage(EmbedLibrary.BUGREPORT.getEmbed()));
    }),
    INVITE("invitelink", true, false, new int[]{0, 0}, msg -> {
        msg.getUserAuthor().ifPresent(user -> {
            user.openPrivateChannel().thenAccept(privateChannel -> {
                privateChannel.sendMessage(EmbedLibrary.INVITE.getEmbed());
            });
        });
    }),
    DISCORD("discordlink", true, false, new int[]{0, 0}, msg -> {
        msg.getServerTextChannel().ifPresent(event -> event.sendMessage(EmbedLibrary.DISCORD.getEmbed()));
    }),
    DONATE("donate", true, false, new int[]{0, 0}, msg -> {
        msg.getServerTextChannel().ifPresent(event -> event.sendMessage(EmbedLibrary.DONATE.getEmbed()));
    }),

    COUNT_INTERACTION("count", false, true, new int[]{0, 1}, msg -> {
        Server srv = msg.getServer().get();
        User usr = msg.getUserAuthor().get();
        ServerTextChannel stc = msg.getServerTextChannel().get();
        DangoProcessor dangoProcessor = DangoProcessor.softGet(srv);

        List<String> param = extractParam(msg);

        if (param.size() == 0) {
            stc.sendMessage(DangoBot.getBasicEmbed()
                    .addField("Current Counter:", String.valueOf(dangoProcessor.getCounterMax()))
            );
        } else if (param.size() == 1) {
            if (param.get(0).matches("[0-9]*")) {
                long val = Long.parseLong(param.get(0));
                if (val <= Integer.MAX_VALUE && val >= 25) {
                    dangoProcessor.setCounterMax(Integer.parseInt(param.get(0)));
                } else if (val < 25) {
                    SuccessState.ERRORED.withMessage("The given Number is too small, needs to be greater than 25.").evaluateForMessage(msg);
                } else {
                    SuccessState.ERRORED.withMessage("The given Number is too big.").evaluateForMessage(msg);
                }
            }
        } else {
            SuccessState.ERRORED.withMessage("Too many or too few arguments.").evaluateForMessage(msg);
        }
    }),
    EMOJI_INTERACTION("emoji", false, true, new int[]{0, 1}, msg -> {
        Server srv = msg.getServer().get();
        ServerTextChannel stc = msg.getServerTextChannel().get();
        DangoProcessor dangoProcessor = DangoProcessor.softGet(srv);

        List<String> param = extractParam(msg);

        if (param.size() == 0) {
            stc.sendMessage(DangoBot.getBasicEmbed()
                    .addField("Current Emoji:", dangoProcessor.getEmoji().getPrintable())
            );
        } else if (param.size() == 1) {
            List<CustomEmoji> customEmojis = msg.getCustomEmojis();

            if (customEmojis.size() == 1) {
                dangoProcessor.setEmoji(new Emoji(customEmojis.get(0)));
            } else if (customEmojis.size() == 0) {
                dangoProcessor.setEmoji(new Emoji(param.get(0)));
            }
        } else {
            SuccessState.ERRORED.withMessage("Too many or too few arguments.").evaluateForMessage(msg);
        }
    }),

    LEVELUP_ACTION_INTERACTION("action", false, true, new int[]{0, 2}, msg -> {
        Server srv = msg.getServer().get();
        ServerTextChannel stc = msg.getServerTextChannel().get();
        DangoProcessor dangoProcessor = DangoProcessor.softGet(srv);
        PropertiesMapper actions = dangoProcessor.actions;

        List<String> param = extractParam(msg);

        Debugger.print(param.size());

        if (param.size() == 0) {

            EmbedBuilder embed = DangoBot.getBasicEmbed()
                    .setDescription("Current Levelup-Actions:");

            for (Map.Entry<String, ArrayList<String>> entry : actions.entrySet()) {
                Optional<Role> roleById = Main.API.getRoleById(entry.getValue().get(1));

                embed.addField(
                        "Level "+entry.getKey()+":",
                        entry.getValue().get(0).equals("applyrole") ? (roleById.map(role -> "Add Role: " + role.getMentionTag()).orElse("Unknown Role")) : "Add Dango"
                );
            }
        } else if (param.size() >= 3 && param.size() <= 4) {
            List<Role> mentionedRoles = msg.getMentionedRoles();

            switch (param.get(0)) {
                case "applyrole":
                    if (actions.mapSize() < 25) {
                    dangoProcessor.addAction(Integer.parseInt(param.get(0)), "applyrole", mentionedRoles.get(0));
                    } else {
                        SuccessState.ERRORED.withMessage("There are already too many Actions! ").evaluateForMessage(msg);
                    }

                    break;
                case "removerole":
                    if (actions.mapSize() < 25) {
                        dangoProcessor.addAction(Integer.parseInt(param.get(0)),"removerole", mentionedRoles.get(0));
                    } else {
                        SuccessState.ERRORED.withMessage("There are already too many Actions! ").evaluateForMessage(msg);
                    }

                    break;
                case "delete":
                    dangoProcessor.actions.removeKey(param.get(0));
                    break;
            }
        } else {
            SuccessState.ERRORED.withMessage("Too many or too few arguments.\n" +
                    "The correct use is:\n" +
                    "dango action <Level> <Action> <Parameter>\n" +
                    "\n" +
                    "Example:\n" +
                    "dango action 6 applyrole @Regular").evaluateForMessage(msg);
        }
    }),

    ADD_AUTH("auth", false, true, new int[]{1, 1}, msg -> {
        Server srv = msg.getServer().get();
        User usr = msg.getUserAuthor().get();

        Auth.softGet(srv).addAuth(usr).thenAccept(state -> Utils.evaluateState(msg, state));
    }),
    REMOVE_AUTH("unauth", false, true, new int[]{1, 1}, msg -> {
        Server srv = msg.getServer().get();
        User usr = msg.getUserAuthor().get();

        Auth.softGet(srv).removeAuth(usr).thenAccept(state -> Utils.evaluateState(msg, state));
    }),
    AUTHS("auths", false, true, new int[]{0, 0}, msg -> {
        Server srv = msg.getServer().get();

        msg.getServerTextChannel().ifPresent(chl -> Auth.softGet(srv).sendEmbed(chl).thenAccept(state -> Utils.evaluateState(msg, state)));
    }),

    TESTCOMMAND("testcommand", false, true, new int[]{0, 0}, msg -> {
        Server srv = msg.getServer().get();
        Channel chl = msg.getChannel();
        User usr = msg.getUserAuthor().get();

        if (msg.getAuthor().isBotOwner()) {
            chl.asServerTextChannel().ifPresent(stc -> {
                if (usr.isBotOwner()) {
                    DangoProcessor.softGet(srv);
                }
            });
        }
    });

    public static final HashSet<String> KEYWORDS = new HashSet<String>() {{
        add("dango");
        add("dangobot");
        add(Main.API.getYourself().getMentionTag());
    }};
    public static final HashSet<Command> VALUES = new HashSet<Command>() {{
        this.addAll(Arrays.asList(Command.values()));
    }};
    private static Debugger log = new Debugger(Command.class.getName());
    public boolean canRunPrivately;
    private String keyword;
    private boolean requiresAuth;
    // Command Usage Stuff
    private int[] parameterRange;
    private Consumer<Message> consumer;

    Command(String keyword, boolean canRunPrivately, boolean requiresAuth, int[] parameterRange, Consumer<Message> consumer) {
        this.keyword = keyword;
        this.canRunPrivately = canRunPrivately;
        this.requiresAuth = requiresAuth;
        this.parameterRange = parameterRange;
        this.consumer = consumer;
    }

    public static SuccessState processCommand(Message msg) {
        SuccessState val = SuccessState.NOT_RUN;
        Server srv = msg.getServer().get();
        Channel chl = msg.getServerTextChannel().get();
        User usr = msg.getUserAuthor().get();

        List<String> parts = Collections.unmodifiableList(Arrays.asList(msg.getContent().split(" ")));

        if (!msg.isPrivate()) {
            if (KEYWORDS.contains(parts.get(0).toLowerCase())) {
                List<String> param = extractParam(msg);

                if (getCommand(msg).isPresent()) {
                    Optional<Command> myCommand = findFromKeyword(Utils.fromNullable(parts, 1));

                    Auth auth = Auth.softGet(srv);

                    if (myCommand.isPresent()) {
                        Command cmd = myCommand.get();

                        int satisfier = 0;

                        if (param.size() >= cmd.parameterRange[0] && param.size() <= cmd.parameterRange[1]) {
                            satisfier++;
                            log.put("Parameter Range OK", true);
                        } else val = SuccessState.UNSUCCESSFUL;

                        if (!cmd.requiresAuth || auth.isAuth(usr)) {
                            satisfier++;
                            log.put("Auth OK", true);
                        } else val = SuccessState.UNAUTHORIZED;

                        log.put("Satisfier: " + satisfier, true);
                        if (satisfier == 2) {
                            cmd.consumer.accept(msg);
                            val = SuccessState.SUCCESSFUL;
                        }
                    }
                }
            }
        }

        return val;
    }

    public static Optional<Command> getCommand(Message msg) {
        return findFromKeyword(Utils.fromNullable(Arrays.asList(msg.getContent().split(" ")), 1));
    }

    private static Optional<Command> findFromKeyword(String keyword) {
        if (keyword != null) {
            for (Command x : VALUES) {
                if (x.keyword.equals(keyword))
                    return Optional.of(x);
            }
        }

        return Optional.empty();
    }

    private static List<String> extractParam(Message msg) {
        List<String> param, parts;

        parts = Collections.unmodifiableList(Arrays.asList(msg.getContent().split(" ")));

        if (KEYWORDS.contains(parts.get(0).toLowerCase())) {
            param = Collections.unmodifiableList(parts.subList(2, parts.size()));
        } else {
            param = new ArrayList<>();
        }

        return param;
    }

    public void runPrivate(Message msg) {
        this.consumer.accept(msg);
    }
}
