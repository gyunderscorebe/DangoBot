package de.kaleidox.dangobot.util;


import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class Utils {
    private static Debugger log = new Debugger(Utils.class.getName());

    public static <U> U fromNullable(List<U> parts, int index) {
        if (parts.size() >= index) {
            try {
                return parts.get(index);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                // nothing
            }
        }

        return null;
    }

    public static <T> ArrayList<T> arrayToArrayList(T[] oldArray) {
        return new ArrayList<>(Arrays.asList(oldArray));
    }

    public static void evaluateState(Message msg, SuccessState state) {
        switch (state) {
            case SUCCESSFUL:
                msg.addReaction("✅");
                break;
            case UNSUCCESSFUL:
                msg.addReaction("❓");
                break;
            case ERRORED:
                msg.addReaction("❌");
                break;
            case UNAUTHORIZED:
                msg.addReaction("⛔");
                break;
            default:
                break;
        }
    }

    public static void sleep(Long milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            log.put("Sleep Interrupted.");
        }
    }

    public static void addWastebasket(Message msg) {
        if (msg.getAuthor().isYourself() && !msg.getPrivateChannel().isPresent()) {
            msg.addReaction("🗑");
            msg.addReactionAddListener(reaAdd -> {
                Emoji emoji = reaAdd.getEmoji();

                if (!reaAdd.getUser().isBot()) {
                    emoji.asUnicodeEmoji().ifPresent(then -> {
                        if (then.equals("🗑")) {
                            msg.delete();
                        }
                    });
                }
            });
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
}
