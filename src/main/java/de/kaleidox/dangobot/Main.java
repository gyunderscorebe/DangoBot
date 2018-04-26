package de.kaleidox.dangobot;

import de.kaleidox.dangobot.bot.Command;
import de.kaleidox.dangobot.bot.StatusScroll;
import de.kaleidox.dangobot.bot.specific.DangoProcessor;
import de.kaleidox.dangobot.util.Debugger;
import de.kaleidox.dangobot.util.Mapper;
import org.discordbots.api.client.DiscordBotListAPI;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Main {
    public final static ConcurrentHashMap<File, ConcurrentHashMap<String, String>> MAPS = new ConcurrentHashMap<>();
    public static DiscordApi API;
    public static DiscordBotListAPI DBLAPI;
    public static ConcurrentHashMap<String, String> authUsersMap = new ConcurrentHashMap<>();
    private static Debugger log = new Debugger(Main.class.getName());
    private static Debugger chat = new Debugger("Chat");

    public static void main(String args[]) {
        Mapper.packMaps();
        Mapper.loadMaps();

        DBLAPI = new DiscordBotListAPI
                .Builder()
                .token(DangoBot.DBL_BOT_TOKEN)
                .build();

        new DiscordApiBuilder()
                .setToken(DangoBot.BOT_TOKEN)
                .login()
                .thenAccept(api -> {
                    Main.API = api;
                    StatusScroll status = new StatusScroll(api);

                    //// Cosmetics
                    api.updateUsername(DangoBot.BOT_NAME);
                    try {
                        api.updateAvatar(new URL(DangoBot.ICON_URL));
                    } catch (MalformedURLException e) {
                        log.put("Failed to Update the Avatar");
                    }
                    api.addMessageCreateListener(chat::print);

                    //// Actual Bot Part
                    api.addMessageCreateListener(event -> {
                        Message msg = event.getMessage();
                        User usr = msg.getUserAuthor().get();

                        if (msg.isPrivate()) {
                            Optional<Command> commandOpt = Command.getCommand(msg);

                            commandOpt.ifPresent(command -> {
                                if (command.canRunPrivately) {
                                    command.runPrivate(msg);
                                } else {
                                    msg.getPrivateChannel().get().sendMessage(DangoBot.getBasicEmbed()
                                            .addField("I'm Sorry!", "I'm sorry, but you can't run this command from private chat.")
                                    );
                                }
                            });
                        } else {
                            Server srv = event.getServer().get();

                            Command.processCommand(msg);
                            DangoProcessor.softGet(srv).increment(msg);
                        }
                    });

                    //// Shenanigans
                    // Wastebaskets
                    api.addReactionAddListener(event -> {
                        Emoji emoji = event.getEmoji();
                        Message msg = event.requestMessage().join();

                        if (!event.getUser().isBot()) {
                            emoji.asUnicodeEmoji().ifPresent(then -> {
                                if (then.equals("🗑") || then.equals("\uD83D\uDEAE")) {
                                    msg.delete();
                                }
                            });
                        }
                    });

                    api.getThreadPool().getScheduler().scheduleAtFixedRate(status::update, 20, 20, TimeUnit.SECONDS); // Update the Status every 20 Seconds
                    api.getThreadPool().getScheduler().scheduleAtFixedRate(() -> DBLAPI.setStats(API.getYourself().getIdAsString(), API.getServers().size()), 1, 1, TimeUnit.MINUTES); // Update DBL server Count every Minute
                });
    }
}
