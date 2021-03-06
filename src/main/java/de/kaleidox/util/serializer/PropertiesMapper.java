package de.kaleidox.util.serializer;

import de.kaleidox.util.CustomCollectors;
import de.kaleidox.util.Debugger;
import de.kaleidox.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PropertiesMapper {
    protected ConcurrentHashMap<String, String> map;
    protected ConcurrentHashMap<String, List<String>> values = new ConcurrentHashMap<>();
    protected IOPort<ConcurrentHashMap<String, String>, Map<String, String>> ioPort;
    private Character splitWith;
    private Debugger log = new Debugger(PropertiesMapper.class.getName());

    public PropertiesMapper(IOPort<ConcurrentHashMap<String, String>, Map<String, String>> ioPort) {
        this(ioPort, ';');
    }

    public PropertiesMapper(File file) {
        this(IOPort.mapPort(file), ';');
    }

    public PropertiesMapper(String filePath) {
        this(IOPort.mapPort(new File(filePath)), ';');
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
            Utils.safePut(values, entry.getKey(),
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

    public String softGet(Object key, int index, Object valueIfAbsent) {
        if (values.containsKey(key.toString())) {
            if (values.get(key.toString()).size() > index)
                return values.get(key.toString()).get(index);
            else {
                values.get(key.toString()).add(index, valueIfAbsent.toString());
                return valueIfAbsent.toString();
            }
        } else {
            values.put(key.toString(), new ArrayList<>());
            values.get(key.toString()).add(index, valueIfAbsent.toString());
            return valueIfAbsent.toString();
        }
    }

    public String set(Object key, int index, Object item) {
        values.get(key.toString()).set(index, item.toString());
        return item.toString();
    }

    public ArrayList<String> set(Object key, ArrayList<String> newValues) {
        if (values.containsKey(key.toString()))
            values.replace(key.toString(), newValues);
        else
            values.put(key.toString(), newValues);

        return newValues;
    }

    public List<String> getAll(Object fromKey) {
        List<String> val = null;

        if (values.containsKey(fromKey)) {
            val = values.get(fromKey.toString());
        } else {
            val = new ArrayList<>();
            values.put(fromKey.toString(), val);
        }

        return val;
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

    public void addAll(Object key, ArrayList<Object> values) {
        values.forEach(v -> {
            String t = v.toString();
            this.values.get(key.toString()).add(t);
        });
    }

    public boolean containsValue(Object key, Object value) {
        if (values == null) {
            return false;
        } else {
            if (values.containsKey(key.toString())) {
                return values.get(key.toString()).contains(value.toString());
            } else {
                return false;
            }
        }
    }

    public PropertiesMapper removeValue(Object fromKey, Object value) {
        if (values.containsKey(fromKey.toString())) {
            List<String> val = values.get(fromKey.toString())
                    .stream()
                    .filter(e -> !e.equals(value.toString()))
                    .collect(Collectors.toList());

            values.put(fromKey.toString(), val);

            if (map.get(fromKey.toString()).isEmpty())
                map.remove(fromKey.toString());

            write();
        }

        return this;
    }

    public void removeKey(Object key) {
        values.remove(key.toString());
    }

    public int mapSize() {
        return values.size();
    }

    public Set<Map.Entry<String, List<String>>> entrySet() {
        return values.entrySet();
    }

    // Write to Map, end part

    public void write() {
        values.forEach((key, value) ->
                map.put(key, value
                        .stream()
                        .collect(
                                CustomCollectors.toConcatenatedString(splitWith)
                        )
                )
        );

        ioPort.write(map);
    }

    public ConcurrentHashMap<String, String> getMap() {
        return map;
    }

    public ConcurrentHashMap<String, List<String>> getValues() {
        return values;
    }

    public void clearAll() {
        values.clear();
        map.clear();

        ioPort.write(map);
    }
}
