package de.kaleidox.util.listeners;

import org.javacord.api.listener.message.MessageDeleteListener;

public final class MessageListeners {
    public final static MessageDeleteListener MESSAGE_DELETE_CLEANUP = event -> {
        event.getMessage()
                .ifPresent(message -> {
                    message.getMessageAttachableListeners().forEach((key, value) -> message.removeMessageAttachableListener(key));
                });
    };
}
