package de.kaleidox.dangobot.bot.specific.records;

import de.kaleidox.dangobot.util.Debugger;
import de.kaleidox.dangobot.util.Utils;
import de.kaleidox.dangobot.util.serializer.PropertiesMapper;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class UserRecord {
    private static final ConcurrentHashMap<Server, UserRecord> selfMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<User, UserEntry> userEntries = new ConcurrentHashMap<>();
    private Debugger log = new Debugger(UserRecord.class.getName());
    private Server myServer;
    private Long serverId;
    private PropertiesMapper values;

    private UserRecord(Server server) {
        this.myServer = server;

        serverId = myServer.getId();

        File folder = new File("props/userRecords/" + serverId + "/");
        folder.mkdirs();

        File main = new File("props/userRecords/" + serverId + "/default.properties");

        if (!main.exists()) {
            try {
                main.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.values = new PropertiesMapper(main, ';');

        Utils.safePut(selfMap, server, this);
    }

    /*
    This Object's "Constructor".
    Looks for an already existing instance of the required Object or creates a new one.

    @returns The adequate Instance of this Object.
     */
    public static UserRecord softGet(Server server) {
        return (selfMap.containsKey(server) ? selfMap.get(server) : selfMap.put(server, new UserRecord(server)));
    }

    public void newMessage(MessageCreateEvent event) {
    }

    // TODO Scheduled to a later release
}
