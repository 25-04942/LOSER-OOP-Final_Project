package loser.ui;

import loser.core.Pet;
import loser.core.Settings;

import java.time.LocalTime;

/**
 * What the user typed on the Limits & alerts (or setup) screen, still as text.
 * Immutable: the with...() methods return a changed copy.
 */
public final class SettingsForm {
    private final String hours;
    private final String minutes;
    private final String petName;
    private final LocalTime resetTime;
    private final String longSession;
    private final String tired;
    private final String sick;
    private final String devastated;
    private final boolean petUpdatesOn;
    private final boolean warningsOn;

    public SettingsForm(String hours, String minutes, String petName, LocalTime resetTime, String longSession,
                        String tired, String sick, String devastated, boolean petUpdatesOn, boolean warningsOn) {
        this.hours = hours;
        this.minutes = minutes;
        this.petName = petName;
        this.resetTime = resetTime;
        this.longSession = longSession;
        this.tired = tired;
        this.sick = sick;
        this.devastated = devastated;
        this.petUpdatesOn = petUpdatesOn;
        this.warningsOn = warningsOn;
    }

    /** A form holding the values that are saved right now. */
    public static SettingsForm current(Settings s, Pet pet) {
        return new SettingsForm(String.valueOf(s.getDailyLimit() / 60), String.valueOf(s.getDailyLimit() % 60),
                pet.getName(), s.getResetTime(), String.valueOf(s.getLongSessionMinutes()),
                String.valueOf(s.getTiredPercent()), String.valueOf(s.getSickPercent()),
                String.valueOf(s.getDevastatedPercent()), s.isNotificationsOn(), s.isWarningsOn());
    }

    public SettingsForm withLimit(String hours, String minutes) {
        return new SettingsForm(hours, minutes, petName, resetTime, longSession, tired, sick, devastated,
                petUpdatesOn, warningsOn);
    }

    public SettingsForm withPetName(String petName) {
        return new SettingsForm(hours, minutes, petName, resetTime, longSession, tired, sick, devastated,
                petUpdatesOn, warningsOn);
    }

    public SettingsForm withResetTime(LocalTime resetTime) {
        return new SettingsForm(hours, minutes, petName, resetTime, longSession, tired, sick, devastated,
                petUpdatesOn, warningsOn);
    }

    public SettingsForm withLongSession(String longSession) {
        return new SettingsForm(hours, minutes, petName, resetTime, longSession, tired, sick, devastated,
                petUpdatesOn, warningsOn);
    }

    public SettingsForm withLevels(String tired, String sick, String devastated) {
        return new SettingsForm(hours, minutes, petName, resetTime, longSession, tired, sick, devastated,
                petUpdatesOn, warningsOn);
    }

    public SettingsForm withSwitches(boolean petUpdatesOn, boolean warningsOn) {
        return new SettingsForm(hours, minutes, petName, resetTime, longSession, tired, sick, devastated,
                petUpdatesOn, warningsOn);
    }

    public String getHours()          { return hours; }
    public String getMinutes()        { return minutes; }
    public String getPetName()        { return petName; }
    public LocalTime getResetTime()   { return resetTime; }
    public String getLongSession()    { return longSession; }
    public String getTired()          { return tired; }
    public String getSick()           { return sick; }
    public String getDevastated()     { return devastated; }
    public boolean isPetUpdatesOn()   { return petUpdatesOn; }
    public boolean isWarningsOn()     { return warningsOn; }
}
