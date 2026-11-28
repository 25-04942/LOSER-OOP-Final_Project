package loser.ui;

import java.util.Collections;
import java.util.List;

/** Ready-to-display content for the "Your screen time" (today's usage) screen. Immutable. */
public final class UsageModel {

    /** One bar in the "By category" list. */
    public static final class CategoryRow {
        private final String label;
        private final int minutes;
        private final String minutesText;
        private final double fraction;       // 1.0 = the category with the most minutes
        private final String categoryKey;    // "social", "games", ... for picking colors

        CategoryRow(String label, int minutes, String minutesText, double fraction, String categoryKey) {
            this.label = label;
            this.minutes = minutes;
            this.minutesText = minutesText;
            this.fraction = fraction;
            this.categoryKey = categoryKey;
        }

        public String getLabel()        { return label; }
        public int getMinutes()         { return minutes; }
        public String getMinutesText()  { return minutesText; }
        public double getFraction()     { return fraction; }
        public String getCategoryKey()  { return categoryKey; }
    }

    /** One row in the "By app" list. */
    public static final class AppRow {
        private final String appName;
        private final String categoryLabel;
        private final String minutesText;
        private final String categoryKey;

        AppRow(String appName, String categoryLabel, String minutesText, String categoryKey) {
            this.appName = appName;
            this.categoryLabel = categoryLabel;
            this.minutesText = minutesText;
            this.categoryKey = categoryKey;
        }

        public String getAppName()        { return appName; }
        public String getCategoryLabel()  { return categoryLabel; }
        public String getMinutesText()    { return minutesText; }
        public String getCategoryKey()    { return categoryKey; }
    }

    private final String dateLine;
    private final String headline;
    private final String totalText;
    private final String limitLine;
    private final String stateName;
    private final double limitFraction;
    private final String remainingText;
    private final List<CategoryRow> categories;
    private final List<AppRow> apps;

    UsageModel(String headline, String dateLine, String totalText, String limitLine, String stateName,
               double limitFraction, String remainingText,
               List<CategoryRow> categories, List<AppRow> apps) {
        this.headline = headline;
        this.dateLine = dateLine;
        this.totalText = totalText;
        this.limitLine = limitLine;
        this.stateName = stateName;
        this.limitFraction = limitFraction;
        this.remainingText = remainingText;
        this.categories = Collections.unmodifiableList(categories);
        this.apps = Collections.unmodifiableList(apps);
    }

    /** e.g. "A little balance, a happier Mochi". */
    public String getHeadline()       { return headline; }
    /** e.g. "Today, Wed 11 Nov". */
    public String getDateLine()       { return dateLine; }
    /** e.g. "1h 48m". */
    public String getTotalText()      { return totalText; }
    /** e.g. "of your 3h daily limit". */
    public String getLimitLine()      { return limitLine; }
    /** Pet state label next to the total, e.g. "Healthy". */
    public String getStateName()      { return stateName; }
    /** Progress bar fill, 0.0 to 1.0 (stays at 1.0 when over the limit). */
    public double getLimitFraction()  { return limitFraction; }
    /** e.g. "1h 12m left today" or "20m over today". */
    public String getRemainingText()  { return remainingText; }
    /** Categories with usage, most minutes first. */
    public List<CategoryRow> getCategories() { return categories; }
    /** Apps, most minutes first. */
    public List<AppRow> getApps()     { return apps; }
    public boolean isEmpty()          { return apps.isEmpty(); }
}
