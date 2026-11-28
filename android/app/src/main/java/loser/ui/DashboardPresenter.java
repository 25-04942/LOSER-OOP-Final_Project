package loser.ui;

import loser.core.Pet;
import loser.core.Settings;
import loser.core.UsageTracker;
import loser.state.PetState;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Turns the app's objects into the texts shown on the pet dashboard. */
public final class DashboardPresenter {
    private static final String BULLET = "\u2022";
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    private DashboardPresenter() { }

    public static DashboardModel present(Pet pet, UsageTracker tracker, Settings settings) {
        PetState state = pet.getCurrentState();
        int total = tracker.getTotalScreenTime();
        int limit = settings.getDailyLimit();
        int remaining = limit - total;

        String limitLine = "of your " + formatMinutes(limit) + " daily limit " + BULLET + " "
                + describeRemaining(remaining);
        String title = state.getStateName() + ": " + describeRemaining(remaining);
        // Past the limit, the state's own over-limit wording is shown.
        String body = remaining < 0 ? state.getOverLimitText() : state.getStatusText();
        String reset = pet.getName() + " resets at " + describeClock(settings.getResetTime())
                + ". Your daily usage is saved in your weekly history.";

        String key = state.getStateName().toLowerCase(Locale.ROOT);
        return new DashboardModel(pet.getName(), state.getStateName(), state.getMessage(),
                formatMinutes(total), limitLine, title, body, reset,
                state.getSpriteName(), "room_backdrop_" + key, "ic_condition_" + key);
    }

    /** 108 -> "1h 48m", 180 -> "3h", 45 -> "45m", 0 -> "0m". */
    public static String formatMinutes(int minutes) {
        return loser.core.TimeText.format(minutes);
    }

    private static String describeRemaining(int remaining) {
        if (remaining > 0) return formatMinutes(remaining) + " left";
        if (remaining == 0) return "limit reached";
        return formatMinutes(-remaining) + " over";
    }

    private static String describeClock(LocalTime t) {
        return t.equals(LocalTime.MIDNIGHT) ? "midnight" : clockText(t);
    }

    /** 15:30 -> "3:30 PM"; 00:00 -> "12:00 AM". */
    public static String clockText(LocalTime t) {
        return t.format(CLOCK);
    }

    /** Same as describeClock but public: "midnight" or "6:00 AM". */
    public static String resetText(LocalTime t) {
        return describeClock(t);
    }
}
