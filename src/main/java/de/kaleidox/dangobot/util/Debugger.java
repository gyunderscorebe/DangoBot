package de.kaleidox.dangobot.util;

import de.kaleidox.dangobot.DangoBot;
import org.javacord.api.entity.message.Message;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Debugger {
    private static Debugger log = new Debugger("Debugger");
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private String title;
    private String subclass;
    private boolean isSubclass = false;
    private StringBuilder sb = new StringBuilder();

    public Debugger(String title) {
        this.title = title;
    }

    public Debugger(String title, String subclass) {
        this.title = title;
        this.subclass = subclass;
        isSubclass = true;
    }

    public static void print(Object d) {
        System.out.println(d);
    }

    public void speak() {
        this.put("### --- DEBUGGING --- ###", true);
    }

    public Boolean put(Object t, boolean isDebug) {
        return put(t.toString(), isDebug);
    }

    public Boolean put(Object t) {
        try {
            return put(t.toString());
        } catch (NullPointerException e) {
            return put("Tried to output NULL with cause: " + e.getCause());
        }
    }

    public Boolean put(String method, String message, boolean isDebug) {
        if (isDebug) {
            if (DangoBot.isTesting)
                return put("[" + method + "] [Debug] " + message);
            else
                return false;
        } else {
            return put("[" + method + "] " + message);
        }
    }

    public Boolean put(String message, boolean isDebug) {
        if (isDebug) {
            if (DangoBot.isTesting)
                return put("[Debug] " + message);
            else
                return false;
        } else {
            return put(message);
        }
    }

    public Boolean put(String message) {
        clear();
        sb.append("[");
        putTime();
        sb.append("|");
        sb.append(title);
        sb.append(isSubclass ? ":" + subclass : "");
        sb.append("] ");
        sb.append(message);

        return send();
    }

    // Private Methods

    public Boolean print(Message msg) {
        StringBuilder info = new StringBuilder();

        info.append("");

        return false;
    }

    private Boolean send() {
        Boolean give;

        try {
            System.out.println(sb.toString());
        } catch (NullPointerException e) {
            give = false;
        } finally {
            give = true;
        }

        return give;
    }

    private void clear() {
        sb.delete(0, sb.length());
    }

    private String getTime() {
        return sdf.format(new Timestamp(System.currentTimeMillis()));
    }

    private void putTime() {
        sb.append(getTime());
    }
}
