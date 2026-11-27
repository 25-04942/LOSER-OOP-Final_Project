package loser.exception;

/** Thrown when the app cannot read usage data because Usage Access is not granted. */
public class MissingPermissionException extends Exception {
    private static final long serialVersionUID = 1L;

    public MissingPermissionException() {
        super("Usage access permission is required. Enable it in Settings > Usage access.");
    }

    public MissingPermissionException(String message) {
        super(message);
    }
}
