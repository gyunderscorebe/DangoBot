package de.kaleidox.dangobot.bot;

import de.kaleidox.dangobot.Main;
import de.kaleidox.dangobot.Nub;
import de.kaleidox.dangobot.bot.specific.DangoProcessor;
import de.kaleidox.dangobot.util.Debugger;
import de.kaleidox.dangobot.util.SuccessState;
import de.kaleidox.dangobot.util.Utils;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public enum Command {
    // Enum Stuff
    HELP("help", false, new int[]{0, 0}, msg -> {
        msg.getServerTextChannel().ifPresent(event -> event.sendMessage(EmbedLibrary.HELP.getEmbed()));
    }),
    INFO("info", false, new int[]{0, 0}, msg -> {
        msg.getServerTextChannel().ifPresent(event -> event.sendMessage(EmbedLibrary.INFO.getEmbed()));
    }),
    BUGREPORT("bug", false, new int[]{0, 0}, msg -> {
        msg.getServerTextChannel().ifPresent(event -> event.sendMessage(EmbedLibrary.BUGREPORT.getEmbed()));
    }),
    INVITE("invitelink", false, new int[]{0, 0}, msg -> {
        msg.getUserAuthor().ifPresent(user -> {
            user.openPrivateChannel().thenAccept(privateChannel -> {
                privateChannel.sendMessage(EmbedLibrary.INVITE.getEmbed());
            });
        });
    }),
    DISCORD("discordlink", false, new int[]{0, 0}, msg -> {
        msg.getServerTextChannel().ifPresent(event -> event.sendMessage(EmbedLibrary.DISCORD.getEmbed()));
    }),
    DONATE("donate", false, new int[]{0, 0}, msg -> {
        msg.getServerTextChannel().ifPresent(event -> event.sendMessage(EmbedLibrary.DONATE.getEmbed()));
    }),

    ADD_AUTH("auth", true, new int[]{1, 1}, msg -> {
        Server srv = msg.getServer().get();
        User usr = msg.getUserAuthor().get();

        Auth.softGet(srv).addAuth(usr).thenAccept(state -> Utils.evaluateState(msg, state));
    }),
    REMOVE_AUTH("unauth", true, new int[]{1, 1}, msg -> {
        Server srv = msg.getServer().get();
        User usr = msg.getUserAuthor().get();

        Auth.softGet(srv).removeAuth(usr).thenAccept(state -> Utils.evaluateState(msg, state));
    }),
    AUTHS("auths", true, new int[]{0, 0}, msg -> {
        Server srv = msg.getServer().get();

        msg.getServerTextChannel().ifPresent(chl -> Auth.softGet(srv).sendEmbed(chl).thenAccept(state -> Utils.evaluateState(msg, state)));
    }),

    TESTCOMMAND("testcommand", false, new int[]{0, 0}, msg -> {
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
        add("nub");
        add("nubbot");
        add("<@392065608398798851>");
    }};
    public static final HashSet<Command> VALUES = new HashSet<Command>() {{
        this.addAll(Arrays.asList(Command.values()));
    }};
    private static Debugger log = new Debugger(Command.class.getName());
    private String keyword;
    private boolean requiresAuth;
    // Command Usage Stuff
    private int[] parameterRange;
    private Consumer<Message> consumer;

    Command(String keyword, boolean requiresAuth, int[] parameterRange, Consumer<Message> consumer) {
        this.keyword = keyword;
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
        } else if (msg.getChannel() instanceof PrivateChannel) {
            msg.getPrivateChannel().get().sendMessage(Nub.getBasicEmbed()
                    .addField("I'm Sorry!", "I'm sorry, but I currently can't work from DMs! Please try messaging me with `dangobot help` on a Server!")
            );
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
}
