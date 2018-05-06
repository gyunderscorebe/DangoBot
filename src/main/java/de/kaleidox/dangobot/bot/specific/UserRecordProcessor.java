package de.kaleidox.dangobot.bot.specific;

import de.kaleidox.dangobot.util.Debugger;
import de.kaleidox.dangobot.util.ObjectVariableEnum;
import de.kaleidox.dangobot.util.Utils;
import de.kaleidox.dangobot.util.Value;
import de.kaleidox.dangobot.util.serializer.PropertiesMapper;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static de.kaleidox.dangobot.bot.specific.UserRecordProcessor.Variable.AVERAGE_MSG_LENGTH;
import static de.kaleidox.dangobot.bot.specific.UserRecordProcessor.Variable.AVG_MSG_PER_DAY;
import static de.kaleidox.dangobot.bot.specific.UserRecordProcessor.Variable.COUNTED_MSG_TODAY;
import static de.kaleidox.dangobot.bot.specific.UserRecordProcessor.Variable.MSG_COUNTS_LAST_WEEK;

public class UserRecordProcessor {
    private static final ConcurrentHashMap<Server, UserRecordProcessor> selfMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<User, PropertiesMapper> entries = new ConcurrentHashMap<>();
    private Debugger log = new Debugger(UserRecordProcessor.class.getName());
    private Server myServer;
    private Long serverId;
    private PropertiesMapper defaultEntry;

    private UserRecordProcessor(Server server) {
        this.myServer = server;

        serverId = myServer.getId();

        File folder = new File("props/userRecords/" + serverId + "/");
        folder.mkdirs();

        File defaultEntry = new File("props/userRecords/" + serverId + "/default.properties");

        if (!defaultEntry.exists()) {
            try {
                defaultEntry.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.defaultEntry = new PropertiesMapper(defaultEntry, ';');

        Utils.safePut(selfMap, server, this);
    }

    /*
    This Object's "Constructor".
    Looks for an already existing instance of the required Object or creates a new one.

    @returns The adequate Instance of this Object.
     */
    public static UserRecordProcessor softGet(Server server) {
        return (selfMap.containsKey(server) ? selfMap.get(server) : selfMap.put(server, new UserRecordProcessor(server)));
    }

    public static void resetDailies() {
        selfMap.forEach((srv, proc) -> {
            proc.entries.forEach((user, mapper) -> {
                COUNTED_MSG_TODAY.setValueFor(mapper, 0);

                ArrayList<Integer> weekCounts = Utils.reformat(MSG_COUNTS_LAST_WEEK.getValuesFor(mapper), Value::asInteger);
                weekCounts.remove(weekCounts.size() - 1);
                ArrayList<Integer> newWeekCounts = new ArrayList<>();
                newWeekCounts.add(0);
                newWeekCounts.addAll(weekCounts);

                MSG_COUNTS_LAST_WEEK.setValuesFor(mapper, newWeekCounts);
            });
        });
    }

    public boolean decideDango(User usr) {
        int random = Utils.random(0, 100);
        PropertiesMapper userEntry = createOrGetUserEntry(usr);

        return true; // TODO dango decider term
    }

    public void newMessage(MessageCreateEvent event) {
        Message msg = event.getMessage();
        event.getMessage()
                .getUserAuthor()
                .ifPresent(usr -> {
                    PropertiesMapper userEntry = createOrGetUserEntry(usr);
                    Float msgLength = AVERAGE_MSG_LENGTH.getValueFor(userEntry).asFloat();
                    Float dayAverage = AVG_MSG_PER_DAY.getValueFor(userEntry).asFloat();
                    Integer todayCounted = COUNTED_MSG_TODAY.getValueFor(userEntry).asInteger();
                    ArrayList<Integer> weekCounts = Utils.reformat(MSG_COUNTS_LAST_WEEK.getValuesFor(userEntry), Value::asInteger);

                    COUNTED_MSG_TODAY.setValueFor(userEntry, todayCounted + 1);
                    AVERAGE_MSG_LENGTH.setValueFor(userEntry, (msg.getContent().length() / dayAverage));

                    userEntry.write();
                });
    }

    private PropertiesMapper createOrGetUserEntry(User user) {
        if (entries.containsKey(user)) {
            return entries.get(user);
        } else {
            File thisEntry = new File("props/userRecords/" + serverId + "/" + user.getId() + ".properties");

            if (!thisEntry.exists()) {
                try {
                    thisEntry.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return entries.put(user, new PropertiesMapper(thisEntry));
        }
    }

    public enum Variable implements ObjectVariableEnum {
        AVERAGE_MSG_LENGTH("average_msg_length", 0, "0.0F", Float.class),
        AVG_MSG_PER_DAY("average_msg_per_day", 0, "0.0F", Float.class),
        COUNTED_MSG_TODAY("counted_msg_today", 0, "0", Integer.class),
        MSG_COUNTS_LAST_WEEK("msg_counts_last_week", 0, "0", Integer.class);

        public String name;
        public int position;
        public String defaultValue;
        public Class type;

        Variable(String name, int position, String defaultValue, Class type) {
            this.name = name;
            this.position = position;
            this.defaultValue = defaultValue;
            this.type = type;
        }

        public static Optional<UserRecordProcessor.Variable> getVariable(String name) {
            return Arrays.stream(values())
                    .filter(v -> v.name.equals(name))
                    .findAny();
        }

        public Value getValueFor(PropertiesMapper mapper) {
            return new Value(mapper.softGet(this.name, this.position, this.defaultValue), this.type);
        }

        public ArrayList<Value> getValuesFor(PropertiesMapper mapper) {
            ArrayList<Value> values = new ArrayList<>();

            mapper.getAll(this.name)
                    .forEach(v -> values.add(new Value(v, this.type)));

            return values;
        }

        public <T> void setValueFor(PropertiesMapper mapper, T value) {
            setValueFor(mapper, new Value(value.toString(), value.getClass()));
        }

        public <T> void setValuesFor(PropertiesMapper mapper, ArrayList<T> values) {
            mapper.addAll(this.name, Utils.reformat(values, Object::toString));
        }

        public void setValueFor(PropertiesMapper mapper, Value value) {
            mapper.set(this.name, this.position, value.asString());
        }
    }
}
