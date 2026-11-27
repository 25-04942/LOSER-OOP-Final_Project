package loser.core;

import loser.model.DailyRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Keeps the last 7 finished days and computes simple statistics. */
public class WeeklyReport {
    public static final int MAX_DAYS = 7;

    public enum Trend {
        IMPROVING("Improving (less screen time lately)"),
        STABLE("Stable"),
        WORSENING("Worsening (more screen time lately)"),
        NOT_ENOUGH_DATA("Not enough data yet");

        private final String label;
        Trend(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    private final ArrayList<DailyRecord> days = new ArrayList<DailyRecord>();

    /** Adds a day (replacing the same date if present) and drops days older than 7. */
    public void addDay(DailyRecord day) {
        if (day == null) throw new IllegalArgumentException("Day cannot be null.");
        for (int i = days.size() - 1; i >= 0; i--) {
            if (days.get(i).getDate().equals(day.getDate())) days.remove(i);
        }
        days.add(day);
        Collections.sort(days, new Comparator<DailyRecord>() {
            @Override public int compare(DailyRecord a, DailyRecord b) {
                return a.getDate().compareTo(b.getDate());
            }
        });
        while (days.size() > MAX_DAYS) days.remove(0);
    }

    /** The saved days, oldest first (at most 7). */
    public List<DailyRecord> getDays() { return Collections.unmodifiableList(days); }
    public int size()                  { return days.size(); }

    /** True if a record for this date is stored. */
    public boolean hasDay(java.time.LocalDate date) {
        for (DailyRecord d : days) {
            if (d.getDate().equals(date)) return true;
        }
        return false;
    }

    public boolean isEmpty()           { return days.isEmpty(); }

    public double getWeeklyAverage() {
        if (days.isEmpty()) return 0.0;
        int sum = 0;
        for (DailyRecord d : days) sum += d.getTotalMinutes();
        return (double) sum / days.size();
    }

    /** Day with the most screen time, or null if the report is empty. */
    public DailyRecord getHighestDay() {
        DailyRecord highest = null;
        for (DailyRecord d : getDays()) {
            if (highest == null || d.getTotalMinutes() > highest.getTotalMinutes()) highest = d;
        }
        return highest;
    }

    /** Compares the latest day with the average of the days before it (10% tolerance). */
    public Trend getTrend() {
        List<DailyRecord> week = getDays();
        if (week.size() < 2) return Trend.NOT_ENOUGH_DATA;
        int latest = week.get(week.size() - 1).getTotalMinutes();
        int sum = 0;
        for (int i = 0; i < week.size() - 1; i++) sum += week.get(i).getTotalMinutes();
        double previousAvg = (double) sum / (week.size() - 1);

        if (previousAvg == 0) return latest > 0 ? Trend.WORSENING : Trend.STABLE;
        double change = (latest - previousAvg) / previousAvg;
        if (change < -0.10) return Trend.IMPROVING;
        if (change > 0.10) return Trend.WORSENING;
        return Trend.STABLE;
    }

    public String displayWeek() {
        if (days.isEmpty()) return "No data yet. Finish a day to see your weekly report.";
        StringBuilder sb = new StringBuilder("=== Weekly Report ===\n");
        for (DailyRecord d : getDays()) {
            sb.append(String.format("%s  %4d min  %-10s %s%n",
                    d.getDate(), d.getTotalMinutes(), d.getPetStateName(), bar(d.getTotalMinutes())));
        }
        DailyRecord high = getHighestDay();
        sb.append(String.format("Average: %.1f min/day%n", getWeeklyAverage()));
        sb.append("Highest day: ").append(high.getDate()).append(" (")
          .append(high.getTotalMinutes()).append(" min)\n");
        sb.append("Trend: ").append(getTrend());
        return sb.toString();
    }

    /** One '#' per 15 minutes. */
    private String bar(int minutes) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < minutes / 15; i++) b.append('#');
        return b.toString();
    }
}
