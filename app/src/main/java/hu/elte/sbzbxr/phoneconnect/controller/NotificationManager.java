package hu.elte.sbzbxr.phoneconnect.controller;

import android.app.Service;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

import hu.elte.sbzbxr.phoneconnect.model.notification.MyNotificationListenerService;

public class NotificationManager {
    private static final String LOG_TAG="NotificationManager";
    private final ServiceController serviceController;

    public NotificationManager(ServiceController controller){
        this.serviceController=controller;
    }

    private static boolean testForPermission(Service service){
        String notificationListenerString = Settings.Secure.getString(service.getContentResolver(),"enabled_notification_listeners");
        return !(notificationListenerString == null || !notificationListenerString.contains(service.getPackageName()));
    }

    public void start(Service service){
        //From:https://stackoverflow.com/questions/33566799/notificationlistenerservice-not-connecting-to-notification-manager
        if(!testForPermission(service)){
            requestNotificationListeningPermission(service);
        }
        if(!testForPermission(service)){Log.d(LOG_TAG, "User declined access to Notifications");return;}

        Intent intent = new Intent(service, MyNotificationListenerService.class);
        service.startService(intent);
        //service.bindService(intent,connection, Context.BIND_IMPORTANT);
    }

    /**
     *
     * @param service The service that initiates the stop
     * @return true if the NotificationService ran and successfully stopped, false otherwise
     */
    public boolean stop(Service service){
        if (notificationListenerService != null) {
            notificationListenerService.stop();
        }
        return service.stopService(new Intent(service,MyNotificationListenerService.class));
    }

    private static void requestNotificationListeningPermission(Service service) {
        Log.d(LOG_TAG, "Ask for access to Notifications");
        Intent requestIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        requestIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        service.startActivity(requestIntent);
    }

    @Nullable private MyNotificationListenerService notificationListenerService;

    public boolean isListening(){
        if(notificationListenerService==null) return false;
        return notificationListenerService.isConnectedToSystem();
    }

    public void setNotificationListenerService(@Nullable MyNotificationListenerService notificationListenerService) {
        this.notificationListenerService = notificationListenerService;
    }
}
