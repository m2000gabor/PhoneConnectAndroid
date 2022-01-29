package hu.elte.sbzbxr.phoneconnect.controller;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import hu.elte.sbzbxr.phoneconnect.model.notification.MyNotificationListenerService;

public class NotificationManager {
    private static final String LOG_TAG="NotificationManager";
    private static final Class<MyNotificationListenerService> notificationService = MyNotificationListenerService.class;

    private NotificationManager(){}

    private static boolean testForPermission(Activity activity){
        String notificationListenerString = Settings.Secure.getString(activity.getContentResolver(),"enabled_notification_listeners");
        return !(notificationListenerString == null || !notificationListenerString.contains(activity.getPackageName()));
    }

    public static boolean start(Activity activity){
        //From:https://stackoverflow.com/questions/33566799/notificationlistenerservice-not-connecting-to-notification-manager
        if(!testForPermission(activity)){
            requestNotificationListeningPermission(activity);
        }
        if(!testForPermission(activity)){Log.d(LOG_TAG, "User declined access to Notifications");return false;}
        Intent intent = new Intent(activity, notificationService);
        return activity.startService(intent) != null;
    }

    /**
     *
     * @param activity
     * @return true if the NotificationService ran and successfully stopped, false otherwise
     */
    public static boolean stop(Activity activity){
        return activity.stopService(new Intent(activity,notificationService));
    }

    private static void requestNotificationListeningPermission(Activity activity) {
        Log.d(LOG_TAG, "Ask for access to Notifications");
        Intent requestIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        requestIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(requestIntent);
    }
}
