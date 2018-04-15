package de.kaleidox.mo.util.serializer;

import de.kaleidox.mo.util.Debugger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class IOPort<T, X> {
    private static Debugger log = new Debugger(IOPort.class.getName());
    private File file;
    private Supplier<T> reader;
    private Consumer<X> writer;

    public IOPort(File file, Supplier<T> reader, Consumer<X> writer) {
        this.file = file;
        this.reader = reader;
        this.writer = writer;
    }

    public File getFile() {
        return file;
    }

    public static IOPort<ConcurrentHashMap<String, String>, Map<String, String>> mapPort(File file) {
        log.put("EXISTS? "+file.exists());
        return new IOPort<>(
                file,
                () -> {
                    Properties props = new Properties();
                    ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

                    try {
                        props.load(new FileInputStream(file.getPath()));
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }

                    props.forEach((key, value) -> map.put(key.toString(), value.toString()));

                    return map;
                },
                item -> {
                    Properties props = new Properties();

                    for (Map.Entry<String, String> entry : item.entrySet()) {
                        props.put(entry.getKey(), entry.getValue());
                    }

                    try {
                        props.store(new FileOutputStream(file.getPath()), null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    public T read() {
        return reader.get();
    }

    public Collection<String> readAsCollection(Collection<String> supplier, Character splitWith) {
        return new ArrayList<>(Arrays.asList(
                reader
                        .get()
                        .toString()
                        .split(splitWith.toString())));
    }

    public void write(X item) {
        writer.accept(item);
    }
}
