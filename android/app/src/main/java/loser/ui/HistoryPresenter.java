package loser.ui;

import loser.core.Settings;
import loser.core.WeeklyReport;
import loser.model.DailyRecord;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Turns the WeeklyReport into the content of the "Weekly history" screen. */
public final class HistoryPresenter {
    private static final DateTimeFormatter DAY_MONTH = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
    private static final DateTimeFormatter SHORT_DAY = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH);
    private static final DateTimeFormatter LONG_DAY = DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.ENGLISH);
    private static final DateTimeFormatter WEEKDAY = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);

    private HistoryPresenter() { }

    public static HistoryModel present(WeeklyReport weekly, Settings settings) {
        List<DailyRecord> days = weekly.getDays();     // oldest first
        int limit = settings.getDailyLimit();
        String limitName = DashboardPresenter.formatMinutes(limit);

        DailyRecord highest = weekly.getHighestDay();
        int scaleMax = limit;
        for (DailyRecord d : days) scaleMax = Math.max(scaleMax, d.getTotalMinutes());

        // chart: oldest on the left
        List<HistoryModel.Bar> bars = new ArrayList<HistoryModel.Bar>();
        int within = 0;
        for (DailyRecord d : days) {
            boolean over = d.getTotalMinutes() > limit;
            if (!over) within++;
            bars.add(new HistoryModel.Bar(
                    d.getDate().getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH).substring(0, 1),
                    String.valueOf(d.getDate().getDayOfMonth()),
                    (double) d.getTotalMinutes() / scaleMax,
                    over,
                    d.getDate().format(WEEKDAY) + " " + d.getDate().format(DAY_MONTH) + ": "
                            + DashboardPresenter.formatMinutes(d.getTotalMinutes())
                            + (over ? ", over your limit" : ", within your limit")));
        }

        // daily records: newest first
        List<HistoryModel.Record> records = new ArrayList<HistoryModel.Record>();
        for (int i = days.size() - 1; i >= 0; i--) {
            DailyRecord d = days.get(i);
            boolean ok = d.getTotalMinutes() <= limit;
            records.add(new HistoryModel.Record(d.getDate().format(SHORT_DAY),
                    DashboardPresenter.formatMinutes(d.getTotalMinutes()),
                    (ok ? "Within limit" : "Over limit") + "  ›", ok));
        }

        // average card: the proposal asks for the daily average, the highest day and the trend
        double avg = weekly.getWeeklyAverage();
        String change;
        HistoryModel.Tone tone;
        switch (weekly.getTrend()) {
            case IMPROVING: change = "Trend: improving"; tone = HistoryModel.Tone.GOOD; break;
            case WORSENING: change = "Trend: rising";    tone = HistoryModel.Tone.WARN; break;
            case STABLE:    change = "Trend: steady";    tone = HistoryModel.Tone.NEUTRAL; break;
            default:        change = "Trend: not enough data yet"; tone = HistoryModel.Tone.NEUTRAL; break;
        }

        // insight card
        boolean good = within * 2 >= days.size();
        String title, body;
        if (days.isEmpty()) {
            title = "No weekly summary yet";
            body = "Finish a day to see how your week is going.";
            good = true;
        } else if (good) {
            title = "More balance this week";
            body = within + " of " + days.size() + " days within your " + limitName + " limit.\n"
                    + "Keep making time for life offline.";
        } else {
            title = "A busier week than planned";
            body = within + " of " + days.size() + " days within your " + limitName + " limit.\n"
                    + "Small steps still count. Try one screen-free hour.";
        }

        return new HistoryModel(
                rangeLine(days),
                DashboardPresenter.formatMinutes((int) Math.round(avg)),
                change, tone,
                highest == null ? "–" : DashboardPresenter.formatMinutes(highest.getTotalMinutes()),
                highest == null ? "No data yet" : highest.getDate().format(LONG_DAY),
                "Daily limit: " + limitName,
                (double) limit / scaleMax,
                bars, title, body, good, records);
    }

    /** "Your last 7 complete days • 4–10 Nov" (fewer days are named exactly). */
    private static String rangeLine(List<DailyRecord> days) {
        if (days.isEmpty()) return "No complete days yet";
        LocalDate first = days.get(0).getDate();
        LocalDate last = days.get(days.size() - 1).getDate();
        String range;
        if (first.equals(last)) {
            range = last.format(DAY_MONTH);
        } else if (first.getMonth() == last.getMonth()) {
            range = first.getDayOfMonth() + "–" + last.format(DAY_MONTH);
        } else {
            range = first.format(DAY_MONTH) + "–" + last.format(DAY_MONTH);
        }
        String lead = days.size() == 1 ? "Your last complete day" : "Your last " + days.size() + " complete days";
        return lead + " • " + range;
    }
}
