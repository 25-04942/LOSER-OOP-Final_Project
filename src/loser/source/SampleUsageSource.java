package loser.source;

import loser.exception.MissingPermissionException;
import loser.model.Category;
import loser.model.UsageRecord;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Fake usage data for testing without a phone. Can simulate a missing permission. */
public class SampleUsageSource implements UsageSource {
    private LocalDate date;
    private boolean permissionGranted = true;

    public SampleUsageSource(LocalDate date) { this.date = date; }

    public void setDate(LocalDate date)                  { this.date = date; }
    public void setPermissionGranted(boolean granted)    { this.permissionGranted = granted; }
    public boolean isPermissionGranted()                 { return permissionGranted; }

    /** Convenience for the console version: the sample day. */
    public List<UsageRecord> readTodayUsage() throws MissingPermissionException {
        return readUsageFor(date, LocalTime.MIDNIGHT);
    }

    /** The same sample apps for any day (fake data). */
    @Override
    public List<UsageRecord> readUsageFor(LocalDate day, LocalTime resetTime) throws MissingPermissionException {
        if (!permissionGranted) throw new MissingPermissionException();
        LocalDate date = day;
        List<UsageRecord> list = new ArrayList<UsageRecord>();
        list.add(new UsageRecord("Facebook", Category.SOCIAL, 40, 20, date));
        list.add(new UsageRecord("Mobile Legends", Category.GAMES, 60, 45, date));
        list.add(new UsageRecord("Messenger", Category.COMMUNICATION, 30, 10, date));
        list.add(new UsageRecord("Google Classroom", Category.STUDY, 25, 25, date));
        list.add(new UsageRecord("Chrome", Category.OTHER, 15, 8, date));
        return list;
    }
}
