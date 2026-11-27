package loser.ui;

/** Ready-to-display text for the pet dashboard. Immutable. */
public final class DashboardModel {
    private final String petName;
    private final String stateName;
    private final String speech;
    private final String totalText;
    private final String limitLine;
    private final String conditionTitle;
    private final String conditionBody;
    private final String resetLine;
    private final String spriteName;
    private final String backdropName;
    private final String indicatorName;

    DashboardModel(String petName, String stateName, String speech, String totalText,
                   String limitLine, String conditionTitle, String conditionBody,
                   String resetLine, String spriteName,
                   String backdropName, String indicatorName) {
        this.petName = petName;
        this.stateName = stateName;
        this.speech = speech;
        this.totalText = totalText;
        this.limitLine = limitLine;
        this.conditionTitle = conditionTitle;
        this.conditionBody = conditionBody;
        this.resetLine = resetLine;
        this.spriteName = spriteName;
        this.backdropName = backdropName;
        this.indicatorName = indicatorName;
    }

    public String getPetName()         { return petName; }
    /** "Healthy", "Tired", "Sick" or "Devastated" (the pill under the pet's name). */
    public String getStateName()       { return stateName; }
    /** Speech bubble text. */
    public String getSpeech()          { return speech; }
    /** Big number in the screen-time card, e.g. "1h 48m". */
    public String getTotalText()       { return totalText; }
    /** e.g. "of your 3h daily limit • 1h 12m left". */
    public String getLimitLine()       { return limitLine; }
    /** e.g. "Healthy: 1h 12m left". */
    public String getConditionTitle()  { return conditionTitle; }
    public String getConditionBody()   { return conditionBody; }
    public String getResetLine()       { return resetLine; }
    /** Drawable name, e.g. "pet_healthy". */
    public String getSpriteName()      { return spriteName; }
    /** Drawable name of the round room backdrop, e.g. "room_backdrop_tired". */
    public String getBackdropName()    { return backdropName; }
    /** Drawable name of the small icon in the condition pill, e.g. "ic_condition_tired". */
    public String getIndicatorName()   { return indicatorName; }
}
