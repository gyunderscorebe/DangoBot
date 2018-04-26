package de.kaleidox.dangobot.bot;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;

public class StatusScroll {
    private DiscordApi api;
    private int thisMessage;

    public StatusScroll(DiscordApi api) {
        this.api = api;
        thisMessage = 0;
    }

    public void update() {
        switch (thisMessage) {
            case 0:
            case 1:
                api.updateActivity(" all of your Secrets.", ActivityType.WATCHING);
                thisMessage = 2;
                break;
            case 2:
                api.updateActivity(" on " + api.getServers().size() + " Servers.", ActivityType.PLAYING);
                thisMessage = 3;
                break;
            case 3:
                api.updateActivity(" to \"dango help\"", ActivityType.LISTENING);
                thisMessage = 1;
                break;
        }
    }
}
