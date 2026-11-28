package loser.ui;

import loser.core.Pet;
import loser.core.Settings;
import loser.core.UsageTracker;
import loser.model.Category;
import loser.model.UsageRecord;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Turns the app's objects into the content of the "Your screen time" screen. */
public final class UsagePresenter {
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH);

    private UsagePresenter() { }

    public static UsageModel present(Pet pet, UsageTracker tracker, Settings settings) {
        int total = tracker.getTotalScreenTime();
        int limit = settings.getDailyLimit();
        int remaining = limit - total;

        String remainingText;
        if (remaining > 0)       remainingText = DashboardPresenter.formatMinutes(remaining) + " left today";
        else if (remaining == 0) remainingText = "Limit reached";
        else                     remainingText = DashboardPresenter.formatMinutes(-remaining) + " over today";

        return new UsageModel(
                "A little balance, a happier " + pet.getName(),
                "Today, " + tracker.getDate().format(DAY),
                DashboardPresenter.formatMinutes(total),
                "of your " + DashboardPresenter.formatMinutes(limit) + " daily limit",
                pet.getCurrentState().getStateName(),
                Math.min(1.0, (double) total / limit),
                remainingText,
                categoryRows(tracker),
                appRows(tracker));
    }

    private static List<UsageModel.CategoryRow> categoryRows(UsageTracker tracker) {
        Map<Category, Integer> totals = tracker.getTotalByCategory();
        int max = 0;
        for (int v : totals.values()) max = Math.max(max, v);

        List<UsageModel.CategoryRow> rows = new ArrayList<UsageModel.CategoryRow>();
        for (Map.Entry<Category, Integer> e : totals.entrySet()) {
            int minutes = e.getValue();
            if (minutes <= 0) continue;
            rows.add(new UsageModel.CategoryRow(e.getKey().getLabel(), minutes,
                    DashboardPresenter.formatMinutes(minutes), (double) minutes / max, key(e.getKey())));
        }
        Collections.sort(rows, new Comparator<UsageModel.CategoryRow>() {   // stable: ties keep enum order
            @Override public int compare(UsageModel.CategoryRow a, UsageModel.CategoryRow b) {
                return b.getMinutes() - a.getMinutes();
            }
        });
        return rows;
    }

    private static List<UsageModel.AppRow> appRows(UsageTracker tracker) {
        List<UsageRecord> sorted = new ArrayList<UsageRecord>(tracker.getRecords());
        Collections.sort(sorted, new Comparator<UsageRecord>() {
            @Override public int compare(UsageRecord a, UsageRecord b) {
                int byMinutes = b.getMinutesUsed() - a.getMinutesUsed();
                return byMinutes != 0 ? byMinutes : a.getAppName().compareToIgnoreCase(b.getAppName());
            }
        });
        List<UsageModel.AppRow> rows = new ArrayList<UsageModel.AppRow>();
        for (UsageRecord r : sorted) {
            if (r.getMinutesUsed() <= 0) continue;
            rows.add(new UsageModel.AppRow(r.getAppName(), r.getCategory().getLabel(),
                    DashboardPresenter.formatMinutes(r.getMinutesUsed()), key(r.getCategory())));
        }
        return rows;
    }

    private static String key(Category c) {
        return c.name().toLowerCase(Locale.ROOT);
    }
}
