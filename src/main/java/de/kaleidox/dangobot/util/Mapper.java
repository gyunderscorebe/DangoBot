package de.kaleidox.dangobot.util;

import de.kaleidox.dangobot.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Mapper {
    private static Debugger log = new Debugger(Mapper.class.getName());

    public static void packMaps() {
        log.put("Packing Maps...", false);

        Main.MAPS.put(new File("props/authUsers.properties"), Main.authUsersMap);

        log.put("Maps packed.", false);
    }

    public static void loadMaps() {
        Properties props;

        log.put("Loading Maps...", false);

        for (Map.Entry<File, ConcurrentHashMap<String, String>> bigEntry : Main.MAPS.entrySet()) {
            props = new Properties();

            try {
                props.load(new FileInputStream(bigEntry.getKey().getPath()));
            } catch (IOException e2) {
                e2.printStackTrace();
            }

            for (String key : props.stringPropertyNames()) {
                bigEntry.getValue().put(key, props.get(key).toString());
            }
        }

        log.put("Maps loaded.", false);
    }

    public static <K, V> V safePut(ConcurrentHashMap<K, V> map, K key, V value) {
        if (!map.containsKey(key))
            return map.put(key, value);
        else
            return map.replace(key, value);
    }
}
