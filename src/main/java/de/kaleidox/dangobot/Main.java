package de.kaleidox.dangobot;

import de.kaleidox.dangobot.bot.Auth;
import de.kaleidox.dangobot.bot.Command;
import de.kaleidox.dangobot.bot.StatusScroll;
import de.kaleidox.dangobot.bot.specific.DangoProcessor;
import de.kaleidox.dangobot.util.Debugger;
import de.kaleidox.dangobot.util.Mapper;
import de.kaleidox.dangobot.util.SuccessState;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Main {
    public final static HashMap<String, ConcurrentHashMap<String, String>> MAPS = new HashMap<>();
    public static DiscordApi API;
    public static ConcurrentHashMap<String, String> authUsersMap = new ConcurrentHashMap<>();

    private static Debugger log = new Debugger(Main.class.getName());

    public static ConcurrentHashMap<Long, Auth> authInstancesMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Long, DangoProcessor> dangoProcessorMap = new ConcurrentHashMap<>();

    public static void main(String args[]) {
        Mapper.packMaps();
        Mapper.loadMaps();

        new DiscordApiBuilder()
                .setToken(Nub.BOT_TOKEN)
                .login()
                .thenAccept(api -> {
                    Main.API = api;
                    StatusScroll status = new StatusScroll(api);

                    //// Cosmetics
                    api.updateUsername("Dango Bot");

                    //// Actual Bot Part
                    api.addMessageCreateListener(event -> {
                        Message msg = event.getMessage();
                        User usr = msg.getUserAuthor().get();

                        if (msg.isPrivate()) {
                            // private stuff
                        } else {
                            Server srv = event.getServer().get();

                            SuccessState commandState = Command.processCommand(msg);

                            if (commandState == SuccessState.NOT_RUN) {
                                DangoProcessor.softGet(srv).increment(usr);
                            } else commandState.evaluateForMessage(msg);
                        }
                    });

                    //// Shenanigans
                    // Wastebaskets
                    api.addMessageCreateListener(msgAdd -> {
                        Message msg = msgAdd.getMessage();

                        if (msg.getAuthor().isYourself() && !msg.getPrivateChannel().isPresent()) {
                            msg.addReaction("🗑");
                            msg.addReactionAddListener(reaAdd -> {
                                Emoji emoji = reaAdd.getEmoji();

                                if (!reaAdd.getUser().isBot()) {
                                    emoji.asUnicodeEmoji().ifPresent(then -> {
                                        if (then.equals("🗑")) {
                                            msg.delete();
                                        }
                                    });
                                }
                            });
                        }
                    });

                    api.getThreadPool().getScheduler().scheduleAtFixedRate(Mapper::saveMaps, 30, 30, TimeUnit.SECONDS); // Saving Maps every 30 Seconds
                    api.getThreadPool().getScheduler().scheduleAtFixedRate(status::update, 20, 20, TimeUnit.SECONDS); // Update the Status every 20 Seconds
                });
    }
}
