package loser.ui;

import java.util.Collections;
import java.util.List;

/** Everything the "Weekly history" screen shows, already formatted. Immutable. */
public final class HistoryModel {

    /** How a number should be colored: good (green), neutral, or a warning. */
    public enum Tone { GOOD, NEUTRAL, WARN }

    /** One bar of the chart. fraction is relative to the tallest scale value (0..1). */
    public static final class Bar {
        private final String letter;
        private final String dateNumber;
        private final double fraction;
        private final boolean overLimit;
        private final String description;

        Bar(String letter, String dateNumber, double fraction, boolean overLimit, String description) {
            this.letter = letter;
            this.dateNumber = dateNumber;
            this.fraction = fraction;
            this.overLimit = overLimit;
            this.description = description;
        }

        public String getLetter()       { return letter; }
        public String getDateNumber()   { return dateNumber; }
        public double getFraction()     { return fraction; }
        public boolean isOverLimit()    { return overLimit; }
        /** For screen readers, e.g. "Fri 6 Nov: 4h, over your limit". */
        public String getDescription()  { return description; }
    }

    /** One line of "Daily records". */
    public static final class Record {
        private final String dateText;
        private final String totalText;
        private final String statusText;
        private final boolean withinLimit;

        Record(String dateText, String totalText, String statusText, boolean withinLimit) {
            this.dateText = dateText;
            this.totalText = totalText;
            this.statusText = statusText;
            this.withinLimit = withinLimit;
        }

        public String getDateText()     { return dateText; }
        public String getTotalText()    { return totalText; }
        public String getStatusText()   { return statusText; }
        public boolean isWithinLimit()  { return withinLimit; }
    }

    private final String rangeLine;
    private final String averageText;
    private final String averageChangeText;
    private final Tone averageChangeTone;
    private final String highestText;
    private final String highestDateText;
    private final String limitText;
    private final double limitFraction;
    private final List<Bar> bars;
    private final String insightTitle;
    private final String insightBody;
    private final boolean insightGood;
    private final List<Record> records;

    HistoryModel(String rangeLine, String averageText, String averageChangeText, Tone averageChangeTone,
                 String highestText, String highestDateText, String limitText, double limitFraction,
                 List<Bar> bars, String insightTitle, String insightBody, boolean insightGood,
                 List<Record> records) {
        this.rangeLine = rangeLine;
        this.averageText = averageText;
        this.averageChangeText = averageChangeText;
        this.averageChangeTone = averageChangeTone;
        this.highestText = highestText;
        this.highestDateText = highestDateText;
        this.limitText = limitText;
        this.limitFraction = limitFraction;
        this.bars = Collections.unmodifiableList(bars);
        this.insightTitle = insightTitle;
        this.insightBody = insightBody;
        this.insightGood = insightGood;
        this.records = Collections.unmodifiableList(records);
    }

    public String getRangeLine()          { return rangeLine; }
    public String getAverageText()        { return averageText; }
    public String getAverageChangeText()  { return averageChangeText; }
    public Tone getAverageChangeTone()    { return averageChangeTone; }
    public String getHighestText()        { return highestText; }
    public String getHighestDateText()    { return highestDateText; }
    public String getLimitText()          { return limitText; }
    /** Where the limit line sits, as a fraction of the chart height (0..1). */
    public double getLimitFraction()      { return limitFraction; }
    public List<Bar> getBars()            { return bars; }
    public String getInsightTitle()       { return insightTitle; }
    public String getInsightBody()        { return insightBody; }
    public boolean isInsightGood()        { return insightGood; }
    public List<Record> getRecords()      { return records; }
    public boolean isEmpty()              { return bars.isEmpty(); }
}
