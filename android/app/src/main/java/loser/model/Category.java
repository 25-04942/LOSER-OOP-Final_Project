package loser.model;

/**
 * App categories used to group screen time.
 */
public enum Category {
    SOCIAL("Social media"),
    GAMES("Games"),
    STUDY("Study"),
    COMMUNICATION("Communication"),
    OTHER("Other");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
