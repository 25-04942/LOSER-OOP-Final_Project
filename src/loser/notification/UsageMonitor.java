package loser.notification;

import loser.core.Pet;
import loser.core.Settings;
import loser.core.TimeText;
import loser.core.UsageTracker;
import loser.model.UsageRecord;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Decides when to alert the user (proposal, Phase 5): when the pet's state changes, when screen
 * time nears or passes the daily limit, and when a single app session runs too long.
 * Each alert is sent only once per day. Plain Java, so it is tested on a PC; the Android app
 * calls check() after every usage update (screen opened, or the background check every 15 minutes).
 */
public class UsageMonitor {
    /** "Nears the limit" = 80% of it. */
    public static final double NEAR_LIMIT_RATIO = 0.8;

    private static final String KEY_NEAR = "near";
    private static final String KEY_LIMIT = "limit";
    private static final String KEY_SESSION = "session:";

    private LocalDate day;
    private String lastState = "Healthy";
    private final Set<String> sent = new LinkedHashSet<String>();

    public synchronized void check(UsageTracker tracker, Settings settings, Pet pet, NotificationService notifier) {
        if (!tracker.getDate().equals(day)) {      // a new day: alerts can be sent again
            day = tracker.getDate();
            sent.clear();
            lastState = "Healthy";
        }

        // 1. pet state changed
        String state = pet.getCurrentState().getStateName();
        if (!state.equals(lastState)) {
            lastState = state;
            notifier.notifyStateChange(pet.getCurrentState());
        }

        // 2. near / over the limit, and 3. a long single session (both are "screen-time warnings")
        if (!settings.isWarningsOn()) return;

        int total = tracker.getTotalScreenTime();
        int limit = settings.getDailyLimit();
        if (total >= limit) {
            sent.add(KEY_NEAR);
            if (sent.add(KEY_LIMIT)) {
                notifier.notifyWarning("You've reached your daily limit of " + TimeText.format(limit) + ". "
                        + pet.getName() + " could use a break.");
            }
        } else if ((double) total / limit >= NEAR_LIMIT_RATIO && sent.add(KEY_NEAR)) {
            notifier.notifyWarning("You've used " + TimeText.format(total) + " of your "
                    + TimeText.format(limit) + " limit. Give your eyes—and " + pet.getName() + "—a rest.");
        }

        int threshold = settings.getLongSessionMinutes();
        for (UsageRecord r : tracker.getRecords()) {
            if (r.getLongestSession() >= threshold && sent.add(KEY_SESSION + r.getAppName())) {
                notifier.notifyLongSession(r, threshold);
            }
        }
    }

    /** Text to keep on the device, so alerts are not repeated after the app restarts. */
    public synchronized String saveState() {
        StringBuilder sb = new StringBuilder();
        sb.append("day=").append(day == null ? "" : day.toString()).append('\n');
        sb.append("state=").append(lastState);
        for (String key : sent) sb.append('\n').append(key.replace('\n', ' '));
        return sb.toString();
    }

    /** Restores saveState() text. Damaged text is ignored. */
    public synchronized void restoreState(String text) {
        if (text == null || text.trim().isEmpty()) return;
        try {
            String[] lines = text.split("\n");
            LocalDate d = lines[0].length() > 4 ? LocalDate.parse(lines[0].substring(4)) : null;
            String s = lines[1].substring(6);
            Set<String> keys = new LinkedHashSet<String>();
            for (int i = 2; i < lines.length; i++) keys.add(lines[i]);
            this.day = d;
            this.lastState = s.isEmpty() ? "Healthy" : s;
            this.sent.clear();
            this.sent.addAll(keys);
        } catch (RuntimeException e) {
            // damaged: start fresh
        }
    }
}
