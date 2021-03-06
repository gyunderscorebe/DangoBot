package de.kaleidox.dangobot.bot;

import de.kaleidox.dangobot.DangoBot;
import de.kaleidox.dangobot.Main;
import de.kaleidox.util.Debugger;
import de.kaleidox.util.SuccessState;
import de.kaleidox.util.Utils;
import de.kaleidox.util.serializer.PropertiesMapper;
import de.kaleidox.util.serializer.SelectedPropertiesMapper;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static de.kaleidox.util.SuccessState.NOT_RUN;
import static de.kaleidox.util.SuccessState.SUCCESSFUL;
import static de.kaleidox.util.SuccessState.UNSUCCESSFUL;

public class Auth {
    private static final ConcurrentHashMap<Long, Auth> selfMap = new ConcurrentHashMap<>();
    public PropertiesMapper auths = new PropertiesMapper(new File("props/authUsers.properties"), ';');
    private Debugger log = new Debugger(Auth.class.getName());
    private Server myServer;
    private Long serverId;

    private Auth(Server server) {
        this.myServer = server;

        serverId = myServer.getId();

        Utils.safePut(selfMap, serverId, this);
    }

    /*
    This Object's "Constructor".
    Looks for an already existing instance of the required Object or creates a new one.

    @returns The adequate Instance of this Object.
     */
    public static Auth softGet(Server server) {
        return (selfMap.containsKey(server.getId()) ? selfMap.get(server.getId()) : selfMap.put(server.getId(), new Auth(server)));
    }

    public boolean isAuth(User user) {
        boolean val = false;

        // Is contained in this Servers AUTHS Entry?
        if (auths.containsValue(serverId.toString(), user.getIdAsString())) {
            val = true;
        }

        // Has MANAGE SERVER?
        if (myServer.hasPermission(user, PermissionType.MANAGE_SERVER)) {
            val = true;
        }

        // Is Admin?
        if (myServer.isAdmin(user)) {
            val = true;
        }

        // Is Bot Owner?
        if (user.isBotOwner()) {
            val = true;
        }

        return val;
    }

    public SuccessState addAuth(User user) {
        SuccessState val = SuccessState.NOT_RUN;

        auths.add(serverId.toString(), user.getIdAsString()).write();
        val = SUCCESSFUL;

        log.put("Added Auth for " + myServer.getName());

        auths.write();

        return val;
    }

    public SuccessState removeAuth(User user) {
        SuccessState val;

        if (auths.containsValue(myServer.getIdAsString(), user.getIdAsString())) {
            auths.removeValue(serverId.toString(), user.getIdAsString());
            val = SUCCESSFUL;

            log.put("Removed Auth for " + myServer.getName());
        } else {
            val = UNSUCCESSFUL;
        }

        auths.write();

        return val;
    }

    public SuccessState sendEmbed(ServerTextChannel chl) {
        AtomicReference<SuccessState> val = new AtomicReference<>();
        EmbedBuilder eb = DangoBot.getBasicEmbed();
        User usr;
        SelectedPropertiesMapper select = auths.select(serverId);

        val.set(NOT_RUN);

        eb.setTitle(DangoBot.BOT_NAME + " - **Authed Users on __" + myServer.getName() + "__:**");
        eb.setDescription("_Administrators and User with Permission \"Manage Server\" are Authed by Default._");

        if (select.size() != 0) {
            for (String x : select.getAll()) {
                usr = Main.API.getUserById(x).join(); // TODO: Change this to using then... Methods

                eb.addField(usr.getName(), usr.getMentionTag(), true);
            }

            chl.sendMessage(eb).thenRun(() -> val.set(SUCCESSFUL));
        } else {
            eb.addField("No Auths.", "There are no Auths for this Server.", false);

            chl.sendMessage(eb).thenRun(() -> val.set(UNSUCCESSFUL));
        }

        return val.get();
    }
}
