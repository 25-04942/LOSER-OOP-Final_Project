package loser.core;

import loser.model.Category;
import loser.model.DailyRecord;
import loser.model.UsageRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Holds today's UsageRecords and computes totals and the usage ratio. */
public class UsageTracker {
    private final ArrayList<UsageRecord> records = new ArrayList<UsageRecord>();
    private final Settings settings;
    private LocalDate date;

    public UsageTracker(Settings settings) {
        this(settings, LocalDate.now());
    }

    public UsageTracker(Settings settings, LocalDate date) {
        if (settings == null) throw new IllegalArgumentException("Settings cannot be null.");
        if (date == null) throw new IllegalArgumentException("Date cannot be null.");
        this.settings = settings;
        this.date = date;
    }

    public synchronized LocalDate getDate() { return date; }

    /** Adds usage. If the app is already tracked today, the minutes are merged. */
    public synchronized void addUsage(UsageRecord record) {
        if (record == null) throw new IllegalArgumentException("Record cannot be null.");
        if (!record.getDate().equals(date)) {
            throw new IllegalArgumentException(
                "Record date " + record.getDate() + " does not match tracker date " + date + ".");
        }
        UsageRecord existing = find(record.getAppName());
        if (existing != null) {
            existing.addUsage(record.getMinutesUsed(), record.getLongestSession());
        } else {
            // store a copy so outside code cannot change our data
            records.add(new UsageRecord(record.getAppName(), record.getCategory(),
                    record.getMinutesUsed(), record.getLongestSession(), record.getDate()));
        }
    }

    /** Replaces today's data with fresh data (used after reading from the device). */
    public synchronized void replaceAll(List<UsageRecord> fresh) {
        records.clear();
        for (UsageRecord r : fresh) addUsage(r);
    }

    private UsageRecord find(String appName) {
        for (UsageRecord r : records) {
            if (r.getAppName().equalsIgnoreCase(appName.trim())) return r;
        }
        return null;
    }

    public synchronized int getTotalScreenTime() {
        int total = 0;
        for (UsageRecord r : records) total += r.getMinutesUsed();
        return total;
    }

    /** Minutes per category. Every category is present (0 if unused). */
    public synchronized Map<Category, Integer> getTotalByCategory() {
        Map<Category, Integer> totals = new EnumMap<Category, Integer>(Category.class);
        for (Category c : Category.values()) totals.put(c, 0);
        for (UsageRecord r : records) {
            totals.put(r.getCategory(), totals.get(r.getCategory()) + r.getMinutesUsed());
        }
        return totals;
    }

    /** Screen time divided by the daily limit (1.0 = exactly at the limit). */
    public synchronized double getRatio() {
        return (double) getTotalScreenTime() / settings.getDailyLimit();
    }

    public synchronized UsageRecord getMostUsedApp() {
        UsageRecord top = null;
        for (UsageRecord r : records) {
            if (top == null || r.getMinutesUsed() > top.getMinutesUsed()) top = r;
        }
        return top;
    }

    public synchronized int getLongestSession() {
        int longest = 0;
        for (UsageRecord r : records) longest = Math.max(longest, r.getLongestSession());
        return longest;
    }

    public synchronized List<UsageRecord> getRecords() {
        return Collections.unmodifiableList(new ArrayList<UsageRecord>(records));   // a copy: safe to loop over
    }

    public synchronized boolean isEmpty() { return records.isEmpty(); }

    /** Snapshot of today, ready to be stored in the weekly report. */
    public synchronized DailyRecord createDailyRecord(String petStateName) {
        return new DailyRecord(date, getTotalScreenTime(), getTotalByCategory(), petStateName);
    }

    /** Starts a new day: clears all records. */
    public synchronized void reset(LocalDate newDate) {
        if (newDate == null) throw new IllegalArgumentException("Date cannot be null.");
        records.clear();
        this.date = newDate;
    }
}
