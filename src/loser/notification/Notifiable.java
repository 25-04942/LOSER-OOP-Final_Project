package loser.notification;

/** Anything that can deliver a notification message. */
public interface Notifiable {
    void sendNotification(String message);
}
