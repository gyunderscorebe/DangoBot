package de.kaleidox.mo;

import de.kaleidox.mo.bot.Auth;
import de.kaleidox.mo.bot.Command;
import de.kaleidox.mo.bot.StatusScroll;
import de.kaleidox.mo.util.Mapper;
import de.kaleidox.mo.util.SuccessState;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Main {
    public final static HashMap<String, ConcurrentHashMap<String, String>> MAPS = new HashMap<>();
    public static DiscordApi API;
    public static ConcurrentHashMap<String, String> authUsersMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> channelEmojiMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> channelConfigMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> activeChannelsMap = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<Long, Auth> authInstancesMap = new ConcurrentHashMap<>();

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

                    //// Actual Bot Part
                    api.addMessageCreateListener(event -> {
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
