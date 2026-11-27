package loser.storage;

import loser.model.Category;
import loser.model.DailyRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Turns DailyRecords into text lines and back, so the Android app can keep them on the device.
 * One line per day: {@code 2026-11-10|108|Healthy|SOCIAL=48;GAMES=30}
 * (plain Java, so it is tested on a PC).
 */
public final class RecordCodec {
    private RecordCodec() { }

    public static String encode(DailyRecord record) {
        StringBuilder cats = new StringBuilder();
        for (Map.Entry<Category, Integer> e : record.getCategoryTotals().entrySet()) {
            if (cats.length() > 0) cats.append(';');
            cats.append(e.getKey().name()).append('=').append(e.getValue());
        }
        return record.getDate() + "|" + record.getTotalMinutes() + "|"
                + record.getPetStateName().replace('|', ' ') + "|" + cats;
    }

    /** @throws IllegalArgumentException if the line is damaged */
    public static DailyRecord decode(String line) {
        if (line == null) throw new IllegalArgumentException("Line cannot be null.");
        String[] parts = line.split("\\|", -1);
        if (parts.length != 4) throw new IllegalArgumentException("Wrong number of fields: " + line);
        try {
            LocalDate date = LocalDate.parse(parts[0]);
            int total = Integer.parseInt(parts[1]);
            Map<Category, Integer> cats = new EnumMap<Category, Integer>(Category.class);
            if (!parts[3].isEmpty()) {
                for (String pair : parts[3].split(";")) {
                    String[] kv = pair.split("=");
                    cats.put(Category.valueOf(kv[0]), Integer.parseInt(kv[1]));
                }
            }
            return new DailyRecord(date, total, cats, parts[2]);
        } catch (RuntimeException e) {          // bad date, number, category name ...
            throw new IllegalArgumentException("Damaged record: " + line, e);
        }
    }

    public static String encodeAll(List<DailyRecord> records) {
        StringBuilder sb = new StringBuilder();
        for (DailyRecord r : records) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(encode(r));
        }
        return sb.toString();
    }

    /** Damaged lines are skipped, so one bad line never loses the whole week. */
    public static List<DailyRecord> decodeAll(String text) {
        List<DailyRecord> list = new ArrayList<DailyRecord>();
        if (text == null || text.trim().isEmpty()) return list;
        for (String line : text.split("\n")) {
            if (line.trim().isEmpty()) continue;
            try {
                list.add(decode(line));
            } catch (IllegalArgumentException e) {
                // skip this line
            }
        }
        return list;
    }
}
