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
    HELP("help", true, false, new int[]{0, 1}, msg -> {
        Server srv = msg.getServer().get();
        User usr = msg.getUserAuthor().get();
        ServerTextChannel stc = msg.getServerTextChannel().get();

        List<String> param = extractParam(msg);

        if (param.size() == 0) {
            stc.sendMessage(HelpLibrary.HELP.getEmbed());
        } else {
            Optional<Command> fromKeyword = Command.findFromKeyword(param.get(0));

            if (fromKeyword.isPresent()) {
                stc.sendMessage(HelpLibrary.getHelp(fromKeyword.get()).map(HelpLibrary::getEmbed).orElse(DangoBot.getBasicEmbed()
                        .addField("No Help found!", "No help found.")
                ));
            } else {
                stc.sendMessage(DangoBot.getBasicEmbed()
                        .addField("No Help found!", "No help found.")
                );
            }
            SuccessState.SUCCESSFUL.evaluateForMessage(msg);
        }
    }),
    INFO("info", true, false, new int[]{0, 0}, msg -> {
        msg.getServerTextChannel().ifPresent(event -> {
            event.sendMessage(EmbedLibrary.INFO.getEmbed());
            SuccessState.SUCCESSFUL.evaluateForMessage(msg);
        });
    }),
    BUGREPORT("bug", true, false, new int[]{0, 0}, msg -> {
        msg.getServerTextChannel().ifPresent(event -> {
            event.sendMessage(EmbedLibrary.BUGREPORT.getEmbed());
            SuccessState.SUCCESSFUL.evaluateForMessage(msg);
        });
    }),
    INVITE("invitelink", true, false, new int[]{0, 0}, msg -> {
        msg.getUserAuthor().ifPresent(user -> {
            user.openPrivateChannel().thenAccept(privateChannel -> {
                privateChannel.sendMessage(EmbedLibrary.INVITE.getEmbed());
                SuccessState.SUCCESSFUL.evaluateForMessage(msg);
            });
        });
    }),
    DISCORD("discordlink", true, false, new int[]{0, 0}, msg -> {
        msg.getServerTextChannel().ifPresent(event -> {
            event.sendMessage(EmbedLibrary.DISCORD.getEmbed());
            SuccessState.SUCCESSFUL.evaluateForMessage(msg);
        });
    }),
    DONATE("donate", true, false, new int[]{0, 0}, msg -> {
        msg.getServerTextChannel().ifPresent(event -> {
            event.sendMessage(EmbedLibrary.DONATE.getEmbed());
            SuccessState.SUCCESSFUL.evaluateForMessage(msg);
        });
    }),

    SCOREBOARD("scores", false, false, new int[]{0, 0}, msg -> {
        Server srv = msg.getServer().get();
        User usr = msg.getUserAuthor().get();
        ServerTextChannel stc = msg.getServerTextChannel().get();
        DangoProcessor dangoProcessor = DangoProcessor.softGet(srv);

        dangoProcessor.sendScoreboard(stc);
        msg.delete("Done");
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
                    SuccessState.SUCCESSFUL.evaluateForMessage(msg);
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

            try {
                if (customEmojis.size() == 1) {
                    dangoProcessor.setEmoji(new Emoji(customEmojis.get(0)));
                } else if (customEmojis.size() == 0) {
                    dangoProcessor.setEmoji(new Emoji(param.get(0)));
                }
            } catch (StringIndexOutOfBoundsException e) {
                SuccessState.ERRORED.withMessage("That Emoji is not from this Server. You must specify an emoji from this Server.").evaluateForMessage(msg);
            }
        } else {
            SuccessState.ERRORED.withMessage("Too many or too few arguments.").evaluateForMessage(msg);
        }
    }),

    LEVELUP_ACTION_INTERACTION("action", false, true, new int[]{0, 3}, msg -> {
        Server srv = msg.getServer().get();
        ServerTextChannel stc = msg.getServerTextChannel().get();
        DangoProcessor dangoProcessor = DangoProcessor.softGet(srv);
        PropertiesMapper actions = dangoProcessor.actions;

        List<String> param = extractParam(msg);

        if (param.size() == 0) {
            EmbedBuilder embed = DangoBot.getBasicEmbed()
                    .setDescription("Current Levelup-Actions:");
            StringBuilder field = new StringBuilder();

            if (!actions.entrySet().isEmpty()) {
                for (Map.Entry<String, List<String>> entry : actions.entrySet()) {
                    for (List<String> that : Utils.everyOfList(2, entry.getValue())) {
                        Optional<Role> roleById = Main.API.getRoleById(that.get(1));

                        switch (that.get(0)) {
                            case "applyrole":
                                field.append(roleById.map(role -> "Add Role: " + role.getMentionTag()).orElse("Unknown Role"));
                                field.append("\n");
                                break;
                            case "removerole":
                                field.append(roleById.map(role -> "Remove Role: " + role.getMentionTag()).orElse("Unknown Role"));
                                field.append("\n");
                                break;
                            case "adddango":
                            default:
                                field.append("Add Dango");
                                field.append("\n");
                                break;
                        }
                    }

                    embed.addField("Level " + entry.getKey() + ":", field.toString());
                    field = new StringBuilder();
                }
            } else {
                embed.addField("Whoops!", "There are currently no LevelUp-Actions.");
            }

            stc.sendMessage(embed);
        } else if (param.size() >= 2 && param.size() <= 3) {
            int level = Integer.parseInt(param.get(1));
            List<Role> mentionedRoles = msg.getMentionedRoles();
            Role role;
            Optional<Role> roleOpt = mentionedRoles.stream()
                    .limit(1)
                    .findAny();

            switch (param.get(0)) {
                case "applyrole":
                    if (roleOpt.isPresent()) {
                        role = roleOpt.get();

                        if (actions.mapSize() < 25 && !actions.containsValue(level, role.getId())) {
                            dangoProcessor.addRoleAction(level, "applyrole", role);
                        } else if (!actions.containsValue(level, role.getId())) {
                            SuccessState.ERRORED.withMessage("There is already an Action with this Role!").evaluateForMessage(msg);
                        } else {
                            SuccessState.ERRORED.withMessage("There are already too many Actions!").evaluateForMessage(msg);
                        }
                    } else {
                        SuccessState.ERRORED.withMessage("No Role Found.").evaluateForMessage(msg);
                    }

                    break;
                case "removerole":
                    if (roleOpt.isPresent()) {
                        role = roleOpt.get();

                        if (actions.mapSize() < 25 && !actions.containsValue(level, role.getId())) {
                            dangoProcessor.addRoleAction(level, "removerole", role);
                        } else if (!actions.containsValue(level, role.getId())) {
                            SuccessState.ERRORED.withMessage("There is already an Action with this Role!").evaluateForMessage(msg);
                        } else {
                            SuccessState.ERRORED.withMessage("There are already too many Actions!").evaluateForMessage(msg);
                        }
                    } else {
                        SuccessState.ERRORED.withMessage("No Role Found.").evaluateForMessage(msg);
                    }

                    break;
                case "delete":
                    if (param.size() == 3) {
                        dangoProcessor.removeAction(level, param.get(2));
                    } else {
                        dangoProcessor.removeActions(level);
                    }
                    break;
            }
        } else {
            SuccessState.ERRORED.withMessage("Too many or too few arguments.",
                    "The correct use is:\n" +
                            "`dango action <Actiontitle> <Level> [Parameter]` or `dango action delete <Level> <Actiontitle>`\n" +
                            "\n" +
                            "Examples:\n" +
                            "`dango action applyrole 6 @Regular` - Applies the role @Regular for the 6th level.\n" +
                            "`dango action delete 4 ` - Removes the REMOVEROLE Action from Level 4").evaluateForMessage(msg);
        }
    }),

    ADD_AUTH("auth", false, true, new int[]{1, 10}, msg -> {
        Server srv = msg.getServer().get();
        List<User> mentionedUsers = msg.getMentionedUsers();
        Auth auth = Auth.softGet(srv);

        if (mentionedUsers.size() < 1) {
            SuccessState.ERRORED.withMessage("No User Mentions found.").evaluateForMessage(msg);
        } else {
            mentionedUsers.forEach(user -> auth.addAuth(user).evaluateForMessage(msg));
        }
    }),
    REMOVE_AUTH("unauth", false, true, new int[]{1, 10}, msg -> {
        Server srv = msg.getServer().get();
        List<User> mentionedUsers = msg.getMentionedUsers();
        Auth auth = Auth.softGet(srv);

        if (mentionedUsers.size() < 1) {
            SuccessState.ERRORED.withMessage("No User Mentions found.").evaluateForMessage(msg);
        } else {
            mentionedUsers.forEach(user -> auth.removeAuth(user).evaluateForMessage(msg));
        }
    }),
    AUTHS("auths", false, true, new int[]{0, 0}, msg -> {
        Server srv = msg.getServer().get();
        Auth auth = Auth.softGet(srv);

        msg.getServerTextChannel().ifPresent(chl -> {
            auth.sendEmbed(chl).evaluateForMessage(msg);
        });
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
