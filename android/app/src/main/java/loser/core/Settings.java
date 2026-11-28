package loser.core;

import loser.exception.InvalidSettingsException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** User settings. Setters validate input and throw InvalidSettingsException. */
public class Settings {
    public static final int MIN_LIMIT = 1;        // minutes: the proposal says the limit must be greater than zero
    public static final int MAX_LIMIT = 1440;     // 24 hours
    public static final int DEFAULT_LIMIT = 180;  // 3 hours
    public static final int DEFAULT_LONG_SESSION = 60;   // minutes in one app before a warning
    public static final int MAX_THRESHOLD_PERCENT = 300;

    private int dailyLimit;
    private LocalTime resetTime;
    private boolean notificationsOn;   // "Pet condition updates"
    private boolean warningsOn;        // "Screen-time warnings" (near the limit, at the limit, long session)
    private int longSessionMinutes = DEFAULT_LONG_SESSION;
    // Pet state levels as a percent of the daily limit: Tired from 50%, Sick from 80%, Devastated over 100%.
    private int tiredPercent = 50;
    private int sickPercent = 80;
    private int devastatedPercent = 100;

    public Settings() {
        this.dailyLimit = DEFAULT_LIMIT;
        this.resetTime = LocalTime.MIDNIGHT;
        this.notificationsOn = true;
        this.warningsOn = true;
    }

    public Settings(int dailyLimit, LocalTime resetTime, boolean notificationsOn)
            throws InvalidSettingsException {
        setDailyLimit(dailyLimit);
        setResetTime(resetTime);
        this.notificationsOn = notificationsOn;
        this.warningsOn = true;
    }

    public int getDailyLimit()          { return dailyLimit; }
    public LocalTime getResetTime()     { return resetTime; }
    public boolean isNotificationsOn()  { return notificationsOn; }
    public boolean isWarningsOn()       { return warningsOn; }
    public int getLongSessionMinutes()  { return longSessionMinutes; }
    public int getTiredPercent()        { return tiredPercent; }
    public int getSickPercent()         { return sickPercent; }
    public int getDevastatedPercent()   { return devastatedPercent; }
    public double getTiredRatio()       { return tiredPercent / 100.0; }
    public double getSickRatio()        { return sickPercent / 100.0; }
    public double getDevastatedRatio()  { return devastatedPercent / 100.0; }

    public final void setDailyLimit(int dailyLimit) throws InvalidSettingsException {
        if (dailyLimit < MIN_LIMIT || dailyLimit > MAX_LIMIT) {
            throw new InvalidSettingsException(
                "Daily limit must be between " + MIN_LIMIT + " and " + MAX_LIMIT + " minutes.");
        }
        this.dailyLimit = dailyLimit;
    }

    public final void setResetTime(LocalTime resetTime) throws InvalidSettingsException {
        if (resetTime == null) {
            throw new InvalidSettingsException("Reset time cannot be empty.");
        }
        this.resetTime = resetTime;
    }

    public void setNotificationsOn(boolean notificationsOn) {
        this.notificationsOn = notificationsOn;
    }

    public void setWarningsOn(boolean warningsOn) {
        this.warningsOn = warningsOn;
    }

    public final void setLongSessionMinutes(int minutes) throws InvalidSettingsException {
        if (minutes < 1 || minutes > MAX_LIMIT) {
            throw new InvalidSettingsException(
                "Long session must be between 1 and " + MAX_LIMIT + " minutes.");
        }
        this.longSessionMinutes = minutes;
    }

    /** The levels must go up (Tired < Sick < Devastated) and stay within 1% to 300%. */
    public final void setThresholds(int tired, int sick, int devastated) throws InvalidSettingsException {
        if (tired < 1 || devastated > MAX_THRESHOLD_PERCENT || !(tired < sick && sick < devastated)) {
            throw new InvalidSettingsException("Levels must go up: Tired < Sick < Devastated, between 1% and "
                + MAX_THRESHOLD_PERCENT + "%.");
        }
        this.tiredPercent = tired;
        this.sickPercent = sick;
        this.devastatedPercent = devastated;
    }

    /**
     * The "day" a moment belongs to. A day starts at the reset time, so with a reset time of
     * 03:00 the moment 01:30 still belongs to the previous calendar day.
     */
    public LocalDate dayOf(LocalDateTime moment) {
        if (moment == null) throw new IllegalArgumentException("Moment cannot be null.");
        return moment.toLocalTime().isBefore(resetTime) ? moment.toLocalDate().minusDays(1) : moment.toLocalDate();
    }

    /** Re-checks every field. Call after loading saved settings. */
    public void validate() throws InvalidSettingsException {
        setDailyLimit(dailyLimit);
        setResetTime(resetTime);
    }

    @Override
    public String toString() {
        return "Daily limit: " + dailyLimit + " min | Reset time: " + resetTime
                + " | Pet updates: " + (notificationsOn ? "ON" : "OFF")
                + " | Warnings: " + (warningsOn ? "ON" : "OFF")
                + " | Long session: " + longSessionMinutes + " min"
                + " | Levels: " + tiredPercent + "/" + sickPercent + "/" + devastatedPercent + "%";
    }
}
