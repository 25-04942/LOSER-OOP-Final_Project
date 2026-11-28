package loser.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Summary of one finished day. Stored in the WeeklyReport. */
public class DailyRecord {
    private final LocalDate date;
    private final int totalMinutes;
    private final Map<Category, Integer> categoryTotals;
    private final String petStateName;

    public DailyRecord(LocalDate date, int totalMinutes,
                       Map<Category, Integer> categoryTotals, String petStateName) {
        if (date == null) throw new IllegalArgumentException("Date cannot be null.");
        if (totalMinutes < 0) throw new IllegalArgumentException("Total minutes cannot be negative.");
        if (categoryTotals == null) throw new IllegalArgumentException("Category totals cannot be null.");
        if (petStateName == null || petStateName.trim().isEmpty()) {
            throw new IllegalArgumentException("Pet state name cannot be empty.");
        }
        this.date = date;
        this.totalMinutes = totalMinutes;
        this.categoryTotals = new EnumMap<Category, Integer>(Category.class);
        this.categoryTotals.putAll(categoryTotals);
        this.petStateName = petStateName;
    }

    public LocalDate getDate()          { return date; }
    public int getTotalMinutes()        { return totalMinutes; }
    public String getPetStateName()     { return petStateName; }

    /** Read-only view so outside code cannot change a saved day. */
    public Map<Category, Integer> getCategoryTotals() {
        return Collections.unmodifiableMap(categoryTotals);
    }

    @Override
    public String toString() {
        return date + ": " + totalMinutes + " min, pet was " + petStateName;
    }
}
