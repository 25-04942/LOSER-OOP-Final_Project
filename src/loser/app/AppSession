package loser.app;

import loser.core.Pet;
import loser.core.Settings;
import loser.core.UsageTracker;
import loser.core.WeeklyReport;
import loser.exception.MissingPermissionException;
import loser.model.DailyRecord;
import loser.model.UsageRecord;
import loser.notification.NotificationService;
import loser.notification.UsageMonitor;
import loser.source.UsageSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Holds the objects that live as long as the app (settings, pet, tracker, weekly report)
 * and the steps that keep them in sync. The Android Application class owns one of these,
 * so every screen shows the same data. No Android code here, so it can be tested on a PC.
 * Methods are synchronized because the screens and the background check both use it.
 */
public class AppSession {
    /** Told when a finished day was added to the weekly report, so it can be saved on the device. */
    public interface DaySavedListener {
        void onDaysChanged(WeeklyReport report);
    }

    private final Settings settings = new Settings();
    private final Pet pet;
    private final UsageTracker tracker;
    private final WeeklyReport weekly = new WeeklyReport();
    private final UsageMonitor monitor = new UsageMonitor();
    private NotificationService notifier = new NotificationService(settings);
    private DaySavedListener daySavedListener;

    public AppSession(String petName) {
        this(petName, LocalDate.now());
    }

    public AppSession(String petName, LocalDate today) {
        this.pet = new Pet(petName);
        this.tracker = new UsageTracker(settings, today);
    }

    public Settings getSettings()              { return settings; }
    public Pet getPet()                        { return pet; }
    public UsageTracker getTracker()           { return tracker; }
    public WeeklyReport getWeeklyReport()      { return weekly; }
    public UsageMonitor getMonitor()           { return monitor; }
    public NotificationService getNotifier()   { return notifier; }

    /** Android replaces the console notifier with one that shows real notifications. */
    public void setNotifier(NotificationService notifier) {
        if (notifier == null) throw new IllegalArgumentException("Notifier cannot be null.");
        this.notifier = notifier;
    }

    public void setDaySavedListener(DaySavedListener listener) {
        this.daySavedListener = listener;
    }

    /** The day a moment belongs to, following the user's reset time. */
    public LocalDate logicalDay(LocalDateTime moment) {
        return settings.dayOf(moment);
    }

    /** Puts saved days back into the weekly report (at app start). Does not call the listener. */
    public synchronized void restoreDays(List<DailyRecord> saved) {
        for (DailyRecord d : saved) weekly.addDay(d);
    }

    /**
     * If a new day has started, saves yesterday into the weekly report and
     * starts fresh (empty tracker, pet back to Healthy).
     * If the day moved backwards (the user set a later reset time), the tracker just switches
     * to that day without saving anything.
     * @return true if a rollover happened
     */
    public synchronized boolean rollOverIfNewDay(LocalDate today) {
        if (today.isAfter(tracker.getDate())) {
            weekly.addDay(tracker.createDailyRecord(pet.getCurrentState().getStateName()));
            tracker.reset(today);
            pet.resetPet();
            fireDaysChanged();
            return true;
        }
        if (today.isBefore(tracker.getDate())) {
            tracker.reset(today);
            pet.resetPet();
        }
        return false;
    }

    /** Reads usage from the source, then updates the tracker, the pet and the alerts. */
    public synchronized void sync(UsageSource source, LocalDate today) throws MissingPermissionException {
        rollOverIfNewDay(today);
        List<UsageRecord> fresh = source.readUsageFor(today, settings.getResetTime());
        tracker.replaceAll(fresh);
        refreshPet();
    }

    /**
     * Fills the weekly report with the 7 complete days before today, read from the phone.
     * Days that are already saved are kept as they are, except yesterday, which is read again
     * (it may have changed after the last time the app was open).
     * The state recorded for each day is the one the pet would have had at that day's usage.
     */
    public synchronized void loadHistory(UsageSource source, LocalDate today) throws MissingPermissionException {
        boolean changed = false;
        for (int i = 1; i <= WeeklyReport.MAX_DAYS; i++) {
            LocalDate day = today.minusDays(i);
            if (i > 1 && weekly.hasDay(day)) continue;
            UsageTracker dayTracker = new UsageTracker(settings, day);
            dayTracker.replaceAll(source.readUsageFor(day, settings.getResetTime()));
            Pet dayPet = new Pet(pet.getName());
            applyThresholds(dayPet);
            dayPet.updateState(dayTracker.getRatio());
            weekly.addDay(dayTracker.createDailyRecord(dayPet.getCurrentState().getStateName()));
            changed = true;
        }
        if (changed) fireDaysChanged();
    }

    /** Re-evaluates the pet from the current ratio and levels (call after the limit or levels change too). */
    public synchronized void refreshPet() {
        applyThresholds(pet);
        pet.updateState(tracker.getRatio());
        monitor.check(tracker, settings, pet, notifier);
    }

    private void applyThresholds(Pet target) {
        target.setThresholds(settings.getTiredRatio(), settings.getSickRatio(), settings.getDevastatedRatio());
    }

    private void fireDaysChanged() {
        if (daySavedListener != null) daySavedListener.onDaysChanged(weekly);
    }
}
