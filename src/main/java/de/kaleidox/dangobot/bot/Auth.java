package de.kaleidox.dangobot.bot;

import de.kaleidox.dangobot.DangoBot;
import de.kaleidox.dangobot.Main;
import de.kaleidox.dangobot.util.Debugger;
import de.kaleidox.dangobot.util.Mapper;
import de.kaleidox.dangobot.util.SuccessState;
import de.kaleidox.dangobot.util.serializer.PropertiesMapper;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class Auth {
    private Debugger log = new Debugger(Auth.class.getName());

    private Server myServer;
    private Long serverId;
    private PropertiesMapper auths = new PropertiesMapper(new File("props/authUsers.properties"), ';');

    private Auth(Server server) {
        this.myServer = server;

        serverId = myServer.getId();

        Mapper.safePut(Main.authInstancesMap, serverId, this);
    }

    /*
    This Object's "Constructor".
    Looks for an already existing instance of the required Object or creates a new one.

    @returns The adequate Instance of this Object.
     */
    public static Auth softGet(Server server) {
        return (Main.authInstancesMap.containsKey(server.getId()) ? Main.authInstancesMap.get(server.getId()) : Main.authInstancesMap.put(server.getId(), new Auth(server)));
    }

    public boolean isAuth(User user) {
        boolean val = false;

        // Is contained in this Servers AUTHS Entry?
        if (auths.containsKey(serverId.toString())) {
            if (auths.getAll(serverId.toString()).contains(user.getIdAsString())) {
                val = true;
            }
        }

        // Has MODIFY SERVER?
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

    public CompletableFuture<SuccessState> addAuth(User user) {
        CompletableFuture<SuccessState> val = new CompletableFuture<>();

        auths.add(serverId.toString(), user.getIdAsString()).write();
        val.complete(SuccessState.SUCCESSFUL);

        log.put("Added Auth for " + myServer.getName());

        return val;
    }

    public CompletableFuture<SuccessState> removeAuth(User user) {
        CompletableFuture<SuccessState> val = new CompletableFuture<>();

        if (auths.containsValue(myServer.getIdAsString(), user.getIdAsString())) {
            auths.removeValue(serverId.toString(), user.getIdAsString()).write();
            val.complete(SuccessState.SUCCESSFUL);

            log.put("Removed Auth for " + myServer.getName());
        } else {
            val.complete(SuccessState.UNSUCCESSFUL);
        }

        return val;
    }

    public CompletableFuture<SuccessState> sendEmbed(ServerTextChannel chl) {
        CompletableFuture<SuccessState> val = new CompletableFuture<>();
        EmbedBuilder eb = DangoBot.getBasicEmbed();
        User usr;

        eb.setTitle(DangoBot.BOT_NAME + " - **Authed Users on __" + myServer.getName() + "__:**");
        eb.setDescription("_Administrators and User with Permission \"Manage Server\" are Authed by Default._");

        if (auths.containsKey(myServer.getIdAsString())) {
            if (auths.size(myServer.getIdAsString()) != 0) {
                for (String x : auths.getAll(myServer.getIdAsString())) {
                    usr = Main.API.getUserById(x).join();

                    eb.addField(usr.getName(), usr.getMentionTag(), true);
                }

                chl.sendMessage(eb).thenRun(() -> val.complete(SuccessState.SUCCESSFUL));
            } else {
                eb.addField("No Auths.", "There are no Auths for this Server.", false);

                chl.sendMessage(eb).thenRun(() -> val.complete(SuccessState.UNSUCCESSFUL));
            }
        } else {
            eb.addField("No Auths.", "There are no Auths for this Server.", false);

            chl.sendMessage(eb).thenRun(() -> val.complete(SuccessState.UNSUCCESSFUL));
        }

        return val;
    }
}
