package loser.ui;

/** Everything the "Limits & alerts" screen shows, already formatted. Immutable. */
public final class SettingsModel {
    private final String hoursText;
    private final String minutesText;
    private final String limitHint;
    private final boolean petUpdatesOn;
    private final boolean warningsOn;
    private final boolean notificationsAllowed;
    private final String notificationStatus;
    private final String previewCaption;
    private final String previewTitle;
    private final String previewBody;
    private final String petName;
    private final String resetTimeText;
    private final String longSessionText;
    private final String tiredText;
    private final String sickText;
    private final String devastatedText;
    private final String subtitle;
    private final String limitNote;
    private final String petUpdatesDesc;

    SettingsModel(String hoursText, String minutesText, String limitHint, boolean petUpdatesOn,
                  boolean warningsOn, boolean notificationsAllowed, String notificationStatus,
                  String previewCaption, String previewTitle, String previewBody,
                  String petName, String resetTimeText, String longSessionText, String tiredText,
                  String sickText, String devastatedText, String subtitle, String limitNote,
                  String petUpdatesDesc) {
        this.hoursText = hoursText;
        this.minutesText = minutesText;
        this.limitHint = limitHint;
        this.petUpdatesOn = petUpdatesOn;
        this.warningsOn = warningsOn;
        this.notificationsAllowed = notificationsAllowed;
        this.notificationStatus = notificationStatus;
        this.previewCaption = previewCaption;
        this.previewTitle = previewTitle;
        this.previewBody = previewBody;
        this.petName = petName;
        this.resetTimeText = resetTimeText;
        this.longSessionText = longSessionText;
        this.tiredText = tiredText;
        this.sickText = sickText;
        this.devastatedText = devastatedText;
        this.subtitle = subtitle;
        this.limitNote = limitNote;
        this.petUpdatesDesc = petUpdatesDesc;
    }

    public String getHoursText()            { return hoursText; }
    public String getMinutesText()          { return minutesText; }
    public String getLimitHint()            { return limitHint; }
    public boolean isPetUpdatesOn()         { return petUpdatesOn; }
    public boolean isWarningsOn()           { return warningsOn; }
    /** Whether Android lets this app show notifications on this device. */
    public boolean isNotificationsAllowed() { return notificationsAllowed; }
    public String getNotificationStatus()   { return notificationStatus; }
    public String getPreviewCaption()       { return previewCaption; }
    public String getPreviewTitle()         { return previewTitle; }
    public String getPreviewBody()          { return previewBody; }
    public String getPetName()              { return petName; }
    /** "12:00 AM" */
    public String getResetTimeText()        { return resetTimeText; }
    public String getLongSessionText()      { return longSessionText; }
    public String getTiredText()            { return tiredText; }
    public String getSickText()             { return sickText; }
    public String getDevastatedText()       { return devastatedText; }
    /** "A gentler routine for you and Mochi" */
    public String getSubtitle()             { return subtitle; }
    /** Two lines under the limit boxes. */
    public String getLimitNote()            { return limitNote; }
    public String getPetUpdatesDesc()       { return petUpdatesDesc; }
}
