package de.kaleidox.dangobot.discord.ui;

import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;

public class Utils {
    public static void addWastebasket(Message msg) {
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
    }
}
