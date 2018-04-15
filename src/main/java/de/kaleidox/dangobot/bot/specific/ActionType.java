package de.kaleidox.dangobot.bot.specific;

import java.util.Arrays;
import java.util.Optional;

public enum ActionType {
    GIVE_DANGO("dango"),
    GIVE_RANK("rank");

    public String identifier;

    ActionType(String identifier) {
        this.identifier = identifier;
    }

    public static Optional<ActionType> find(String identifier) {
        return Arrays.stream(ActionType.values())
                .filter(e -> e.identifier.equals(identifier))
                .findAny();
    }
}
