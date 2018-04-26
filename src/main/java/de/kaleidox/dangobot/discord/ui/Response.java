package de.kaleidox.dangobot.discord.ui;

import com.vdurmont.emoji.EmojiParser;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.util.logging.ExceptionLogger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class Response {
    public static void addInfoReaction(Message message, String emojiTag, Boolean deleteAfterSend, EmbedBuilder infoEmbed) {
        String emoji = EmojiParser.parseToUnicode(emojiTag);
        AtomicReference<Message> sentMessage = new AtomicReference<>();

        message.addReaction(emoji)
                .exceptionally(ExceptionLogger.get());

        MessageDeleteListener deleteListener = event -> {
            message.removeOwnReactionByEmoji(emoji);
            message.delete();
            message.getMessageAttachableListeners().forEach((key, value) -> message.removeMessageAttachableListener(key));
        };

        ReactionAddListener addListener = event -> {
            if (!event.getUser().isYourself() && event.getEmoji().asUnicodeEmoji().map(emoji::equals).orElse(false)) {
                message.getChannel().sendMessage(infoEmbed)
                        .thenAccept(myMsg -> {
                            sentMessage.set(myMsg);
                            myMsg.addMessageAttachableListener(deleteListener);
                        })
                        .thenAccept(nothing -> {
                            if (deleteAfterSend) {
                                message.delete().exceptionally(ExceptionLogger.get());
                            }
                        })
                        .exceptionally(ExceptionLogger.get());
            }
        };

        ReactionRemoveListener removeListener = event -> event.getEmoji().asUnicodeEmoji()
                .filter(emoji::equals)
                .ifPresent(unicodeEmoji -> sentMessage.get().delete().exceptionally(ExceptionLogger.get()));

        message.addReactionAddListener(addListener);
        message.addReactionRemoveListener(removeListener);
    }

    public static void addInfoReaction(CompletableFuture<Message> msgFut, String emojiTag, Boolean deleteAfterSend, EmbedBuilder infoEmbed) {
        addInfoReaction(msgFut.join(), emojiTag, deleteAfterSend, infoEmbed);
    }

    public static void addInfoReaction(Message message, EmbedBuilder infoEmbed) {
        addInfoReaction(message, "ℹ", false, infoEmbed);
    }

    public static void addInfoReaction(CompletableFuture<Message> msgFut, EmbedBuilder infoEmbed) {
        addInfoReaction(msgFut.join(), "ℹ", false, infoEmbed);
    }
}
