package de.kaleidox.util;


import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public final class Utils {
    private static Debugger log = new Debugger(Utils.class.getName());

    public static <U> U fromNullable(List<U> parts, int index, U valueIfAbsent) {
        if (parts.size() >= index) {
            try {
                return parts.get(index);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return valueIfAbsent;
            }
        }

        return null;
    }

    public static void sleep(Long milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            log.put("Sleep Interrupted.");
        }
    }

    public static Color getRandomColor() {
        int r = 0, g = 0, b = 0;

        int hi = 255, me = 150, lo = 75, ze = 0;

        switch (random(1, 7)) {
            case 1:
            case 6:
            case 7:
                r = random(me, hi);
                g = random(ze, lo);
                b = random(ze, lo);
                break;
            case 2:
            case 5:
                r = random(ze, lo);
                g = random(me, hi);
                b = random(ze, lo);
                break;
            case 3:
            case 4:
                r = random(ze, lo);
                g = random(ze, lo);
                b = random(me, hi);
                break;
        }

        return new Color(r, g, b);
    }

    public static int random(int from, int to) {
        return ThreadLocalRandom.current().nextInt(from, to);
    }

    public static double random(double from, double to) {
        return ThreadLocalRandom.current().nextDouble(from, to);
    }

    public static boolean isNumeric(String e) {
        return e.matches("[0-9]+");
    }

    public static <T> List<List<T>> everyOfList(int every, List<T> of) {
        ArrayList<List<T>> val = new ArrayList<>();
        ArrayList<T> count = new ArrayList<>();
        int i = 0, run = 0;

        while (run != of.size()) {
            if (i == every)
                i = 0;

            if (i == 0) {
                count = new ArrayList<>();
                val.add(count);
            }

            count.add(of.get(run));

            i++;
            run++;
        }

        return val;
    }

    private static <A, B, X, Y> HashMap<X, Y> reformat(Map<A, B> map, Function<A, X> keyFormatter, Function<B, Y> valueFormatter) {
        HashMap<X, Y> val = new HashMap<>();

        map.forEach((key, value) -> {
            val.put(
                    keyFormatter.apply(key),
                    valueFormatter.apply(value)
            );
        });

        return val;
    }

    public static <A, X> ArrayList<X> reformat(List<A> list, Function<A, X> formatter) {
        ArrayList<X> val = new ArrayList<>();

        list.forEach(e -> val.add(formatter.apply(e)));

        return val;
    }

    public static <T> ArrayList<T> reverseList(List<T> list) {
        ArrayList<T> val = new ArrayList<>();

        for (int i = list.size() - 1; i > -1; i--) {
            val.add(list.get(i));
        }

        return val;
    }

    public static <K, V> V safePut(ConcurrentHashMap<K, V> map, K key, V value) {
        if (!map.containsKey(key))
            return map.put(key, value);
        else
            return map.replace(key, value);
    }

    public static int addAllTogether(List<Integer> weekCounts) {
        final int[] val = {0};

        weekCounts.forEach(i -> {
            val[0] = val[0] + i;
        });

        return val[0];
    }

    public static long extractId(Object from) {
        String o = from.toString();

        if (o.matches("[0-9]+"))
            return Long.parseLong(o);
        else {
            // TODO implement a better way

            return Long.parseLong(o.substring(o.indexOf("#")+1, o.indexOf(">")-1));
        }
    }
}
