package de.kaleidox.dangobot.util;

import de.kaleidox.dangobot.util.serializer.SelectedPropertiesMapper;
import org.javacord.api.entity.server.Server;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServerPreferences {
    private static final ConcurrentHashMap<Long, ServerPreferences> selfMap = new ConcurrentHashMap<>();
    public SelectedPropertiesMapper settings;
    public ConcurrentHashMap<String, String> variables = new ConcurrentHashMap<String, String>() {{
        put("command_channel", "none");
        put("advanced_leaderboard", "false");
    }};
    private Debugger log;
    private Server myServer;
    private Long serverId;

    private ServerPreferences(Server server) {
        this.myServer = server;

        serverId = myServer.getId();
        settings = new SelectedPropertiesMapper(new File("props/serverPreferences.properties"), serverId);

        variables.put("command_channel", settings.softGet(0, "none"));
        variables.put("advanced_leaderboard", settings.softGet(1, "false"));

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

    public SuccessState setVariable(Variable variable, Object value) {
        if (variable.accepts(value.toString())) {
            settings.set(variable.position, value);
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

    public Value getVariable(Variable variable) {
        return new Value(settings.softGet(variable.position, variable.defaultValue), variable);
    }

    public Set<Map.Entry<String, String>> getVariables() {
        return variables.entrySet();
    }

    public enum Variable {
        COMMAND_CHANNEL("command_channel", 0, "none", "[0-9]+", Long.class),
        ADVANCED_LEADERBOARD("advanced_leaderboard", 1, "false", "(true)|(false)", Boolean.class),
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
    }

    public class Value {
        private String of;
        private Class type;

        Value(String of, Variable ofVariable) {
            this.of = of;
            this.type = ofVariable.type;
        }

        public boolean asBoolean() {
            if (type == Boolean.class)
                return Boolean.valueOf(of);
            else
                return false;
        }

        public int asInteger() {
            if (type == Integer.class)
                return Integer.parseInt(of);
            else
                return 0;
        }

        public long asLong() {
            if (type == Long.class)
                return Long.parseLong(of);
            else
                return 0;
        }
    }
}
