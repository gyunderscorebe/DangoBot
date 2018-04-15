package de.kaleidox.mo.util.serializer;

import de.kaleidox.mo.util.CustomCollectors;
import de.kaleidox.mo.util.Debugger;
import de.kaleidox.mo.util.Mapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PropertiesMapper {
    protected ConcurrentHashMap<String, String> map;
    protected ConcurrentHashMap<String, ArrayList<String>> values = new ConcurrentHashMap<>();
    protected IOPort<ConcurrentHashMap<String, String>, Map<String, String>> ioPort;
    private Character splitWith;
    private Debugger log = new Debugger(PropertiesMapper.class.getName());

    public PropertiesMapper(IOPort<ConcurrentHashMap<String, String>, Map<String, String>> ioPort) {
        this(ioPort, ';');
    }

    public PropertiesMapper(File file) {
        this(IOPort.mapPort(file), ';');
    }

    public PropertiesMapper(File file, Character splitWith) {
        this(IOPort.mapPort(file), ';');
    }

    public PropertiesMapper(IOPort<ConcurrentHashMap<String, String>, Map<String, String>> ioPort, Character splitWith) {
        log = new Debugger(PropertiesMapper.class.getName(), ioPort.getFile().getName());

        this.splitWith = splitWith;
        this.ioPort = ioPort;

        this.map = this.ioPort.read();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            Mapper.safePut(values, entry.getKey(),
                    new ArrayList<>(
                            Arrays.asList(
                                    entry.getValue()
                                            .split(
                                                    splitWith.
                                                            toString()
                                            )
                            )
                    )
            );
        }

        log.put(values);
    }

    public SelectedPropertiesMapper select(Object key) {
        return new SelectedPropertiesMapper(ioPort, splitWith, key);
    }

    // Getters

    public Character getSplitWith() {
        return splitWith;
    }

    // Modifiers

    // Unselected

    public PropertiesMapper add(Object toKey, Object add) {
        if (!values.containsKey(toKey.toString())) {
            values.put(toKey.toString(), new ArrayList<>());
        }

        values.get(toKey.toString()).add(add.toString());

        return this;
    }

    public String get(Object fromKey, int index) {
        return values.get(fromKey.toString()).get(index);
    }

    public String softGet(Object key, int index, String valueIfAbsent) {
        if (values.get(key.toString()).size() >= index)
            return values.get(key.toString()).get(index);
        else {
            values.get(key.toString()).add(index, valueIfAbsent);
            return valueIfAbsent;
        }
    }

    public String set(Object key, int index, Object item) {
        values.get(key.toString()).set(index, item.toString());
        return item.toString();
    }

    public ArrayList<String> getAll(Object fromKey) {
        return values.get(fromKey.toString());
    }

    public PropertiesMapper clear(Object key) {
        values.get(key.toString()).clear();

        return this;
    }

    public boolean containsKey(Object key) {
        return values.containsKey(key.toString());
    }

    public int size(Object key) {
        return values.get(key.toString()).size();
    }

    public void remove(Object key, int index) {
        values.get(key.toString()).remove(index);
    }

    public boolean containsValue(Object key, Object value) {
        return map.get(key.toString()).contains(value.toString());
    }

    public PropertiesMapper removeValue(Object fromKey, Object value) {
        map.put(fromKey.toString(), new ArrayList<>(Arrays
                .asList(map.get(fromKey.toString())
                        .split(this.splitWith.toString())))
                .stream()
                .filter(e -> !e.equals(value.toString()))
                .collect(CustomCollectors.toConcatenatedString(this.splitWith))
        );

        if (map.get(fromKey.toString()).isEmpty())
            map.remove(fromKey.toString());

        return this;
    }

    // Write to Map, end part

    public void write(String key) {
        // TODO some weird saving bug
        ioPort.write(map);
    }
}
