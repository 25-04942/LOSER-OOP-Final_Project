package loser.state;

/** Usage from 50% up to (not including) 80% of the daily limit. */
public class TiredState extends PetState {
    public TiredState() { this(0.5, 0.8); }
    public TiredState(double from, double sickFrom) { super("Tired", from, sickFrom); }

    @Override public String getAppearance() { return "(-_-) zzz"; }
    @Override public String getMessage()    { return "Time for a screen break."; }
    @Override public String getStatusText() { return "You're close to your daily limit."; }
}
