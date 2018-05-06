package de.kaleidox.dangobot.bot.specific;

import de.kaleidox.dangobot.DangoBot;
import de.kaleidox.dangobot.util.CustomCollectors;
import de.kaleidox.dangobot.util.Debugger;
import de.kaleidox.dangobot.util.ObjectVariableEnum;
import de.kaleidox.dangobot.util.Utils;
import de.kaleidox.dangobot.util.Value;
import de.kaleidox.dangobot.util.serializer.PropertiesMapper;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
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
import static de.kaleidox.dangobot.bot.specific.UserRecordProcessor.Variable.TOTAL_MSG_LENGTH;

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

        this.defaultEntry.add("average_msg_length", "0.0");
        this.defaultEntry.add("average_msg_per_day", "0.0");
        this.defaultEntry.add("counted_msg_today", "0");
        this.defaultEntry.add("total_msg_length", "0");

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
                MSG_COUNTS_LAST_WEEK.addValueAtBeginning(mapper, 0);
                AVG_MSG_PER_DAY.setValueFor(mapper, 0);
                AVERAGE_MSG_LENGTH.setValueFor(mapper, 0);
                mapper.write();
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
        int thisLength = msg.getContent().length();

        event.getMessage()
                .getUserAuthor()
                .ifPresent(usr -> {
                    PropertiesMapper userEntry = createOrGetUserEntry(usr);
                    Double msgLength = AVERAGE_MSG_LENGTH.getValueFor(userEntry).asDouble();
                    Double dayAverage = AVG_MSG_PER_DAY.getValueFor(userEntry).asDouble();
                    Integer todayCounted = COUNTED_MSG_TODAY.getValueFor(userEntry).asInteger();
                    Integer totalMsgLength = TOTAL_MSG_LENGTH.getValueFor(userEntry).asInteger();
                    ArrayList<Integer> weekCounts = Utils.reformat(MSG_COUNTS_LAST_WEEK.getValuesFor(userEntry), Value::asInteger);

                    int newTotalMsgLength = totalMsgLength + thisLength;
                    Double newLenAvg = (double) newTotalMsgLength / (double) todayCounted;
                    Double newDayAvg = (double) weekCounts.stream()
                            .collect(CustomCollectors.addition()) / (double) 7;

                    TOTAL_MSG_LENGTH.setValueFor(userEntry, newTotalMsgLength);
                    AVG_MSG_PER_DAY.setValueFor(userEntry, newDayAvg);
                    AVERAGE_MSG_LENGTH.setValueFor(userEntry, newLenAvg);
                    COUNTED_MSG_TODAY.setValueFor(userEntry, todayCounted + 1);
                    MSG_COUNTS_LAST_WEEK.changeValue(userEntry, 0, Utils.fromNullable(weekCounts, 0, 0) + 1);

                    userEntry.write();
                });
    }

    public void sendInformation(ServerTextChannel stc, User user, User requestedBy) {
        EmbedBuilder basicEmbed = DangoBot.getBasicEmbed(this.myServer, requestedBy);
        PropertiesMapper userEntry = createOrGetUserEntry(user);

        Double msgLength = AVERAGE_MSG_LENGTH.getValueFor(userEntry).asDouble();
        Double dayAverage = AVG_MSG_PER_DAY.getValueFor(userEntry).asDouble();
        Integer todayCounted = COUNTED_MSG_TODAY.getValueFor(userEntry).asInteger();
        Integer totalMsgLength = TOTAL_MSG_LENGTH.getValueFor(userEntry).asInteger();
        ArrayList<Integer> weekCounts = Utils.reformat(MSG_COUNTS_LAST_WEEK.getValuesFor(userEntry), Value::asInteger);

        basicEmbed
                .addField(AVERAGE_MSG_LENGTH.name, "```" + msgLength + "```")
                .addField(AVG_MSG_PER_DAY.name, "```" + dayAverage + "```")
                .addField(COUNTED_MSG_TODAY.name, "```" + todayCounted + "```")
                .addField(TOTAL_MSG_LENGTH.name, "```" + totalMsgLength + "```");

        for (int i = 0; i < weekCounts.size(); i++) {
            basicEmbed
                    .addInlineField(MSG_COUNTS_LAST_WEEK.name + " before " + i + " Days:", "```" + weekCounts.get(i) + "```");
        }

        stc.sendMessage(basicEmbed);
    }

    private PropertiesMapper createOrGetUserEntry(User user) {
        PropertiesMapper val = null;

        if (entries.containsKey(user)) {
            val = entries.getOrDefault(user, null);
        }

        if (val == null) {
            File thisEntry = new File("props/userRecords/" + serverId + "/" + user.getId() + ".properties");

            if (!thisEntry.exists()) {
                try {
                    thisEntry.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            val = new PropertiesMapper(thisEntry);
            entries.put(user, val);

            val.add("average_msg_length", "0.0");
            val.add("average_msg_per_day", "0.0");
            val.add("counted_msg_today", "0");
            val.add("total_msg_length", "0");
            val.add("msg_counts_last_week", "0");
            val.add("msg_counts_last_week", "0");
            val.add("msg_counts_last_week", "0");
            val.add("msg_counts_last_week", "0");
            val.add("msg_counts_last_week", "0");
            val.add("msg_counts_last_week", "0");
            val.add("msg_counts_last_week", "0");
            val.write();
        }

        return val;
    }

    public enum Variable implements ObjectVariableEnum {
        AVERAGE_MSG_LENGTH("average_msg_length", 0, "0.0", Double.class),
        AVG_MSG_PER_DAY("average_msg_per_day", 0, "0.0", Double.class),
        COUNTED_MSG_TODAY("counted_msg_today", 0, "0", Integer.class),
        MSG_COUNTS_LAST_WEEK("msg_counts_last_week", 0, "0", Integer.class) {
            public void addValueAtBeginning(PropertiesMapper mapper, int value) {
                ArrayList<Integer> weekCounts = Utils.reformat(MSG_COUNTS_LAST_WEEK.getValuesFor(mapper), Value::asInteger);
                weekCounts.remove(weekCounts.size() - 1);
                ArrayList<Integer> newWeekCounts = new ArrayList<>();
                newWeekCounts.add(value);
                newWeekCounts.addAll(weekCounts);

                MSG_COUNTS_LAST_WEEK.setValuesFor(mapper, newWeekCounts);
                mapper.write();
            }

            public void changeValue(PropertiesMapper mapper, int index, int value) {
                ArrayList<Integer> weekCounts = Utils.reformat(MSG_COUNTS_LAST_WEEK.getValuesFor(mapper), Value::asInteger);
                ArrayList<Integer> newWeekCounts = new ArrayList<>();

                for (int i = 0; i < weekCounts.size(); i++) {
                    if (i == index) {
                        newWeekCounts.add(value);
                    } else {
                        newWeekCounts.add(weekCounts.get(i));
                    }
                }

                MSG_COUNTS_LAST_WEEK.setValuesFor(mapper, newWeekCounts);
            }
        },
        TOTAL_MSG_LENGTH("total_msg_length", 0, "0", Integer.class);

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
            ArrayList<Value> reformat = Utils.reformat(mapper.getAll(this.name), t -> new Value(t, this.type));
            return reformat;
        }

        public <T> void setValueFor(PropertiesMapper mapper, T value) {
            setValueFor(mapper, new Value(value.toString(), value.getClass()));
        }

        public <T> void setValuesFor(PropertiesMapper mapper, ArrayList<T> values) {
            mapper.clear(this.name);
            mapper.addAll(this.name, Utils.reformat(values, Object::toString));
        }

        public void setValueFor(PropertiesMapper mapper, Value value) {
            mapper.set(this.name, this.position, value.asString());
        }

        public void addValueAtBeginning(PropertiesMapper mapper, int value) {
            throw new AbstractMethodError();
        }

        public void changeValue(PropertiesMapper mapper, int index, int value) {
            throw new AbstractMethodError();
        }
    }
}
