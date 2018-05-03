package de.kaleidox.dangobot.bot;

import de.kaleidox.dangobot.DangoBot;
import de.kaleidox.dangobot.Main;
import de.kaleidox.dangobot.bot.libraries.EmbedLibrary;
import de.kaleidox.dangobot.bot.libraries.HelpLibrary;
import de.kaleidox.dangobot.bot.specific.DangoProcessor;
import de.kaleidox.dangobot.discord.ui.Response;
import de.kaleidox.dangobot.util.Debugger;
import de.kaleidox.dangobot.util.Emoji;
import de.kaleidox.dangobot.util.ServerPreferences;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static de.kaleidox.dangobot.util.ServerPreferences.Variable.*;

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
    INVITE(new String[]{"invite", "invitelink"}, true, false, new int[]{0, 0}, msg -> {
        msg.getUserAuthor().ifPresent(user -> {
            user.openPrivateChannel().thenAccept(privateChannel -> {
                privateChannel.sendMessage(EmbedLibrary.INVITE.getEmbed());
                SuccessState.SUCCESSFUL.evaluateForMessage(msg);
            });
        });
    }),
    DISCORD(new String[]{"discord", "discordlink"}, true, false, new int[]{0, 0}, msg -> {
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

    SELF_STATS(new String[]{"self", "own", "my", "mine"}, false, false, new int[]{0, 0}, msg -> {
        Server srv = msg.getServer().get();
        User usr = msg.getUserAuthor().get();
        ServerTextChannel stc = msg.getServerTextChannel().get();
        DangoProcessor dangoProcessor = DangoProcessor.softGet(srv);

        dangoProcessor.sendUserScore(stc, usr);
    }),
    SCOREBOARD(new String[]{"stats", "scores", "score", "scoreboard"}, false, false, new int[]{0, 0}, msg -> {
        Server srv = msg.getServer().get();
        User usr = msg.getUserAuthor().get();
        ServerTextChannel stc = msg.getServerTextChannel().get();
        DangoProcessor dangoProcessor = DangoProcessor.softGet(srv);

        dangoProcessor.sendScoreboard(stc, false)
                .evaluateForMessage(msg);
    }),

    GIVE(new String[]{"give", "add"}, false, true, new int[]{1, 2}, msg -> {
        Server srv = msg.getServer().get();
        User usr = msg.getUserAuthor().get();
        ServerTextChannel stc = msg.getServerTextChannel().get();
        DangoProcessor dangoProcessor = DangoProcessor.softGet(srv);
        List<User> mentionedUsers = msg.getMentionedUsers();
        List<String> param = extractParam(msg);

        if (mentionedUsers.size() == 1) {
            User user = mentionedUsers.get(0);

            if (param.size() == 2) {
                String s = param.get(1);

                if (s.matches("[0-9]+")) {
                    int i = Integer.parseInt(s);
                    dangoProcessor.giveDango(user, stc, i);
                    dangoProcessor.sendUserScore(stc, user);

                    SuccessState.SUCCESSFUL
                            .evaluateForMessage(msg);
                } else {
                    dangoProcessor.giveDango(user, stc);
                    dangoProcessor.sendUserScore(stc, user);

                    SuccessState.SUCCESSFUL
                            .withMessage("You did not specify a valid number, so " + user.getNickname(srv).orElseGet(user::getName) + " got 1 Dango.")
                            .evaluateForMessage(msg);
                }
            } else {
                dangoProcessor.giveDango(user, stc);
                dangoProcessor.sendUserScore(stc, user);

                SuccessState.SUCCESSFUL
                        .withMessage("You did not specify a number, so " + user.getNickname(srv).orElseGet(user::getName) + " got 1 Dango.")
                        .evaluateForMessage(msg);
            }
        } else {
            SuccessState.ERRORED
                    .withMessage("No user found", "You didn't mention an User!")
                    .evaluateForMessage(msg);
        }
    }),
    TAKE(new String[]{"take", "remove"}, false, true, new int[]{1, 2}, msg -> {
        Server srv = msg.getServer().get();
        User usr = msg.getUserAuthor().get();
        ServerTextChannel stc = msg.getServerTextChannel().get();
        DangoProcessor dangoProcessor = DangoProcessor.softGet(srv);
        List<User> mentionedUsers = msg.getMentionedUsers();
        List<String> param = extractParam(msg);

        if (mentionedUsers.size() == 1) {
            User user = mentionedUsers.get(0);

            if (param.size() == 2) {
                String s = param.get(1);

                if (s.matches("[0-9]+")) {
                    int i = Integer.parseInt(s);
                    dangoProcessor.removeDango(user, stc, i);
                    dangoProcessor.sendUserScore(stc, user);

                    SuccessState.SUCCESSFUL
                            .evaluateForMessage(msg);
                } else {
                    dangoProcessor.removeDango(user, stc, 1);
                    dangoProcessor.sendUserScore(stc, user);

                    SuccessState.SUCCESSFUL
                            .withMessage("You did not specify a valid number, so " + user.getNickname(srv).orElseGet(user::getName) + " lost 1 Dango.")
                            .evaluateForMessage(msg);
                }
            } else {
                dangoProcessor.removeDango(user, stc, 1);
                dangoProcessor.sendUserScore(stc, user);

                SuccessState.SUCCESSFUL
                        .withMessage("You did not specify a number, so " + user.getNickname(srv).orElseGet(user::getName) + " lost 1 Dango.")
                        .evaluateForMessage(msg);
            }
        } else {
            SuccessState.ERRORED
                    .withMessage("No user found", "You didn't mention an User!")
                    .evaluateForMessage(msg);
        }
    }),
    REVOKE("revoke", false, true, new int[]{0, 1}, msg -> {
        Server srv = msg.getServer().get();
        User usr = msg.getUserAuthor().get();
        ServerTextChannel stc = msg.getServerTextChannel().get();
        DangoProcessor dangoProcessor = DangoProcessor.softGet(srv);
        List<String> param = extractParam(msg);

        if (param.size() == 1) {
            if (Boolean.valueOf(param.get(0))) {
                dangoProcessor.revokeDango();
            }
        } else {
            Response.areYouSure(stc, usr, "Are you sure?", "Do you really want to revoke the last Dango?", 30, TimeUnit.SECONDS)
                    .thenAcceptAsync(yesno -> {
                        if (yesno) {
                            dangoProcessor.revokeDango();

                            SuccessState.SUCCESSFUL
                                    .evaluateForMessage(msg);
                        }
                    });
        }
    }),
    CLEAR(new String[]{"reset", "clear"}, false, true, new int[]{0, 0}, msg -> {
        Server srv = msg.getServer().get();
        User usr = msg.getUserAuthor().get();
        ServerTextChannel stc = msg.getServerTextChannel().get();
        DangoProcessor dangoProcessor = DangoProcessor.softGet(srv);

        Response.areYouSure(stc, usr, "Are you sure?", "Do you really want to reset this server's scores?", 30, TimeUnit.SECONDS)
                .thenAcceptAsync(yesno -> {
                    if (yesno) {
                        dangoProcessor.clearAll();
                    }
                });
    }),

    COUNT_INTERACTION(new String[]{"count", "per", "every"}, false, true, new int[]{0, 1}, msg -> {
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

                    stc.sendMessage(DangoBot.getBasicEmbed()
                            .addField("New Counter:", String.valueOf(dangoProcessor.getCounterMax()))
                    );
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
    EMOJI_INTERACTION(new String[]{"emoji", "custom"}, false, true, new int[]{0, 1}, msg -> {
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
            } catch (NullPointerException e) {
                SuccessState.ERRORED.withMessage("That Emoji is not from this Server. You must specify an emoji from this Server.").evaluateForMessage(msg);
            } finally {
                SuccessState.SUCCESSFUL.evaluateForMessage(msg);

                stc.sendMessage(DangoBot.getBasicEmbed()
                        .addField("New Emoji:", dangoProcessor.getEmoji().getPrintable())
                );
            }
        } else {
            SuccessState.ERRORED.withMessage("Too many or too few arguments.").evaluateForMessage(msg);
        }
    }),

    LEVELUP_ACTION_INTERACTION(new String[]{"levelupaction", "action"}, false, true, new int[]{0, 3}, msg -> {
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
            if (param.get(1).matches("[0-9]+")) {
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
                SuccessState.ERRORED.withMessage("Too many or too few arguments, or wrong argument order.",
                        "The correct use is:\n" +
                                "`dango action <Actiontitle> <Level> [Parameter]` or `dango action delete <Level> <Actiontitle>`\n" +
                                "\n" +
                                "Examples:\n" +
                                "`dango action applyrole 6 @Regular` - Applies the role @Regular for the 6th level.\n" +
                                "`dango action delete 4 ` - Removes the REMOVEROLE Action from Level 4").evaluateForMessage(msg);
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

    ADD_AUTH(new String[]{"auth", "auth-add"}, false, true, new int[]{1, 10}, msg -> {
        Server srv = msg.getServer().get();
        List<User> mentionedUsers = msg.getMentionedUsers();
        Auth auth = Auth.softGet(srv);

        if (mentionedUsers.size() < 1) {
            SuccessState.ERRORED.withMessage("No User Mentions found.").evaluateForMessage(msg);
        } else {
            mentionedUsers.forEach(user -> auth.addAuth(user).evaluateForMessage(msg));
        }
    }),
    REMOVE_AUTH(new String[]{"unauth", "auth-remove"}, false, true, new int[]{1, 10}, msg -> {
        Server srv = msg.getServer().get();
        List<User> mentionedUsers = msg.getMentionedUsers();
        Auth auth = Auth.softGet(srv);

        if (mentionedUsers.size() < 1) {
            SuccessState.ERRORED.withMessage("No User Mentions found.").evaluateForMessage(msg);
        } else {
            mentionedUsers.forEach(user -> auth.removeAuth(user).evaluateForMessage(msg));
        }
    }),
    AUTHS(new String[]{"auths", "auth-list"}, false, true, new int[]{0, 0}, msg -> {
        Server srv = msg.getServer().get();
        Auth auth = Auth.softGet(srv);

        msg.getServerTextChannel().ifPresent(chl -> {
            auth.sendEmbed(chl).evaluateForMessage(msg);
        });
    }),

    PREFERENCE(new String[]{"setup", "pref", "preference"}, false, true, new int[]{0, 2}, msg -> {
        Server srv = msg.getServer().get();
        ServerTextChannel stc = msg.getServerTextChannel().get();
        List<String> param = extractParam(msg);
        List<ServerTextChannel> mentionedChannels = msg.getMentionedChannels();
        ServerPreferences serverPreferences = ServerPreferences.softGet(srv);

        if (param.size() == 0) {
            // post preferences
            EmbedBuilder basicEmbed = DangoBot.getBasicEmbed();

            Arrays.asList(ServerPreferences.Variable.values())
                    .forEach(variable -> {
                        basicEmbed.addField("" +
                                variable.name, "" +
                                serverPreferences.getVariable(variable));
                    });

            stc.sendMessage(basicEmbed);
        } else {
            // edit preferences
            if (param.size() == 2) {
                Optional<ServerPreferences.Variable> variableOptional = ServerPreferences.Variable.getVariable(param.get(0));

                if (variableOptional.isPresent()) {
                    ServerPreferences.Variable variable = variableOptional.get();

                    serverPreferences.setVariable(variable, param.get(1))
                            .evaluateForMessage(msg);
                } else {
                    StringBuilder variables = new StringBuilder();
                    Arrays.asList(ServerPreferences.Variable.values())
                            .forEach(variable -> {
                                variables
                                        .append("- `")
                                        .append(variable.name)
                                        .append("`\n");
                            });

                    SuccessState.ERRORED
                            .withMessage("Unknown Variable Name!", "Possible Variables are:\n" + variables.substring(0, variables.length() - 1))
                            .evaluateForMessage(msg);
                }
            } else {
                StringBuilder variables = new StringBuilder();
                Arrays.asList(ServerPreferences.Variable.values())
                        .forEach(variable -> {
                            variables
                                    .append("- `")
                                    .append(variable.name)
                                    .append("`\n");
                        });

                SuccessState.ERRORED
                        .withMessage("Not enough or too many Arguments!", "Possible Variables are:\n" + variables.substring(0, variables.length() - 1))
                        .evaluateForMessage(msg);
            }
        }
    }),

    TESTCOMMAND("testcommand", false, true, new int[]{0, 0}, msg -> {
        Server srv = msg.getServer().get();
        Channel chl = msg.getChannel();
        User usr = msg.getUserAuthor().get();

        if (msg.getAuthor().isBotOwner()) {
            chl.asServerTextChannel().ifPresent(stc -> {
                // do stuff
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
    private String[] keywords;
    private boolean requiresAuth;
    // Command Usage Stuff
    private int[] parameterRange;
    private Consumer<Message> consumer;

    Command(String keyword, boolean canRunPrivately, boolean requiresAuth, int[] parameterRange, Consumer<Message> consumer) {
        this.keywords = new String[]{keyword};
        this.canRunPrivately = canRunPrivately;
        this.requiresAuth = requiresAuth;
        this.parameterRange = parameterRange;
        this.consumer = consumer;
    }

    Command(String[] keywords, boolean canRunPrivately, boolean requiresAuth, int[] parameterRange, Consumer<Message> consumer) {
        this.keywords = keywords;
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
        ServerPreferences serverPreferences = ServerPreferences.softGet(srv);

        if (!msg.isPrivate()) {
            if (serverPreferences.getVariable(COMMAND_CHANNEL).equals("none") || serverPreferences.getVariable(COMMAND_CHANNEL).equals(chl.getIdAsString())) {
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
                            } else {
                                SuccessState.UNAUTHORIZED.evaluateForMessage(msg);
                            }

                            log.put("Satisfier: " + satisfier, true);
                            if (satisfier == 2) {
                                cmd.consumer.accept(msg);
                                val = SuccessState.SUCCESSFUL;
                            }
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
            return VALUES.stream()
                    .filter(c -> Arrays.stream(c.keywords)
                            .anyMatch(w -> w.equals(keyword)))
                    .findAny();
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
