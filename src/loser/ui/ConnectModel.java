package loser.ui;

/** Everything the "Connect your device" screen shows for the current permissions. Immutable. */
public final class ConnectModel {
    private final boolean usageAllowed;
    private final boolean notificationsAllowed;
    private final String usageStatus;
    private final String usageButton;
    private final boolean usageFootnoteVisible;
    private final String notificationStatus;
    private final String notificationButton;
    private final boolean canContinue;
    private final String continueHint;
    private final String subtitle;
    private final String usageBody;
    private final String usageFootnote;

    ConnectModel(boolean usageAllowed, boolean notificationsAllowed, String usageStatus, String usageButton,
                 boolean usageFootnoteVisible, String notificationStatus, String notificationButton,
                 boolean canContinue, String continueHint,
                 String subtitle, String usageBody, String usageFootnote) {
        this.usageAllowed = usageAllowed;
        this.notificationsAllowed = notificationsAllowed;
        this.usageStatus = usageStatus;
        this.usageButton = usageButton;
        this.usageFootnoteVisible = usageFootnoteVisible;
        this.notificationStatus = notificationStatus;
        this.notificationButton = notificationButton;
        this.canContinue = canContinue;
        this.continueHint = continueHint;
        this.subtitle = subtitle;
        this.usageBody = usageBody;
        this.usageFootnote = usageFootnote;
    }

    public boolean isUsageAllowed()           { return usageAllowed; }
    public boolean isNotificationsAllowed()   { return notificationsAllowed; }
    /** "Required" until usage access is allowed, then "Allowed". */
    public String getUsageStatus()            { return usageStatus; }
    public String getUsageButton()            { return usageButton; }
    /** The small "In Android settings, select Mochi..." line is only useful before access is allowed. */
    public boolean isUsageFootnoteVisible()   { return usageFootnoteVisible; }
    /** "Optional" until notifications are allowed, then "Allowed". */
    public String getNotificationStatus()     { return notificationStatus; }
    public String getNotificationButton()     { return notificationButton; }
    /** Continue only works once usage access is allowed (notifications are optional). */
    public boolean canContinue()              { return canContinue; }
    /** Empty once Continue is available. */
    public String getContinueHint()           { return continueHint; }
    public String getSubtitle()               { return subtitle; }
    public String getUsageBody()              { return usageBody; }
    public String getUsageFootnote()          { return usageFootnote; }
}
