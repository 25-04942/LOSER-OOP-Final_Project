package loser.state;

/** Usage from 80% up to and including 100% of the daily limit. */
public class SickState extends PetState {
    public SickState() { this(0.8, 1.0); }
    /** Sick from {@code from} up to and including {@code upTo} (usually 100% of the limit). */
    public SickState(double from, double upTo) { super("Sick", from, upTo); }

    /** Sick includes exactly 100%; only usage above the limit is Devastated. */
    @Override
    public boolean matches(double ratio) {
        return ratio >= getMinRatio() && ratio <= getMaxRatio();
    }

    @Override public String getAppearance() { return "(>_<) *cough*"; }
    @Override public String getMessage()    { return "Let's put the screen down."; }
    @Override public String getStatusText() { return "You're getting close to your daily limit."; }
}
