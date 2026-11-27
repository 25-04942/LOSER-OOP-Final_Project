package loser.ui;

import loser.core.Pet;
import loser.core.Settings;
import loser.exception.InvalidSettingsException;

import java.time.LocalTime;

/** Content and saving logic of the "Limits & alerts" and setup screens (no Android code, so it is tested on a PC). */
public final class SettingsPresenter {
    /** The warning reminder shown in the preview is the one sent at this share of the limit. */
    public static final double WARNING_RATIO = loser.notification.UsageMonitor.NEAR_LIMIT_RATIO;
    public static final int MAX_PET_NAME = 20;

    private SettingsPresenter() { }

    public static SettingsModel present(Settings settings, Pet pet, boolean notificationsAllowed) {
        int limit = settings.getDailyLimit();
        String name = pet.getName();
        return new SettingsModel(
                two(limit / 60),
                two(limit % 60),
                limitHint(),
                settings.isNotificationsOn(),
                settings.isWarningsOn(),
                notificationsAllowed,
                notificationsAllowed ? "Notifications allowed on this device"
                                     : "Notifications are turned off on this device  \u203a",
                name.toUpperCase(java.util.Locale.ROOT) + "  \u2022  SCREEN TIME",
                "Time for a little break?",
                previewBody(limit, name),
                name,
                DashboardPresenter.clockText(settings.getResetTime()),
                String.valueOf(settings.getLongSessionMinutes()),
                String.valueOf(settings.getTiredPercent()),
                String.valueOf(settings.getSickPercent()),
                String.valueOf(settings.getDevastatedPercent()),
                "A gentler routine for you and " + name,
                name + "\u2019s condition follows your daily usage.\nA fresh day starts automatically at "
                        + DashboardPresenter.resetText(settings.getResetTime()) + ".",
                "Know when " + name + "\u2019s condition changes.");
    }

    /** "Choose 1 minute to 24 hours." (built from the limits in Settings). */
    public static String limitHint() {
        String min = Settings.MIN_LIMIT == 1 ? "1 minute" : Settings.MIN_LIMIT + " minutes";
        return "Choose " + min + " to " + (Settings.MAX_LIMIT / 60) + " hours.";
    }

    /** The reminder text for a given limit, e.g. 180 -> "You've used 2h 24m of your 3h limit." */
    public static String previewBody(int limitMinutes, String petName) {
        int used = (int) Math.round(limitMinutes * WARNING_RATIO);
        return "You\u2019ve used " + DashboardPresenter.formatMinutes(used) + " of your "
                + DashboardPresenter.formatMinutes(limitMinutes) + " limit.\n"
                + "Give your eyes\u2014and " + petName + "\u2014a rest.";
    }

    /**
     * Turns the two text boxes into minutes. Empty means 0.
     *
     * @throws InvalidSettingsException if they are not whole numbers or outside the allowed range
     */
    public static int parseLimit(String hours, String minutes) throws InvalidSettingsException {
        long total;
        try {
            long h = hours == null || hours.trim().isEmpty() ? 0 : Long.parseLong(hours.trim());
            long m = minutes == null || minutes.trim().isEmpty() ? 0 : Long.parseLong(minutes.trim());
            if (h < 0 || m < 0 || m > 59) throw new InvalidSettingsException(limitHint());
            total = h * 60 + m;
        } catch (NumberFormatException e) {
            throw new InvalidSettingsException(limitHint());
        }
        if (total < Settings.MIN_LIMIT || total > Settings.MAX_LIMIT) {
            throw new InvalidSettingsException(limitHint());
        }
        return (int) total;
    }

    /** A whole number from a text box, or an InvalidSettingsException with the given message. */
    private static int parseNumber(String text, String problem) throws InvalidSettingsException {
        try {
            return Integer.parseInt(text == null ? "" : text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidSettingsException(problem);
        }
    }

    /**
     * Saves only the limit and the two switches (kept for the small cases and tests).
     * If the limit is invalid nothing is changed.
     */
    public static void apply(Settings settings, String hours, String minutes,
                             boolean petUpdatesOn, boolean warningsOn) throws InvalidSettingsException {
        settings.setDailyLimit(parseLimit(hours, minutes));
        settings.setNotificationsOn(petUpdatesOn);
        settings.setWarningsOn(warningsOn);
    }

    /**
     * Checks everything in the form first, then saves it all. If anything is wrong an
     * InvalidSettingsException (with a message for the user) is thrown and nothing is changed.
     */
    public static void applyAll(Settings settings, Pet pet, SettingsForm form) throws InvalidSettingsException {
        String name = form.getPetName() == null ? "" : form.getPetName().trim();
        if (name.isEmpty() || name.length() > MAX_PET_NAME) {
            throw new InvalidSettingsException("Give your pet a name (up to " + MAX_PET_NAME + " characters).");
        }
        int limit = parseLimit(form.getHours(), form.getMinutes());
        if (form.getResetTime() == null) throw new InvalidSettingsException("Choose when the day starts.");
        int longSession = parseNumber(form.getLongSession(), "Long session must be a number of minutes.");
        int tired = parseNumber(form.getTired(), "Levels must be whole numbers.");
        int sick = parseNumber(form.getSick(), "Levels must be whole numbers.");
        int devastated = parseNumber(form.getDevastated(), "Levels must be whole numbers.");

        Settings trial = new Settings();           // validate on a copy so a mistake changes nothing
        trial.setDailyLimit(limit);
        trial.setLongSessionMinutes(longSession);
        trial.setThresholds(tired, sick, devastated);

        settings.setDailyLimit(limit);
        settings.setResetTime(form.getResetTime());
        settings.setLongSessionMinutes(longSession);
        settings.setThresholds(tired, sick, devastated);
        settings.setNotificationsOn(form.isPetUpdatesOn());
        settings.setWarningsOn(form.isWarningsOn());
        pet.setName(name);
    }

    private static String two(int value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }
}
