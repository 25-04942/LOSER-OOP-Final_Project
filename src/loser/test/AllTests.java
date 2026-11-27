package loser.test;

import loser.core.Pet;
import loser.core.Settings;
import loser.core.UsageTracker;
import loser.core.WeeklyReport;
import loser.exception.InvalidSettingsException;
import loser.exception.MissingPermissionException;
import loser.model.Category;
import loser.model.DailyRecord;
import loser.model.UsageRecord;
import loser.notification.NotificationService;
import loser.source.SampleUsageSource;
import loser.app.AppSession;
import loser.ui.ConnectModel;
import loser.ui.ConnectPresenter;
import loser.ui.DashboardModel;
import loser.ui.DashboardPresenter;
import loser.ui.HistoryModel;
import loser.ui.HistoryPresenter;
import loser.ui.SettingsForm;
import loser.ui.SettingsModel;
import loser.ui.SettingsPresenter;
import loser.ui.UsageModel;
import loser.ui.UsagePresenter;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

/** Simple self-checking tests (no JUnit needed). Run this class's main(). */
public class AllTests {
    private static int passed = 0, failed = 0;
    private static final LocalDate D = LocalDate.of(2026, 11, 10);

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (ok) passed++; else failed++;
    }

    private static DailyRecord day(int offset, int minutes) {
        Map<Category, Integer> m = new EnumMap<Category, Integer>(Category.class);
        m.put(Category.OTHER, minutes);
        return new DailyRecord(D.plusDays(offset), minutes, m, "Healthy");
    }

    public static void main(String[] args) throws Exception {
        usageRecord();
        settings();
        tracker();
        petStates();
        weekly();
        notifications();
        source();
        dashboard();
        usageScreen();
        historyScreen();
        settingsScreen();
        connectScreen();
        session();
        levelsAndDays();
        alerts();
        savedRecords();
        System.out.println("\n" + passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }

    static void usageRecord() {
        UsageRecord r = new UsageRecord("  Facebook ", Category.SOCIAL, 90, 40, D);
        check("record: name trimmed", r.getAppName().equals("Facebook"));
        r.addUsage(30, 55);
        check("record: addUsage total and longest", r.getMinutesUsed() == 120 && r.getLongestSession() == 55);
        boolean threw = false;
        try { new UsageRecord("X", Category.OTHER, 10, 20, D); } catch (IllegalArgumentException e) { threw = true; }
        check("record: session > total rejected", threw);
    }

    static void settings() throws Exception {
        Settings s = new Settings();
        check("settings: default limit 180", s.getDailyLimit() == 180);
        s.setDailyLimit(120);
        check("settings: valid limit accepted", s.getDailyLimit() == 120);
        boolean low = false, high = false;
        try { s.setDailyLimit(0); } catch (InvalidSettingsException e) { low = true; }
        try { s.setDailyLimit(5000); } catch (InvalidSettingsException e) { high = true; }
        check("settings: too low rejected", low);
        check("settings: too high rejected", high);
        check("settings: limit unchanged after rejection", s.getDailyLimit() == 120);
        boolean nullTime = false;
        try { s.setResetTime(null); } catch (InvalidSettingsException e) { nullTime = true; }
        check("settings: null reset time rejected", nullTime);
    }

    static void tracker() throws Exception {
        Settings s = new Settings(100, java.time.LocalTime.MIDNIGHT, true);
        UsageTracker t = new UsageTracker(s, D);
        check("tracker: empty total is 0", t.getTotalScreenTime() == 0 && t.getRatio() == 0.0);
        t.addUsage(new UsageRecord("Facebook", Category.SOCIAL, 30, 20, D));
        t.addUsage(new UsageRecord("facebook", Category.SOCIAL, 20, 20, D));
        t.addUsage(new UsageRecord("Game", Category.GAMES, 40, 40, D));
        check("tracker: same app merged", t.getRecords().size() == 2);
        check("tracker: total", t.getTotalScreenTime() == 90);
        check("tracker: ratio", Math.abs(t.getRatio() - 0.9) < 1e-9);
        check("tracker: category totals", t.getTotalByCategory().get(Category.SOCIAL) == 50
                && t.getTotalByCategory().get(Category.STUDY) == 0);
        check("tracker: most used app", t.getMostUsedApp().getAppName().equals("Facebook"));
        check("tracker: longest session", t.getLongestSession() == 40);
        boolean wrongDate = false;
        try { t.addUsage(new UsageRecord("Old", Category.OTHER, 5, 5, D.minusDays(1))); }
        catch (IllegalArgumentException e) { wrongDate = true; }
        check("tracker: wrong date rejected", wrongDate);
        DailyRecord dr = t.createDailyRecord("Sick");
        check("tracker: daily record snapshot", dr.getTotalMinutes() == 90 && dr.getPetStateName().equals("Sick"));
        t.reset(D.plusDays(1));
        check("tracker: reset clears", t.isEmpty() && t.getDate().equals(D.plusDays(1)));
    }

    static void petStates() {
        Pet p = new Pet("Lo");
        check("pet: starts Healthy at full HP",
                p.getCurrentState().getStateName().equals("Healthy") && p.getHealthPoints() == 100);
        double[] ratios = {0.0, 0.49, 0.50, 0.79, 0.80, 1.00, 1.01, 5.0};
        String[] expect = {"Healthy", "Healthy", "Tired", "Tired", "Sick", "Sick", "Devastated", "Devastated"};
        for (int i = 0; i < ratios.length; i++) {
            p.updateState(ratios[i]);
            check("pet: ratio " + ratios[i] + " -> " + expect[i],
                    p.getCurrentState().getStateName().equals(expect[i]));
        }
        check("pet: HP never negative", p.getHealthPoints() == 0);
        p.updateState(0.0);
        boolean changed = p.updateState(0.6);
        check("pet: updateState reports change", changed);
        check("pet: no change reported when same state", !p.updateState(0.65));
        boolean threw = false;
        try { p.updateState(-1); } catch (IllegalArgumentException e) { threw = true; }
        check("pet: negative ratio rejected", threw);
        p.resetPet();
        check("pet: reset", p.getHealthPoints() == 100 && p.getCurrentState().getStateName().equals("Healthy"));
        check("pet: messages differ by state (polymorphism)",
                !new loser.state.HealthyState().getMessage().equals(new loser.state.SickState().getMessage()));
    }

    static void weekly() {
        WeeklyReport w = new WeeklyReport();
        check("weekly: empty average 0", w.getWeeklyAverage() == 0.0 && w.getHighestDay() == null);
        check("weekly: empty trend", w.getTrend() == WeeklyReport.Trend.NOT_ENOUGH_DATA);
        check("weekly: empty display message", w.displayWeek().startsWith("No data"));
        for (int i = 0; i < 9; i++) w.addDay(day(i, 100 + i * 10));
        check("weekly: keeps only 7 days", w.size() == 7);
        check("weekly: oldest dropped", w.getDays().get(0).getDate().equals(D.plusDays(2)));
        check("weekly: highest day", w.getHighestDay().getTotalMinutes() == 180);
        check("weekly: average", Math.abs(w.getWeeklyAverage() - 150.0) < 1e-9);
        check("weekly: rising usage = worsening", w.getTrend() == WeeklyReport.Trend.WORSENING);
        w.addDay(day(8, 120));
        check("weekly: same date replaced", w.size() == 7 && w.getDays().get(6).getTotalMinutes() == 120);

        WeeklyReport v = new WeeklyReport();
        v.addDay(day(0, 200)); v.addDay(day(1, 200)); v.addDay(day(2, 100));
        check("weekly: falling usage = improving", v.getTrend() == WeeklyReport.Trend.IMPROVING);
        WeeklyReport u = new WeeklyReport();
        u.addDay(day(0, 100)); u.addDay(day(1, 105));
        check("weekly: small change = stable", u.getTrend() == WeeklyReport.Trend.STABLE);
    }

    static void notifications() throws Exception {
        Settings s = new Settings();
        NotificationService n = new NotificationService(s) {
            @Override protected void deliver(String m) { /* silent in tests */ }
        };
        n.sendNotification("hello");
        check("notify: stored when ON", n.getHistory().size() == 1);
        s.setNotificationsOn(false);
        n.notifyStateChange(new loser.state.SickState());
        check("notify: pet updates ignored when that switch is OFF", n.getHistory().size() == 1);
        s.setNotificationsOn(true);
        s.setWarningsOn(false);
        n.notifyWarning("near the limit");
        n.notifyLongSession(new UsageRecord("Game", Category.GAMES, 90, 90, D), 60);
        check("notify: warnings ignored when that switch is OFF", n.getHistory().size() == 1);
        s.setWarningsOn(true);
        n.sendNotification("   ");
        check("notify: blank ignored", n.getHistory().size() == 1);
        n.notifyLongSession(new UsageRecord("Game", Category.GAMES, 90, 90, D), 60);
        check("notify: long session", n.getHistory().size() == 2);
        n.notifyStateChange(new loser.state.SickState());
        n.notifyWarning("near the limit");
        check("notify: state change and warning go through when ON", n.getHistory().size() == 4);
        n.clearHistory();
        check("notify: clear", n.getHistory().isEmpty());
    }

    static void source() throws Exception {
        SampleUsageSource src = new SampleUsageSource(D);
        check("source: returns data", src.readTodayUsage().size() == 5);
        src.setPermissionGranted(false);
        boolean threw = false;
        try { src.readTodayUsage(); } catch (MissingPermissionException e) { threw = true; }
        check("source: missing permission throws", threw);
    }

    static DashboardModel present(int minutes, int limit) throws Exception {
        Settings s = new Settings(limit, java.time.LocalTime.MIDNIGHT, true);
        UsageTracker t = new UsageTracker(s, D);
        if (minutes > 0) t.addUsage(new UsageRecord("App", Category.OTHER, minutes, Math.min(minutes, 30), D));
        Pet p = new Pet("Mochi");
        p.updateState(t.getRatio());
        return DashboardPresenter.present(p, t, s);
    }

    static void dashboard() throws Exception {
        check("ui: formatMinutes 108", DashboardPresenter.formatMinutes(108).equals("1h 48m"));
        check("ui: formatMinutes 180", DashboardPresenter.formatMinutes(180).equals("3h"));
        check("ui: formatMinutes 45", DashboardPresenter.formatMinutes(45).equals("45m"));
        check("ui: formatMinutes 0", DashboardPresenter.formatMinutes(0).equals("0m"));
        DashboardModel m = present(108, 180);   // numbers from the Figma mock-up
        check("ui: total text", m.getTotalText().equals("1h 48m"));
        check("ui: limit line", m.getLimitLine().equals("of your 3h daily limit \u2022 1h 12m left"));
        check("ui: condition title uses real state", m.getConditionTitle().equals("Tired: 1h 12m left"));
        check("ui: reset line", m.getResetLine().equals(
                "Mochi resets at midnight. Your daily usage is saved in your weekly history."));
        DashboardModel h = present(60, 180);
        check("ui: healthy title", h.getConditionTitle().equals("Healthy: 2h left"));
        check("ui: healthy copy matches design", h.getSpeech().equals("A little balance feels good!")
                && h.getConditionBody().equals("Well within your daily limit."));
        check("ui: sprite name", h.getSpriteName().equals("pet_healthy") && m.getSpriteName().equals("pet_tired"));
        check("ui: at limit", present(180, 180).getConditionTitle().equals("Sick: limit reached"));
        DashboardModel over = present(200, 180);
        check("ui: over limit", over.getConditionTitle().equals("Devastated: 20m over")
                && over.getLimitLine().endsWith("20m over"));
        check("ui: devastated copy matches design", over.getConditionBody().equals("Your screen time is far above your limit.")
                && over.getSpeech().equals("A fresh start tomorrow."));
        check("ui: default over-limit text (Sick design)",
                new loser.state.SickState().getOverLimitText().equals("You've passed your daily limit."));
        DashboardModel far = present(260, 180);   // the design's 4h 20m of 3h
        check("ui: devastated design numbers", far.getTotalText().equals("4h 20m")
                && far.getLimitLine().equals("of your 3h daily limit \u2022 1h 20m over")
                && far.getConditionTitle().equals("Devastated: 1h 20m over"));
        DashboardModel sick = present(150, 180);   // 83% -> Sick
        check("ui: sick copy", sick.getSpeech().equals("Let's put the screen down.")
                && sick.getBackdropName().equals("room_backdrop_sick")
                && sick.getConditionBody().equals("You're getting close to your daily limit."));
        check("ui: empty day", present(0, 180).getTotalText().equals("0m"));
        DashboardModel tired = present(110, 180);   // 61% -> Tired
        check("ui: tired copy matches design", tired.getConditionTitle().equals("Tired: 1h 10m left")
                && tired.getSpeech().equals("Time for a screen break.")
                && tired.getConditionBody().equals("You're close to your daily limit."));
        check("ui: per-state drawable names", tired.getBackdropName().equals("room_backdrop_tired")
                && tired.getIndicatorName().equals("ic_condition_tired")
                && h.getBackdropName().equals("room_backdrop_healthy")
                && h.getIndicatorName().equals("ic_condition_healthy"));
        Settings s = new Settings(180, java.time.LocalTime.of(6, 0), true);
        UsageTracker t = new UsageTracker(s, D);
        check("ui: custom reset time", DashboardPresenter.present(new Pet("Mochi"), t, s)
                .getResetLine().startsWith("Mochi resets at 6:00 AM."));
    }

    static void session() throws Exception {
        AppSession a = new AppSession("Mochi", D);
        final int[] shown = {0};
        a.setNotifier(new NotificationService(a.getSettings()) {
            @Override protected void deliver(String msg) { shown[0]++; }
        });
        a.sync(new SampleUsageSource(D), D);   // 170 of 180 min
        check("session: sync loads usage", a.getTracker().getTotalScreenTime() == 170);
        check("session: pet became Sick", a.getPet().getCurrentState().getStateName().equals("Sick"));
        check("session: state change + near-limit warning sent (2 alerts)", shown[0] == 2);
        a.sync(new SampleUsageSource(D), D);
        check("session: repeated sync does not double count", a.getTracker().getTotalScreenTime() == 170);
        check("session: no repeated alerts", shown[0] == 2);
        check("session: same day no rollover", !a.rollOverIfNewDay(D));
        LocalDate next = D.plusDays(1);
        check("session: new day rolls over", a.rollOverIfNewDay(next));
        check("session: yesterday saved", a.getWeeklyReport().size() == 1
                && a.getWeeklyReport().getDays().get(0).getTotalMinutes() == 170
                && a.getWeeklyReport().getDays().get(0).getPetStateName().equals("Sick"));
        check("session: tracker and pet reset", a.getTracker().isEmpty()
                && a.getPet().getCurrentState().getStateName().equals("Healthy"));
        a.sync(new SampleUsageSource(next), next);
        check("session: sync works on the new day", a.getTracker().getTotalScreenTime() == 170);
        SampleUsageSource denied = new SampleUsageSource(next);
        denied.setPermissionGranted(false);
        boolean threw = false;
        try { a.sync(denied, next); } catch (MissingPermissionException e) { threw = true; }
        check("session: permission denied propagates", threw);
        check("session: data kept after denied sync", a.getTracker().getTotalScreenTime() == 170);
    }

    private static void addDays(WeeklyReport w, LocalDate first, int... minutes) {
        for (int i = 0; i < minutes.length; i++) {
            LocalDate d = first.plusDays(i);
            Map<Category, Integer> m = new EnumMap<Category, Integer>(Category.class);
            m.put(Category.OTHER, minutes[i]);
            w.addDay(new DailyRecord(d, minutes[i], m, "Healthy"));
        }
    }

    private static boolean keepsSeven() {
        WeeklyReport w = new WeeklyReport();
        addDays(w, LocalDate.of(2026, 11, 1), 10, 20, 30, 40, 50, 60, 70, 80, 90);
        return w.size() == 7 && w.getDays().get(0).getTotalMinutes() == 30;
    }

    static void historyScreen() throws Exception {
        Settings s = new Settings(180, java.time.LocalTime.MIDNIGHT, true);
        WeeklyReport w = new WeeklyReport();
        // the week in the Figma frame (4 - 10 Nov)
        addDays(w, LocalDate.of(2026, 11, 4), 120, 150, 240, 180, 135, 105, 108);
        HistoryModel m = HistoryPresenter.present(w, s);

        check("history: 7 days kept", w.size() == 7);
        check("history: range line", m.getRangeLine().equals("Your last 7 complete days \u2022 4\u201310 Nov"));
        check("history: daily average", m.getAverageText().equals("2h 28m"));
        check("history: trend (latest day is below the earlier days)", m.getAverageChangeText().equals("Trend: improving")
                && m.getAverageChangeTone() == HistoryModel.Tone.GOOD);
        check("history: highest day", m.getHighestText().equals("4h")
                && m.getHighestDateText().equals("Friday, 6 Nov"));
        check("history: limit text", m.getLimitText().equals("Daily limit: 3h"));
        check("history: bar letters and dates", m.getBars().size() == 7
                && m.getBars().get(0).getLetter().equals("W") && m.getBars().get(0).getDateNumber().equals("4")
                && m.getBars().get(2).getLetter().equals("F") && m.getBars().get(6).getDateNumber().equals("10"));
        // the design draws 0.45 dp per minute: Fri (4h) = 108 dp, Wed (2h) = 54 dp
        check("history: bar heights match the design", m.getBars().get(2).getFraction() == 1.0
                && Math.abs(m.getBars().get(0).getFraction() * 108 - 54) < 1e-9
                && Math.abs(m.getBars().get(1).getFraction() * 108 - 67.5) < 1e-9
                && Math.abs(m.getBars().get(6).getFraction() * 108 - 48.6) < 1e-9);
        check("history: limit line at 3h of 4h", Math.abs(m.getLimitFraction() - 0.75) < 1e-9);
        check("history: only Friday is over the limit", m.getBars().get(2).isOverLimit()
                && !m.getBars().get(3).isOverLimit() && !m.getBars().get(0).isOverLimit());   // 3h exactly is within
        check("history: insight", m.isInsightGood() && m.getInsightTitle().equals("More balance this week")
                && m.getInsightBody().equals("6 of 7 days within your 3h limit.\nKeep making time for life offline."));
        check("history: records newest first", m.getRecords().size() == 7
                && m.getRecords().get(0).getDateText().equals("Tue, 10 Nov")
                && m.getRecords().get(0).getTotalText().equals("1h 48m")
                && m.getRecords().get(0).getStatusText().equals("Within limit  \u203a")
                && m.getRecords().get(1).getDateText().equals("Mon, 9 Nov")
                && m.getRecords().get(2).getTotalText().equals("2h 15m")
                && m.getRecords().get(4).getStatusText().equals("Over limit  \u203a"));

        // not enough days for a trend
        WeeklyReport one = new WeeklyReport();
        addDays(one, LocalDate.of(2026, 11, 10), 108);
        HistoryModel m2 = HistoryPresenter.present(one, s);
        check("history: one day has no trend yet", m2.getAverageChangeText().equals("Trend: not enough data yet")
                && m2.getAverageChangeTone() == HistoryModel.Tone.NEUTRAL);
        check("history: more than 7 days keeps the latest 7", keepsSeven());

        // empty report
        HistoryModel e = HistoryPresenter.present(new WeeklyReport(), s);
        check("history: empty", e.isEmpty() && e.getRecords().isEmpty() && e.getHighestText().equals("\u2013")
                && e.getAverageText().equals("0m"));

        // a bad week
        WeeklyReport bad = new WeeklyReport();
        addDays(bad, LocalDate.of(2026, 11, 4), 200, 250, 240, 181, 135, 300, 108);
        HistoryModel b = HistoryPresenter.present(bad, s);
        check("history: busy week insight", !b.isInsightGood() && b.getInsightBody().startsWith("2 of 7 days"));

        // filling the history from a usage source
        AppSession a = new AppSession("Mochi", LocalDate.of(2026, 11, 11));
        a.loadHistory(new SampleUsageSource(LocalDate.of(2026, 11, 11)), LocalDate.of(2026, 11, 11));
        check("history: loaded 7 complete days before today", a.getWeeklyReport().size() == 7
                && a.getWeeklyReport().getDays().get(0).getDate().equals(LocalDate.of(2026, 11, 4))
                && a.getWeeklyReport().getDays().get(6).getDate().equals(LocalDate.of(2026, 11, 10))
                && a.getWeeklyReport().getDays().get(0).getTotalMinutes() == 170
                && a.getWeeklyReport().getDays().get(0).getPetStateName().equals("Sick"));
        SampleUsageSource denied = new SampleUsageSource(LocalDate.of(2026, 11, 11));
        denied.setPermissionGranted(false);
        boolean threw = false;
        try { a.loadHistory(denied, LocalDate.of(2026, 11, 11)); } catch (MissingPermissionException ex) { threw = true; }
        check("history: permission denied propagates", threw);
    }

    static void settingsScreen() throws Exception {
        Settings s = new Settings();                       // default: 3 hours, both switches on
        SettingsModel m = SettingsPresenter.present(s, new Pet("Mochi"), true);
        check("settings: limit boxes", m.getHoursText().equals("03") && m.getMinutesText().equals("00"));
        check("settings: hint", m.getLimitHint().equals("Choose 1 minute to 24 hours."));
        check("settings: switches", m.isPetUpdatesOn() && m.isWarningsOn());
        check("settings: notification status", m.getNotificationStatus().equals("Notifications allowed on this device")
                && m.isNotificationsAllowed());
        check("settings: preview texts", m.getPreviewCaption().equals("MOCHI  \u2022  SCREEN TIME")
                && m.getPreviewTitle().equals("Time for a little break?")
                && m.getPreviewBody().equals("You\u2019ve used 2h 24m of your 3h limit.\nGive your eyes\u2014and Mochi\u2014a rest."));
        check("settings: preview follows the limit", SettingsPresenter.previewBody(90, "Mochi").startsWith("You\u2019ve used 1h 12m of your 1h 30m limit."));
        check("settings: notifications blocked text", !SettingsPresenter.present(s, new Pet("Mochi"), false).isNotificationsAllowed()
                && SettingsPresenter.present(s, new Pet("Mochi"), false).getNotificationStatus().startsWith("Notifications are turned off"));

        SettingsPresenter.apply(s, "2", "35", false, false);
        check("settings: saved limit and switches", s.getDailyLimit() == 155 && !s.isNotificationsOn() && !s.isWarningsOn());
        check("settings: boxes show saved limit", SettingsPresenter.present(s, new Pet("Mochi"), true).getHoursText().equals("02")
                && SettingsPresenter.present(s, new Pet("Mochi"), true).getMinutesText().equals("35"));
        SettingsPresenter.apply(s, "24", "0", true, true);
        check("settings: 24 hours is allowed", s.getDailyLimit() == 1440);
        SettingsPresenter.apply(s, "", "1", true, true);
        check("settings: 1 minute is allowed (greater than zero), empty box is 0", s.getDailyLimit() == 1);

        String[][] bad = {{"0", "0"}, {"24", "1"}, {"25", "0"}, {"1", "60"}, {"abc", "0"}, {"-1", "30"}, {"1.5", "0"}, {"", ""}};
        boolean allRejected = true;
        for (String[] b : bad) {
            Settings t = new Settings();
            try { SettingsPresenter.apply(t, b[0], b[1], false, false); allRejected = false; }
            catch (InvalidSettingsException e) {
                if (t.getDailyLimit() != 180 || !t.isNotificationsOn() || !t.isWarningsOn()) allRejected = false;   // nothing changed
            }
        }
        check("settings: bad input rejected and nothing changes", allRejected);

        // everything on the screen, saved together
        Settings all = new Settings();
        Pet pet = new Pet("Mochi");
        SettingsForm base = SettingsForm.current(all, pet);
        check("settings: form starts from the saved values", base.getHours().equals("3") && base.getMinutes().equals("0")
                && base.getPetName().equals("Mochi") && base.getLongSession().equals("60") && base.getTired().equals("50"));
        SettingsPresenter.applyAll(all, pet, base.withLimit("2", "30").withPetName("  Lo  ")
                .withResetTime(java.time.LocalTime.of(4, 30)).withLongSession("45")
                .withLevels("40", "70", "110").withSwitches(false, true));
        check("settings: applyAll saves everything", all.getDailyLimit() == 150 && pet.getName().equals("Lo")
                && all.getResetTime().equals(java.time.LocalTime.of(4, 30)) && all.getLongSessionMinutes() == 45
                && all.getTiredPercent() == 40 && all.getSickPercent() == 70 && all.getDevastatedPercent() == 110
                && !all.isNotificationsOn() && all.isWarningsOn());
        SettingsModel sm = SettingsPresenter.present(all, pet, true);
        check("settings: boxes show the saved values", sm.getPetName().equals("Lo") && sm.getResetTimeText().equals("4:30 AM")
                && sm.getLongSessionText().equals("45") && sm.getTiredText().equals("40") && sm.getDevastatedText().equals("110")
                && sm.getPreviewCaption().equals("LO  \u2022  SCREEN TIME") && sm.getSubtitle().equals("A gentler routine for you and Lo")
                && sm.getLimitNote().equals("Lo\u2019s condition follows your daily usage.\nA fresh day starts automatically at 4:30 AM.")
                && sm.getPetUpdatesDesc().equals("Know when Lo\u2019s condition changes."));
        check("settings: midnight is worded as midnight", SettingsPresenter.present(new Settings(), new Pet("Mochi"), true)
                .getLimitNote().endsWith("starts automatically at midnight."));
        String snapshot = all.toString() + pet.getName();
        SettingsForm good = SettingsForm.current(all, pet);
        SettingsForm[] bads = {
                good.withPetName("   "), good.withPetName("A very very long pet name here"),
                good.withLimit("0", "0"), good.withLimit("x", "5"), good.withResetTime(null),
                good.withLongSession("0"), good.withLongSession("abc"),
                good.withLevels("80", "50", "100"), good.withLevels("a", "b", "c"), good.withLevels("50", "80", "400")};
        boolean nothingChanged = true;
        for (SettingsForm f : bads) {
            try { SettingsPresenter.applyAll(all, pet, f); nothingChanged = false; }
            catch (InvalidSettingsException ex) { if (!(all.toString() + pet.getName()).equals(snapshot)) nothingChanged = false; }
        }
        check("settings: any mistake is rejected and nothing is saved", nothingChanged);
    }

    static void connectScreen() {
        ConnectModel none = ConnectPresenter.present(false, false, "Mochi");     // the design's frame
        check("connect: nothing allowed (design)", none.getUsageStatus().equals("Required")
                && none.getUsageButton().equals("Open usage access settings")
                && none.isUsageFootnoteVisible()
                && none.getNotificationStatus().equals("Optional")
                && none.getNotificationButton().equals("Enable notifications"));
        check("connect: Continue unavailable without usage access", !none.canContinue()
                && none.getContinueHint().equals("Allow usage access to continue."));
        check("connect: texts use the pet's name", none.getSubtitle().equals("Two small steps to help Mochi look after you")
                && none.getUsageBody().startsWith("Let Mochi read your app usage")
                && ConnectPresenter.present(false, false, "Lo").getSubtitle().contains("help Lo look"));
        ConnectModel notifOnly = ConnectPresenter.present(false, true, "Mochi");
        check("connect: notifications alone do not unlock Continue", !notifOnly.canContinue()
                && notifOnly.getNotificationStatus().equals("Allowed"));
        ConnectModel usage = ConnectPresenter.present(true, false, "Mochi");
        check("connect: usage access unlocks Continue", usage.canContinue() && usage.getContinueHint().isEmpty()
                && usage.getUsageStatus().equals("Allowed") && !usage.isUsageFootnoteVisible()
                && usage.getUsageButton().equals("Usage access allowed")
                && usage.getNotificationButton().equals("Enable notifications"));
        ConnectModel both = ConnectPresenter.present(true, true, "Mochi");
        check("connect: both allowed", both.canContinue() && both.getNotificationButton().equals("Notifications enabled"));
    }

    static void levelsAndDays() throws Exception {
        // adjustable levels (proposal: "thresholds are adjustable")
        Settings s = new Settings();
        check("levels: defaults 50/80/100", s.getTiredPercent() == 50 && s.getSickPercent() == 80 && s.getDevastatedPercent() == 100);
        s.setThresholds(30, 60, 90);
        Pet p = new Pet("Mochi");
        p.setThresholds(s.getTiredRatio(), s.getSickRatio(), s.getDevastatedRatio());
        p.updateState(0.29);  boolean a = p.getCurrentState().getStateName().equals("Healthy");
        p.updateState(0.30);  boolean b = p.getCurrentState().getStateName().equals("Tired");
        p.updateState(0.60);  boolean c = p.getCurrentState().getStateName().equals("Sick");
        p.updateState(0.90);  boolean d = p.getCurrentState().getStateName().equals("Sick");        // 90% is still Sick
        p.updateState(0.91);  boolean e = p.getCurrentState().getStateName().equals("Devastated");
        check("levels: pet follows custom levels", a && b && c && d && e);
        check("levels: change detected by state name", !new Pet("X").updateState(0.1));
        String before = new Settings().toString();
        boolean allBad = true;
        int[][] bad = {{0, 80, 100}, {50, 50, 100}, {60, 50, 100}, {50, 80, 80}, {50, 80, 301}};
        for (int[] t : bad) {
            Settings x = new Settings();
            try { x.setThresholds(t[0], t[1], t[2]); allBad = false; }
            catch (InvalidSettingsException ex) { if (!x.toString().equals(before)) allBad = false; }
        }
        check("levels: bad levels rejected, nothing changes", allBad);
        boolean low = false, high = false;
        try { s.setLongSessionMinutes(0); } catch (InvalidSettingsException ex) { low = true; }
        try { s.setLongSessionMinutes(1441); } catch (InvalidSettingsException ex) { high = true; }
        s.setLongSessionMinutes(45);
        check("levels: long session validated", low && high && s.getLongSessionMinutes() == 45
                && new Settings().getLongSessionMinutes() == 60);

        // the day follows the reset time
        Settings r = new Settings(180, java.time.LocalTime.of(3, 0), true);
        check("reset time: 01:30 still belongs to yesterday",
                r.dayOf(java.time.LocalDateTime.of(2026, 11, 11, 1, 30)).equals(LocalDate.of(2026, 11, 10)));
        check("reset time: 03:00 starts the new day",
                r.dayOf(java.time.LocalDateTime.of(2026, 11, 11, 3, 0)).equals(LocalDate.of(2026, 11, 11)));
        check("reset time: midnight default",
                new Settings().dayOf(java.time.LocalDateTime.of(2026, 11, 11, 0, 0)).equals(LocalDate.of(2026, 11, 11)));

        // the session uses it
        AppSession a1 = new AppSession("Mochi", LocalDate.of(2026, 11, 10));
        a1.getSettings().setResetTime(java.time.LocalTime.of(3, 0));
        check("reset time: session day", a1.logicalDay(java.time.LocalDateTime.of(2026, 11, 11, 2, 0)).equals(LocalDate.of(2026, 11, 10)));
        final java.time.LocalTime[] asked = {null};
        a1.sync(new loser.source.UsageSource() {
            @Override public java.util.List<UsageRecord> readUsageFor(LocalDate day, java.time.LocalTime reset) {
                asked[0] = reset;
                return new java.util.ArrayList<UsageRecord>();
            }
        }, LocalDate.of(2026, 11, 10));
        check("reset time: source is told the reset time", java.time.LocalTime.of(3, 0).equals(asked[0]));

        // the day moves backwards when the reset time is moved later: nothing is saved
        AppSession a2 = new AppSession("Mochi", LocalDate.of(2026, 11, 11));
        a2.getTracker().addUsage(new UsageRecord("App", Category.OTHER, 30, 10, LocalDate.of(2026, 11, 11)));
        check("reset time: day moving back does not save", !a2.rollOverIfNewDay(LocalDate.of(2026, 11, 10))
                && a2.getWeeklyReport().isEmpty() && a2.getTracker().getDate().equals(LocalDate.of(2026, 11, 10))
                && a2.getTracker().isEmpty());
    }

    private static UsageTracker trackerWith(Settings s, LocalDate day, int... minutesAndLongest) {
        UsageTracker t = new UsageTracker(s, day);
        for (int i = 0; i < minutesAndLongest.length; i += 2) {
            t.addUsage(new UsageRecord("App" + i, Category.OTHER, minutesAndLongest[i], minutesAndLongest[i + 1], day));
        }
        return t;
    }

    static void alerts() throws Exception {
        final java.util.List<String> got = new java.util.ArrayList<String>();
        Settings s = new Settings();                                   // limit 3h, long session 60
        NotificationService n = new NotificationService(s) {
            @Override protected void deliver(String m) { got.add(m); }
        };
        loser.notification.UsageMonitor mon = new loser.notification.UsageMonitor();
        Pet pet = new Pet("Mochi");
        LocalDate day = LocalDate.of(2026, 11, 11);

        UsageTracker t = trackerWith(s, day, 60, 30);                  // 1h, 33%
        pet.updateState(t.getRatio());
        mon.check(t, s, pet, n);
        check("alerts: nothing at 33%", got.isEmpty());

        t = trackerWith(s, day, 144, 40);                              // 2h 24m = exactly 80%
        pet.updateState(t.getRatio());
        mon.check(t, s, pet, n);
        check("alerts: near-limit warning at 80% (and the state change)", got.size() == 2
                && got.get(1).equals("You've used 2h 24m of your 3h limit. Give your eyes\u2014and Mochi\u2014a rest."));
        mon.check(t, s, pet, n);
        check("alerts: each alert only once", got.size() == 2);

        t = trackerWith(s, day, 190, 40);                              // 3h 10m: over the limit
        pet.updateState(t.getRatio());
        mon.check(t, s, pet, n);
        check("alerts: limit warning + Devastated", got.size() == 4
                && got.get(2).startsWith("Your pet is Devastated")
                && got.get(3).equals("You've reached your daily limit of 3h. Mochi could use a break."));

        // long single session
        got.clear();
        Pet p2 = new Pet("Mochi");
        loser.notification.UsageMonitor m2 = new loser.notification.UsageMonitor();
        t = trackerWith(s, day, 70, 59);
        p2.updateState(t.getRatio());
        m2.check(t, s, p2, n);
        check("alerts: 59 minutes in a row is fine", got.isEmpty());
        t = trackerWith(s, day, 70, 60);
        m2.check(t, s, p2, n);
        check("alerts: 60 minutes in a row warns once", got.size() == 1 && got.get(0).contains("60 minutes straight"));
        m2.check(t, s, p2, n);
        check("alerts: long session only once", got.size() == 1);

        // a new day starts fresh
        t = trackerWith(s, day.plusDays(1), 70, 60);
        m2.check(t, s, p2, n);
        check("alerts: new day can alert again", got.size() == 2);

        // switches
        got.clear();
        Settings off = new Settings();
        off.setWarningsOn(false);
        off.setNotificationsOn(false);
        NotificationService n2 = new NotificationService(off) {
            @Override protected void deliver(String m) { got.add(m); }
        };
        Pet p3 = new Pet("Mochi");
        t = trackerWith(off, day, 200, 100);
        p3.updateState(t.getRatio());
        new loser.notification.UsageMonitor().check(t, off, p3, n2);
        check("alerts: both switches OFF sends nothing", got.isEmpty());

        // survives an app restart
        got.clear();
        Pet p4 = new Pet("Mochi");
        loser.notification.UsageMonitor first = new loser.notification.UsageMonitor();
        t = trackerWith(s, day, 150, 20);
        p4.updateState(t.getRatio());
        first.check(t, s, p4, n);
        int sentBefore = got.size();
        loser.notification.UsageMonitor restarted = new loser.notification.UsageMonitor();
        restarted.restoreState(first.saveState());
        restarted.check(t, s, p4, n);
        check("alerts: saved state stops repeats after a restart", sentBefore == 2 && got.size() == 2);
        loser.notification.UsageMonitor broken = new loser.notification.UsageMonitor();
        broken.restoreState("garbage");
        broken.restoreState(null);
        check("alerts: damaged saved state is ignored", broken.saveState().startsWith("day="));
    }

    static void savedRecords() throws Exception {
        java.util.Map<Category, Integer> cats = new EnumMap<Category, Integer>(Category.class);
        cats.put(Category.SOCIAL, 48);
        cats.put(Category.GAMES, 30);
        DailyRecord r = new DailyRecord(LocalDate.of(2026, 11, 10), 78, cats, "Healthy");
        String line = loser.storage.RecordCodec.encode(r);
        check("storage: line format", line.equals("2026-11-10|78|Healthy|SOCIAL=48;GAMES=30"));
        DailyRecord back = loser.storage.RecordCodec.decode(line);
        check("storage: round trip", back.getDate().equals(r.getDate()) && back.getTotalMinutes() == 78
                && back.getPetStateName().equals("Healthy") && back.getCategoryTotals().get(Category.GAMES) == 30);
        DailyRecord empty = loser.storage.RecordCodec.decode(loser.storage.RecordCodec.encode(
                new DailyRecord(LocalDate.of(2026, 11, 9), 0, new EnumMap<Category, Integer>(Category.class), "Healthy")));
        check("storage: day with no usage", empty.getTotalMinutes() == 0 && empty.getCategoryTotals().isEmpty());
        java.util.List<DailyRecord> list = new java.util.ArrayList<DailyRecord>();
        list.add(r); list.add(empty);
        String text = loser.storage.RecordCodec.encodeAll(list) + "\nthis is damaged\n2026-13-45|1|X|\n";
        java.util.List<DailyRecord> loaded = loser.storage.RecordCodec.decodeAll(text);
        check("storage: damaged lines are skipped", loaded.size() == 2);
        check("storage: empty text", loser.storage.RecordCodec.decodeAll("").isEmpty() && loser.storage.RecordCodec.decodeAll(null).isEmpty());

        // restoring into a session, and only the missing days are read again
        AppSession a = new AppSession("Mochi", LocalDate.of(2026, 11, 11));
        final java.util.List<LocalDate> read = new java.util.ArrayList<LocalDate>();
        loser.source.UsageSource counting = new loser.source.UsageSource() {
            @Override public java.util.List<UsageRecord> readUsageFor(LocalDate day, java.time.LocalTime reset) {
                read.add(day);
                return new java.util.ArrayList<UsageRecord>();
            }
        };
        a.restoreDays(loaded);                       // 9 Nov and 10 Nov are saved
        final int[] saves = {0};
        a.setDaySavedListener(new AppSession.DaySavedListener() {
            @Override public void onDaysChanged(WeeklyReport report) { saves[0]++; }
        });
        a.loadHistory(counting, LocalDate.of(2026, 11, 11));
        check("storage: loadHistory skips saved days but re-reads yesterday",
                read.size() == 6 && read.contains(LocalDate.of(2026, 11, 10)) && !read.contains(LocalDate.of(2026, 11, 9)));
        check("storage: listener told once", saves[0] == 1 && a.getWeeklyReport().size() == 7);
        check("storage: yesterday was replaced by the fresh read", a.getWeeklyReport().getDays().get(6).getTotalMinutes() == 0);
        a.rollOverIfNewDay(LocalDate.of(2026, 11, 12));
        check("storage: rollover also tells the listener", saves[0] == 2);
    }

    static void usageScreen() throws Exception {
        LocalDate wed = LocalDate.of(2026, 11, 11);                 // the date in the Figma frame
        Settings s = new Settings(180, java.time.LocalTime.MIDNIGHT, true);
        UsageTracker t = new UsageTracker(s, wed);
        t.addUsage(new UsageRecord("WhatsApp", Category.COMMUNICATION, 10, 5, wed));   // added out of order
        t.addUsage(new UsageRecord("Google Classroom", Category.STUDY, 20, 20, wed));
        t.addUsage(new UsageRecord("Instagram", Category.SOCIAL, 48, 30, wed));
        t.addUsage(new UsageRecord("Candy Crush", Category.GAMES, 30, 15, wed));
        Pet p = new Pet("Mochi");
        p.updateState(t.getRatio());
        UsageModel m = UsagePresenter.present(p, t, s);

        check("usage: headline uses the pet's name", m.getHeadline().equals("A little balance, a happier Mochi"));
        check("usage: date line", m.getDateLine().equals("Today, Wed 11 Nov"));
        check("usage: total and limit line", m.getTotalText().equals("1h 48m")
                && m.getLimitLine().equals("of your 3h daily limit"));
        check("usage: remaining text", m.getRemainingText().equals("1h 12m left today"));
        check("usage: progress fraction is 60%", Math.abs(m.getLimitFraction() - 0.6) < 1e-9);
        check("usage: category order and labels", m.getCategories().size() == 4
                && m.getCategories().get(0).getLabel().equals("Social media")
                && m.getCategories().get(1).getLabel().equals("Games")
                && m.getCategories().get(2).getLabel().equals("Study")
                && m.getCategories().get(3).getLabel().equals("Communication"));
        check("usage: category minutes text", m.getCategories().get(0).getMinutesText().equals("48m")
                && m.getCategories().get(3).getMinutesText().equals("10m"));
        check("usage: category bars relative to the largest",
                m.getCategories().get(0).getFraction() == 1.0
                && Math.abs(m.getCategories().get(1).getFraction() - 30.0 / 48) < 1e-9
                && Math.abs(m.getCategories().get(3).getFraction() - 10.0 / 48) < 1e-9);
        check("usage: category keys", m.getCategories().get(1).getCategoryKey().equals("games"));
        check("usage: app order (most used first)", m.getApps().get(0).getAppName().equals("Instagram")
                && m.getApps().get(1).getAppName().equals("Candy Crush")
                && m.getApps().get(2).getAppName().equals("Google Classroom")
                && m.getApps().get(3).getAppName().equals("WhatsApp"));
        check("usage: app row texts", m.getApps().get(0).getCategoryLabel().equals("Social media")
                && m.getApps().get(0).getMinutesText().equals("48m"));
        check("usage: not empty", !m.isEmpty());

        UsageModel empty = UsagePresenter.present(new Pet("Mochi"), new UsageTracker(s, wed), s);
        check("usage: empty day", empty.isEmpty() && empty.getCategories().isEmpty()
                && empty.getTotalText().equals("0m") && empty.getLimitFraction() == 0.0
                && empty.getRemainingText().equals("3h left today"));

        UsageTracker big = new UsageTracker(s, wed);
        big.addUsage(new UsageRecord("Game", Category.GAMES, 200, 60, wed));
        UsageModel over = UsagePresenter.present(new Pet("Mochi"), big, s);
        check("usage: over limit caps the bar and says so", over.getLimitFraction() == 1.0
                && over.getRemainingText().equals("20m over today"));
        check("usage: other category is listed", UsagePresenter.present(new Pet("Mochi"), otherOnly(wed, s), s)
                .getCategories().get(0).getLabel().equals("Other"));
    }

    static UsageTracker otherOnly(LocalDate d, Settings s) {
        UsageTracker t = new UsageTracker(s, d);
        t.addUsage(new UsageRecord("Chrome", Category.OTHER, 15, 8, d));
        return t;
    }
}
