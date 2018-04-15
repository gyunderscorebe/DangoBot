package de.kaleidox.dangobot.util;

import de.kaleidox.dangobot.Main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Mapper {
    private static Debugger log = new Debugger(Mapper.class.getName());

    public static void packMaps() {
        log.put("Packing Maps...", false);

        Main.MAPS.put("authUsers", Main.authUsersMap);

        log.put("Maps packed.", false);
    }

    public static void saveMaps() {
        Properties props;

        log.put("Saving Maps...", false);

        for (Map.Entry<String, ConcurrentHashMap<String, String>> bigEntry : Main.MAPS.entrySet()) {
            props = new Properties();

            for (Map.Entry<String, String> entry : bigEntry.getValue().entrySet()) {
                props.put(entry.getKey(), entry.getValue());
                log.put("Saved Entry: " + entry.getKey() + "@" + entry.getValue(), true);
            }

            try {
                props.store(new FileOutputStream("props/" + bigEntry.getKey() + ".properties"), null);
                log.put("Stored Entries", true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        log.put("Maps saved.", false);
    }

    public static void loadMaps() {
        Properties props;

        log.put("Loading Maps...", false);

        for (Map.Entry<String, ConcurrentHashMap<String, String>> bigEntry : Main.MAPS.entrySet()) {
            props = new Properties();

            try {
                props.load(new FileInputStream("props/" + bigEntry.getKey() + ".properties"));
            } catch (IOException e2) {
                e2.printStackTrace();
            }

            for (String key : props.stringPropertyNames()) {
                bigEntry.getValue().put(key, props.get(key).toString());
            }
        }

        log.put("Maps loaded.", false);
    }

    public static ConcurrentHashMap<String, String> getMap(String mapName) {
        return Main.MAPS.get(mapName);
    }

    public static ConcurrentHashMap<String, String> setMap(String mapName, ConcurrentHashMap<String, String> map) {
        Main.MAPS.put(mapName, map);
        saveMaps();
        return map;
    }

    public static <K, V> V safePut(ConcurrentHashMap<K, V> map, K key, V value) {
        if (!map.containsKey(key))
            return map.put(key, value);
        else
            return map.replace(key, value);
    }
}
