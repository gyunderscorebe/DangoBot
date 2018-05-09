package de.kaleidox.util;

import de.kaleidox.dangobot.Main;
import de.kaleidox.util.serializer.SelectedPropertiesMapper;
import org.javacord.api.entity.server.Server;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ServerPreferences {
    private static final ConcurrentHashMap<Long, ServerPreferences> selfMap = new ConcurrentHashMap<>();
    public SelectedPropertiesMapper settings;
    public ConcurrentHashMap<String, String> variables = new ConcurrentHashMap<String, String>() {{
        Arrays.asList(Variable.values())
                .forEach(variable -> put(variable.name, variable.defaultValue));
    }};
    private Debugger log;
    private Server myServer;
    private Long serverId;

    private ServerPreferences(Server server) {
        this.myServer = server;

        serverId = myServer.getId();
        settings = new SelectedPropertiesMapper(new File("props/serverPreferences.properties"), serverId);

        Arrays.asList(Variable.values())
                .forEach(variable -> {
                    variables.put(variable.name, settings.softGet(variable.position, variable.defaultValue));
                });

        log = new Debugger(ServerPreferences.class.getName(), server.getName());

        settings.write();

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

    public SuccessState set(Variable variable, Object value) {
        if (variable.accepts(value.toString())) {
            settings.set(variable.position, variable.convert(value.toString()));
            settings.write();

            return SuccessState.SUCCESSFUL;
        } else {
            return SuccessState.ERRORED
                    .withMessage("This variable does not accept this value: `" + value.toString() + "`\n" +
                            "It only accepts values matching the following RegEx:\n" +
                            "```regex\n" +
                            "" + variable.accepts + "\n" +
                            "```");
        }
    }

    public Value get(Variable variable) {
        return new Value(settings.softGet(variable.position, variable.defaultValue), variable.type);
    }

    public void reset(Variable variable) {
        set(variable, variable.defaultValue);

        settings.write();
    }

    public void resetAll() {
        Arrays.asList(Variable.values())
                .forEach(variable -> {
                    set(variable, variable.defaultValue);
                });

        settings.write();
    }

    public enum Variable {
        COMMAND_CHANNEL("command_channel", 0, "none", "((<#)?[0-9]+(>)?){1}", Long.class){
            private boolean accepts(String value) {
                return Main.API.getChannelById(value).isPresent();
            }

            private String convert(String old) {
                return String.valueOf(Utils.extractId(old));
            }
        },
        ADVANCED_LEADERBOARD("advanced_leaderboard", 1, "true", "(true)|(false)", Boolean.class),
        ENABLE_REVOKE_VOTING("enable_revoke_voting", 2, "false", "(true)|(false)", Boolean.class);

        public String name;
        public int position;
        public String defaultValue;
        public String accepts;
        public Class type;

        Variable(String name, int position, String defaultValue, String accepts, Class type) {
            this.name = name;
            this.position = position;
            this.defaultValue = defaultValue;
            this.accepts = accepts;
            this.type = type;
        }

        public static Optional<Variable> getVariable(String name) {
            return Arrays.stream(values())
                    .filter(v -> v.name.equals(name))
                    .findAny();
        }

        private boolean accepts(String value) {
            return value.matches(this.accepts);
        }

        private String convert(String old) {
            return old;
        }
    }
}
