package loser.state;

/**
 * Abstract base for the pet's health states.
 * Each state covers a range of the usage ratio (screen time / daily limit).
 * Subclasses must supply their own look and message (abstraction, polymorphism).
 */
public abstract class PetState {
    private final String stateName;
    private final double minRatio;   // inclusive
    private final double maxRatio;

    protected PetState(String stateName, double minRatio, double maxRatio) {
        this.stateName = stateName;
        this.minRatio = minRatio;
        this.maxRatio = maxRatio;
    }

    public String getStateName() { return stateName; }
    public double getMinRatio()  { return minRatio; }
    public double getMaxRatio()  { return maxRatio; }

    /** Default range check: minRatio <= ratio < maxRatio. Subclasses may override edges. */
    public boolean matches(double ratio) {
        return ratio >= minRatio && ratio < maxRatio;
    }

    /** Text drawn on the pet screen (ASCII face now, sprite name later). */
    public abstract String getAppearance();

    /** What the pet "says" in this state (shown in the speech bubble). */
    public abstract String getMessage();

    /** One-line status shown in the condition card under the pet. */
    public abstract String getStatusText();

    /** Status shown instead when the daily limit has been passed. States may override it. */
    public String getOverLimitText() {
        return "You've passed your daily limit.";
    }

    /** Name of the sprite image for the Android UI, e.g. "pet_healthy". */
    public String getSpriteName() {
        return "pet_" + stateName.toLowerCase();
    }

    public String getNotificationText() {
        return "Your pet is " + stateName + ". " + getMessage();
    }

    @Override
    public String toString() {
        return stateName;
    }
}
