package loser.core;

/** Turns minutes into short text. */
public final class TimeText {
    private TimeText() { }

    /** 108 -> "1h 48m", 180 -> "3h", 45 -> "45m", 0 -> "0m". */
    public static String format(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("Minutes cannot be negative.");
        int h = minutes / 60;
        int m = minutes % 60;
        if (h > 0 && m > 0) return h + "h " + m + "m";
        if (h > 0) return h + "h";
        return m + "m";
    }
}
