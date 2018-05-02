package de.kaleidox.dangobot.util;

import com.vdurmont.emoji.EmojiParser;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.core.entity.emoji.UnicodeEmojiImpl;

public class Emoji {
    private org.javacord.api.entity.emoji.Emoji emoji;

    public Emoji(org.javacord.api.entity.emoji.Emoji emoji) {
        if (emoji instanceof KnownCustomEmoji) {
            this.emoji = emoji.asKnownCustomEmoji().get();
        } else if (emoji instanceof UnicodeEmojiImpl) {
            this.emoji = UnicodeEmojiImpl.fromString(EmojiParser.parseToAliases(emoji.getMentionTag()));
        } else {
            this.emoji = null;
            throw new NullPointerException("Not an Emoji");
        }
    }

    public Emoji(String unicodeEmoji) {
        this.emoji = UnicodeEmojiImpl.fromString(EmojiParser.parseToAliases(unicodeEmoji));
    }

    public String getPrintable() {
        if (emoji instanceof KnownCustomEmoji) {
            return emoji.asKnownCustomEmoji().get().getMentionTag();
        } else if (emoji instanceof UnicodeEmojiImpl) {
            return EmojiParser.parseToUnicode(emoji.getMentionTag());
        } else {
            return "❌";
        }
    }

    @Override
    public String toString() {
        return getPrintable();
    }
}
