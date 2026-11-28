package loser.exception;

/** Thrown when a setting (daily limit, reset time...) has an invalid value. */
public class InvalidSettingsException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidSettingsException(String message) {
        super(message);
    }
}
