package loser.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Stores one app's usage for one day.
 * Fields are private (encapsulation); values are checked on creation and
 * on every setter so a record can never hold invalid data.
 */
public class UsageRecord {
    private String appName;
    private Category category;
    private int minutesUsed;      // total minutes used today
    private int longestSession;   // longest single session, in minutes
    private LocalDate date;

    public UsageRecord(String appName, Category category, int minutesUsed,
                       int longestSession, LocalDate date) {
        setAppName(appName);
        setCategory(category);
        setMinutesUsed(minutesUsed);
        setLongestSession(longestSession);
        setDate(date);
        checkSessionFitsTotal();
    }

    /** Convenience constructor: record for today. */
    public UsageRecord(String appName, Category category, int minutesUsed, int longestSession) {
        this(appName, category, minutesUsed, longestSession, LocalDate.now());
    }

    // ---------- Getters ----------
    public String getAppName()       { return appName; }
    /** Same as getMinutesUsed(); the name used in the proposal. */
    public int getMinutes()          { return minutesUsed; }
    public Category getCategory()    { return category; }
    public int getMinutesUsed()      { return minutesUsed; }
    public int getLongestSession()   { return longestSession; }
    public LocalDate getDate()       { return date; }

    // ---------- Setters (validated) ----------
    public final void setAppName(String appName) {
        if (appName == null || appName.trim().isEmpty()) {
            throw new IllegalArgumentException("App name cannot be empty.");
        }
        this.appName = appName.trim();
    }

    public final void setCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null.");
        }
        this.category = category;
    }

    public final void setMinutesUsed(int minutesUsed) {
        if (minutesUsed < 0) {
            throw new IllegalArgumentException("Minutes used cannot be negative.");
        }
        this.minutesUsed = minutesUsed;
    }

    public final void setLongestSession(int longestSession) {
        if (longestSession < 0) {
            throw new IllegalArgumentException("Longest session cannot be negative.");
        }
        this.longestSession = longestSession;
    }

    public final void setDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null.");
        }
        this.date = date;
    }

    /** Adds more usage to this record (e.g. when the tracker refreshes). */
    public void addUsage(int extraMinutes, int sessionLength) {
        if (extraMinutes < 0 || sessionLength < 0) {
            throw new IllegalArgumentException("Usage values cannot be negative.");
        }
        this.minutesUsed += extraMinutes;
        if (sessionLength > this.longestSession) {
            this.longestSession = sessionLength;
        }
    }

    private void checkSessionFitsTotal() {
        if (longestSession > minutesUsed) {
            throw new IllegalArgumentException(
                "Longest session (" + longestSession + ") cannot exceed total minutes (" + minutesUsed + ").");
        }
    }

    // ---------- Object methods ----------
    @Override
    public String toString() {
        return String.format("%s [%s] - %d min (longest session: %d min) on %s",
                appName, category, minutesUsed, longestSession, date);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsageRecord)) return false;
        UsageRecord other = (UsageRecord) o;
        return minutesUsed == other.minutesUsed
                && longestSession == other.longestSession
                && appName.equals(other.appName)
                && category == other.category
                && date.equals(other.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, category, minutesUsed, longestSession, date);
    }
}
