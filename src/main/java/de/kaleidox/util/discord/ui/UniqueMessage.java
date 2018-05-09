package de.kaleidox.util.discord.ui;

import de.kaleidox.util.Emoji;
import de.kaleidox.util.listeners.MessageListeners;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class UniqueMessage {
    private final static ConcurrentHashMap<Messageable, UniqueMessage> selfMap = new ConcurrentHashMap<>();
    private final static String REFRESH_EMOJI = "\uD83D\uDD04";

    private Messageable parent;
    private Supplier<String> refresher;

    private Message lastMessage = null;
    private ReactionAddListener refreshListenerAdd;
    private ReactionRemoveListener refreshListenerRem;

    private UniqueMessage(Messageable inParent, Supplier<String> refresher) {
        this.parent = inParent;
        this.refresher = refresher;

        refreshListenerAdd = event -> {
            if (!event.getUser().isYourself()) {
                Emoji emoji = new Emoji(event.getEmoji());

                if (emoji.getPrintable().equals(REFRESH_EMOJI)) {
                    this.refresh();
                }
            }
        };

        refreshListenerRem = event -> {
            if (!event.getUser().isYourself()) {
                Emoji emoji = new Emoji(event.getEmoji());

                if (emoji.getPrintable().equals(REFRESH_EMOJI)) {
                    this.refresh();
                }
            }
        };

        inParent.sendMessage(refresher.get())
                .thenAcceptAsync(msg -> {
                    lastMessage = msg;
                    msg.addMessageAttachableListener(MessageListeners.MESSAGE_DELETE_CLEANUP);
                    msg.addMessageAttachableListener(refreshListenerAdd);
                    msg.addMessageAttachableListener(refreshListenerRem);
                    msg.addReaction(REFRESH_EMOJI);
                });
    }

    public final static UniqueMessage get(Messageable forParent, Supplier<String> defaultRefresher) {
        if (selfMap.containsKey(forParent)) {
            UniqueMessage val = selfMap.get(forParent);
            val.resend();

            return val;
        } else {
            return selfMap.put(forParent,
                    new UniqueMessage(
                            forParent,
                            defaultRefresher
                    )
            );
        }
    }

    public void refresh() {
        if (lastMessage != null) {
            lastMessage.edit(
                    refresher.get()
            );
        }
    }

    public void resend() {
        if (lastMessage != null) {
            lastMessage.delete("Outdated");
        }

        parent.sendMessage(
                refresher.get()
        ).thenAcceptAsync(msg -> {
            lastMessage = msg;
            msg.addMessageAttachableListener(MessageListeners.MESSAGE_DELETE_CLEANUP);
            msg.addMessageAttachableListener(refreshListenerAdd);
            msg.addMessageAttachableListener(refreshListenerRem);
            msg.addReaction(REFRESH_EMOJI);
        });
    }
}
