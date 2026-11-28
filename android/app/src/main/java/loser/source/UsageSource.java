package loser.source;

import loser.exception.MissingPermissionException;
import loser.model.UsageRecord;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Where usage data comes from. The Android app implements this in Kotlin (UsageReader);
 * the console version uses SampleUsageSource.
 */
public interface UsageSource {
    /**
     * Usage of one "day": it starts at {@code resetTime} on {@code day} and lasts 24 hours
     * (or until now, for the current day). Every record is dated {@code day}.
     */
    List<UsageRecord> readUsageFor(LocalDate day, LocalTime resetTime) throws MissingPermissionException;
}
