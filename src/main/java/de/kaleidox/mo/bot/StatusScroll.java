package de.kaleidox.mo.bot;


import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;

public class StatusScroll {
    private DiscordApi api;

    public StatusScroll(DiscordApi api) {
        this.api = api;
    }

    public void update() {
        api.updateActivity("with Levels", ActivityType.PLAYING);
    }
}
