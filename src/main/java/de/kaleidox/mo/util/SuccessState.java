package de.kaleidox.mo.util;

import de.kaleidox.mo.Nub;
import de.kaleidox.mo.discord.ui.Respond;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.util.Arrays;
import java.util.HashSet;

public enum SuccessState {
    NOT_RUN(false),
    ERRORED(false),
    UNAUTHORIZED(false),
    UNSUCCESSFUL(false),
    SUCCESSFUL(true);

    public static final HashSet<SuccessState> VALUES = new HashSet<SuccessState>() {{
        this.addAll(Arrays.asList(SuccessState.values()));
    }};
    private boolean value;
    private String title, text;
    private boolean inline, hasMessage = false;

    SuccessState(boolean value) {
        this.value = value;
    }

    public boolean asBoolean() {
        return value;
    }

    public void evaluateForMessage(Message msg) {
        switch (this) {
            case SUCCESSFUL:
                msg.addReaction("✅");
                break;
            case UNSUCCESSFUL:
                Respond.addInfoReaction(msg, "❓", false, this.getMessageEmbed());
                break;
            case ERRORED:
                Respond.addInfoReaction(msg, "❌", false, this.getMessageEmbed());
                break;
            case UNAUTHORIZED:
                Respond.addInfoReaction(msg, "⛔", false, this.hasMessage ? this.getMessageEmbed() : Nub.getBasicEmbed()
                        .addField("You do not have access to that Command.", "If you think this is a mistake, please Contact an Administrator.")
                );
                break;
            default:
                break;
        }
    }

    public SuccessState withMessage(String text) {
        return withMessage(null, text, false);
    }

    public SuccessState withMessage(String title, String text) {
        return withMessage(title, text, false);
    }

    public SuccessState withMessage(String title, String text, boolean inline) {
        this.title = title;
        this.text = text;
        this.inline = inline;

        hasMessage = true;

        return this;
    }

    public EmbedBuilder getMessageEmbed() {
        return getMessageEmbed(Nub.getBasicEmbed());
    }

    public EmbedBuilder getMessageEmbed(EmbedBuilder baseEmbed) {
        if (hasMessage && this != SUCCESSFUL)
            return baseEmbed.addField(title == null ? "Title not Defined." : title, text, inline);
        else if (this == SUCCESSFUL)
            return baseEmbed.addField("You're fine.", "Everything's fine.");
        else
            throw new NullPointerException("No message defined for SuccessState@" + this.hashCode() + ".");
    }

    public String getMessageText() {
        return text;
    }
}
