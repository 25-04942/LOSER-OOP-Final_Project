package loser.state;

/** Usage above 100% of the daily limit. */
public class DevastatedState extends PetState {
    public DevastatedState() { this(1.0); }
    /** Devastated strictly above {@code from} (usually 100% of the limit). */
    public DevastatedState(double from) { super("Devastated", from, Double.POSITIVE_INFINITY); }

    /** Strictly above the limit. */
    @Override
    public boolean matches(double ratio) {
        return ratio > getMinRatio();
    }

    @Override public String getAppearance() { return "(T_T)"; }
    @Override public String getMessage()    { return "A fresh start tomorrow."; }
    @Override public String getStatusText() { return "Your screen time is far above your limit."; }
    @Override public String getOverLimitText() { return getStatusText(); }
}
