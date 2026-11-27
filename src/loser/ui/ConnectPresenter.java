package loser.ui;

/** Content of the "Connect your device" screen, decided by the two permissions. */
public final class ConnectPresenter {
    private ConnectPresenter() { }

    public static ConnectModel present(boolean usageAllowed, boolean notificationsAllowed, String petName) {
        return new ConnectModel(
                usageAllowed,
                notificationsAllowed,
                usageAllowed ? "Allowed" : "Required",
                usageAllowed ? "Usage access allowed" : "Open usage access settings",
                !usageAllowed,
                notificationsAllowed ? "Allowed" : "Optional",
                notificationsAllowed ? "Notifications enabled" : "Enable notifications",
                usageAllowed,
                usageAllowed ? "" : "Allow usage access to continue.",
                "Two small steps to help " + petName + " look after you",
                "Let " + petName + " read your app usage and screen time to show daily summaries and pet health.",
                "In Android settings, select LOS:ER and allow access.");
    }
}
