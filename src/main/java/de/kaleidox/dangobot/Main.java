package de.kaleidox.dangobot;

import de.kaleidox.dangobot.bot.Command;
import de.kaleidox.dangobot.bot.StatusScroll;
import de.kaleidox.dangobot.bot.specific.DangoProcessor;
import de.kaleidox.dangobot.bot.specific.records.UserRecord;
import de.kaleidox.dangobot.util.Debugger;
import org.discordbots.api.client.DiscordBotListAPI;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageType;
import org.javacord.api.entity.server.Server;

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
    private static Debugger log = new Debugger(Main.class.getName());
    private static Debugger chat = new Debugger("Chat");

    public static void main(String args[]) {
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
                    DBLAPI.setStats(API.getYourself().getIdAsString(), API.getServers().size());

                    //// Cosmetics
                    api.updateUsername(DangoBot.BOT_NAME);
                    try {
                        api.updateAvatar(new URL(DangoBot.ICON_URL));
                    } catch (MalformedURLException e) {
                        log.put("Failed to Update the Avatar");
                    }

                    api.addMessageCreateListener(event -> {
                        event.getServer().ifPresent(server -> {
                            if (server.getId() != 264445053596991498L) {
                                UserRecord.softGet(server).newMessage(event);
                            }
                        });
                    });

                    //// Actual Bot Part
                    api.addMessageCreateListener(event -> {
                        Message msg = event.getMessage();
                        MessageAuthor author = msg.getAuthor();

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
                            if (author.isUser()) {
                                DangoProcessor.softGet(srv).increment(msg);
                            }
                        }
                    });

                    //// Shenanigans
                    // Delete Own PINNED Messages
                    api.addMessageCreateListener(event -> {
                        MessageType type = event.getMessage()
                                .getType();
                        MessageAuthor author = event.getMessage().getAuthor();

                        if (type == MessageType.CHANNEL_PINNED_MESSAGE && author.isYourself()) {
                            event.getMessage()
                                    .delete("Unneccesary");
                        }
                    });

                    api.getThreadPool().getScheduler().scheduleAtFixedRate(status::update, 20, 20, TimeUnit.SECONDS); // Update the Status every 20 Seconds
                    api.getThreadPool().getScheduler().scheduleAtFixedRate(() -> {
                        if (!DangoBot.isTesting)
                            DBLAPI.setStats(API.getYourself().getIdAsString(), API.getServers().size());
                    }, 1, 1, TimeUnit.MINUTES); // Update DBL server Count every Minute
                    api.getThreadPool().getScheduler().scheduleAtFixedRate(DangoProcessor::updateScoreboards, 30, 30, TimeUnit.MINUTES); // update old leaderboards
                });
    }
}
