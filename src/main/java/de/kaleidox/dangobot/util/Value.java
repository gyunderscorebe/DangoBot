package de.kaleidox.dangobot.util;

public class Value {
    private String of;
    private Class type;

    public Value(String of, Class ofType) {
        this.of = of;
        this.type = ofType;
    }

    public String asString() {
        return of;
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

    public float asFloat() {
        if (type == Float.class)
            return Float.parseFloat(of);
        else
            return 0;
    }

    public double asDouble() {
        if (type == Double.class)
            return Double.parseDouble(of);
        else
            return 0;
    }

    @Override
    public String toString() {
        return of;
    }
}