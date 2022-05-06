package hu.elte.sbzbxr.phoneconnect.ui.notifications;

import java.util.List;

import hu.elte.sbzbxr.phoneconnect.model.notification.NotificationPair;

public interface SaveList {
    void saveNotificationPairs(List<NotificationPair> list);
}
