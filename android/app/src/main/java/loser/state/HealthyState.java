package loser.state;

/** Usage under 50% of the daily limit. */
public class HealthyState extends PetState {
    public HealthyState() { this(0.5); }
    /** Healthy until {@code tiredFrom} (a share of the limit, 0.5 = 50%). */
    public HealthyState(double tiredFrom) { super("Healthy", 0.0, tiredFrom); }

    @Override public String getAppearance() { return "(^o^)"; }
    @Override public String getMessage()    { return "A little balance feels good!"; }
    @Override public String getStatusText() { return "Well within your daily limit."; }
}
