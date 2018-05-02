package de.kaleidox.dangobot.util;

import de.kaleidox.dangobot.Main;
import de.kaleidox.dangobot.util.serializer.SelectedPropertiesMapper;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.server.Server;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ServerPreferences {
    private static final ConcurrentHashMap<Long, ServerPreferences> selfMap = new ConcurrentHashMap<>();
    public SelectedPropertiesMapper settings = new SelectedPropertiesMapper(new File("props/serverPreferences.properties"), ';');
    private Debugger log;
    private Server myServer;
    private Long serverId;

    public String commandChannelId;

    private ServerPreferences(Server server) {
        this.myServer = server;

        serverId = myServer.getId();

        commandChannelId = settings.softGet(0, "none");

        log = new Debugger(ServerPreferences.class.getName(), server.getName());

        Utils.safePut(selfMap, serverId, this);
    }

    /*
    This Object's "Constructor".
    Looks for an already existing instance of the required Object or creates a new one.

    @returns The adequate Instance of this Object.
     */
    public static ServerPreferences softGet(Server server) {
        return (selfMap.containsKey(server.getId()) ? selfMap.get(server.getId()) : selfMap.put(server.getId(), new ServerPreferences(server)));
    }

    public void setCommandChannel(ServerTextChannel serverTextChannel) {
        commandChannelId = serverTextChannel.getIdAsString();
        settings.set(0, commandChannelId);
        settings.write();
    }

    public Optional<Channel> getCommandChannel() {
        return (commandChannelId.equals("none") ? Optional.empty() : Main.API.getChannelById(commandChannelId));
    }
}
