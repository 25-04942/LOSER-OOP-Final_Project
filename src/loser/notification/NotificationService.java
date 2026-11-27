package loser.notification;

import loser.core.Settings;
import loser.model.UsageRecord;
import loser.state.PetState;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sends notifications (when enabled in Settings) and keeps a history.
 * The Android app extends this class and overrides deliver() to show a real notification.
 */
public class NotificationService implements Notifiable {
    private final Settings settings;
    private final ArrayList<String> history = new ArrayList<String>();

    public NotificationService(Settings settings) {
        if (settings == null) throw new IllegalArgumentException("Settings cannot be null.");
        this.settings = settings;
    }

    /** Sends one message and keeps it in the history. The notifyXxx methods below check the settings first. */
    @Override
    public void sendNotification(String message) {
        if (message == null || message.trim().isEmpty()) return;
        history.add("[" + LocalTime.now().withNano(0) + "] " + message);
        deliver(message);
    }

    /** Console delivery. Override for Android. */
    protected void deliver(String message) {
        System.out.println("  >> NOTIFICATION: " + message);
    }

    /** Pet condition update ("Pet condition updates" switch). */
    public void notifyStateChange(PetState state) {
        if (!settings.isNotificationsOn()) return;
        sendNotification(state.getNotificationText());
    }

    /** Near / over the limit ("Screen-time warnings" switch). */
    public void notifyWarning(String message) {
        if (!settings.isWarningsOn()) return;
        sendNotification(message);
    }

    /** One app used for too long in a row ("Screen-time warnings" switch). */
    public void notifyLongSession(UsageRecord record, int thresholdMinutes) {
        if (!settings.isWarningsOn()) return;
        if (record.getLongestSession() >= thresholdMinutes) {
            sendNotification("You've used " + record.getAppName() + " for "
                    + record.getLongestSession() + " minutes straight. Time for a break!");
        }
    }

    public List<String> getHistory() { return Collections.unmodifiableList(history); }
    public void clearHistory()       { history.clear(); }
}
