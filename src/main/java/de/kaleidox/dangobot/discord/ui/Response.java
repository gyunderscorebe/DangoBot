package de.kaleidox.dangobot.discord.ui;

import com.vdurmont.emoji.EmojiParser;
import de.kaleidox.dangobot.DangoBot;
import de.kaleidox.dangobot.Main;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.util.logging.ExceptionLogger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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

    public static CompletableFuture<Boolean> areYouSure(TextChannel textChannel, User user, String questionTitle, String questionText, long timeout, TimeUnit timeUnit) {
        CompletableFuture<Boolean> response = new CompletableFuture<>();
        AtomicReference<Message> sentMessage = new AtomicReference<>();

        ReactionAddListener addListener = event -> {
            if (!event.getUser().isYourself() && event.getUser().equals(user)) {
                event.requestMessage().thenAccept(message -> {
                    event.getEmoji().asUnicodeEmoji().ifPresent(emoji -> {
                        switch (emoji) {
                            case "✅":
                                response.complete(true);
                                break;
                            case "❌":
                                response.complete(false);
                                break;
                            default:
                                break;
                        }
                    });
                });
            }
        };

        textChannel.sendMessage(DangoBot.getBasicEmbed()
                .addField(questionTitle, questionText)
                .setDescription("This Message will timeout after " + timeout + " " + timeUnit.name() + " with \"No\".")
        )
                .thenAccept(msg -> {
                    msg.addReaction("✅");
                    msg.addReaction("❌");

                    sentMessage.set(msg);

                    msg.addMessageAttachableListener(addListener);

                    Main.API.getThreadPool().getScheduler().schedule(() -> {
                        msg.getMessageAttachableListeners().forEach((key, value) -> msg.removeMessageAttachableListener(key));
                        msg.delete();
                        response.complete(false);
                    }, timeout, timeUnit);
                });

        response.thenRun(() -> {
            Message msg = sentMessage.get();

            msg.getMessageAttachableListeners().forEach((key, value) -> msg.removeMessageAttachableListener(key));
            msg.delete();
            response.complete(false);
        });

        return response;
    }

    public static CompletableFuture<Boolean> areYouSure(TextChannel textChannel, User user) {
        return areYouSure(textChannel, user, "Are you sure?", "Are you sure you want to do that?", 30, TimeUnit.SECONDS);
    }

    public static CompletableFuture<Boolean> areYouSure(TextChannel textChannel, User user, String questionTitle, String questionText) {
        return areYouSure(textChannel, user, questionTitle, questionText, 30, TimeUnit.SECONDS);
    }
}
