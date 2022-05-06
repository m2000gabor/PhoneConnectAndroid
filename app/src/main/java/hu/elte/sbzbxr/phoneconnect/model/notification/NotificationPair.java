package hu.elte.sbzbxr.phoneconnect.model.notification;

public class NotificationPair {
    public String app;
    public boolean enabled;

    public NotificationPair(String app, boolean enabled) {
        this.app = app;
        this.enabled = enabled;
    }
}
