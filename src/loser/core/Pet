package loser.core;

import loser.state.DevastatedState;
import loser.state.HealthyState;
import loser.state.PetState;
import loser.state.SickState;
import loser.state.TiredState;

import java.util.Arrays;
import java.util.List;

/** The virtual pet. Its state follows the usage ratio. */
public class Pet {
    public static final int MAX_HEALTH = 100;

    private String name;
    private int healthPoints;
    private PetState currentState;
    private List<PetState> states;   // checked in order

    public Pet(String name) {
        setName(name);
        setThresholds(0.5, 0.8, 1.0);
        resetPet();
    }

    public String getName()            { return name; }
    public int getHealthPoints()       { return healthPoints; }
    public PetState getCurrentState()  { return currentState; }

    public final void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Pet name cannot be empty.");
        }
        this.name = name.trim();
    }

    /**
     * Sets where each state starts, as shares of the daily limit (0.5 = 50%).
     * The current state is kept (by name) until the next updateState().
     */
    public final void setThresholds(double tiredFrom, double sickFrom, double devastatedFrom) {
        if (!(tiredFrom > 0 && tiredFrom < sickFrom && sickFrom < devastatedFrom)) {
            throw new IllegalArgumentException("Thresholds must go up: 0 < tired < sick < devastated.");
        }
        PetState oldState = currentState;
        this.states = Arrays.asList(new HealthyState(tiredFrom), new TiredState(tiredFrom, sickFrom),
                new SickState(sickFrom, devastatedFrom), new DevastatedState(devastatedFrom));
        if (oldState != null) {
            for (PetState s : states) {
                if (s.getStateName().equals(oldState.getStateName())) currentState = s;
            }
        }
    }

    /**
     * Updates the state and health from the usage ratio.
     * @return true if the state changed (so a notification can be sent)
     */
    public boolean updateState(double ratio) {
        if (Double.isNaN(ratio) || ratio < 0) {
            throw new IllegalArgumentException("Ratio must be zero or greater.");
        }
        PetState next = null;
        for (PetState s : states) {
            if (s.matches(ratio)) { next = s; break; }   // polymorphic call
        }
        if (next == null) next = states.get(states.size() - 1);

        healthPoints = (int) Math.round(MAX_HEALTH * (1.0 - Math.min(ratio, 1.0)));
        boolean changed = !next.getStateName().equals(currentState.getStateName());
        currentState = next;
        return changed;
    }

    public String getAppearance() {
        return currentState.getAppearance();
    }

    public String getMessage() {
        return currentState.getMessage();
    }

    /** Back to full health and the Healthy state (new day). */
    public final void resetPet() {
        healthPoints = MAX_HEALTH;
        currentState = states.get(0);
    }

    @Override
    public String toString() {
        return name + " is " + currentState + " (HP " + healthPoints + "/" + MAX_HEALTH + ") "
                + currentState.getAppearance();
    }
}
