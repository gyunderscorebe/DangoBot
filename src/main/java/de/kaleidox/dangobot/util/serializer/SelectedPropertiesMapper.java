package de.kaleidox.dangobot.util.serializer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SelectedPropertiesMapper extends PropertiesMapper {
    private String selected;

    public SelectedPropertiesMapper(IOPort<ConcurrentHashMap<String, String>, Map<String, String>> ioPort, Object selected) {
        super(ioPort);

        this.selected = selected.toString();
    }

    public SelectedPropertiesMapper(File file, Object selected) {
        super(file);

        this.selected = selected.toString();
    }

    public SelectedPropertiesMapper(File file, Character splitWith, Object selected) {
        super(file, splitWith);

        this.selected = selected.toString();
    }

    public SelectedPropertiesMapper(IOPort<ConcurrentHashMap<String, String>, Map<String, String>> ioPort, Character splitWith, Object selected) {
        super(ioPort, splitWith);

        this.selected = selected.toString();
    }


    public PropertiesMapper add(Object item) {
        if (selected.isEmpty())
            throw new NullPointerException("No Key selected.");
        else
            add(selected, item);

        return this;
    }

    public String get(int index) {
        if (selected.isEmpty())
            throw new NullPointerException("No Key selected.");
        else
            return get(selected, index);
    }

    public String softGet(int index, Object valueIfAbsent) {
        if (selected.isEmpty())
            throw new NullPointerException("No Key selected.");
        else return softGet(selected, index, valueIfAbsent);
    }

    public String set(int index, Object value) {
        if (selected.isEmpty())
            throw new NullPointerException("No Key selected.");
        else
            return set(selected, index, value);
    }

    public void addAll(ArrayList<Object> values) {
        add(selected, values);
    }

    public ArrayList<String> set(ArrayList<String> newValues) {
        return super.set(selected, newValues);
    }

    public List<String> getAll() {
        if (selected.isEmpty())
            throw new NullPointerException("No Key selected.");
        else
            return getAll(selected);
    }

    public PropertiesMapper clear() {
        if (selected.isEmpty())
            throw new NullPointerException("No Key selected.");
        else
            return clear(selected);
    }

    public int size() {
        if (selected.isEmpty())
            throw new NullPointerException("No Key selected.");
        else
            return size(selected);
    }

    public void remove(int index) {
        if (selected.isEmpty())
            throw new NullPointerException("No Key selected.");
        else
            remove(selected, index);
    }

    public boolean containsValue(Object value) {
        if (selected == null)
            throw new NullPointerException("No Key selected.");
        else {
            try {
                return map.get(selected).contains(value.toString());
            } catch (NullPointerException e) {
                return false;
            }
        }
    }

    public PropertiesMapper removeValue(Object value) {
        if (selected == null)
            throw new NullPointerException("No Key selected.");
        else
            return removeValue(selected, value);
    }

    public void write() {
        super.write();
    }
}
