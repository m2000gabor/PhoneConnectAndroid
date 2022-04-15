package hu.elte.sbzbxr.phoneconnect.model.notification;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.Nullable;

import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.NotificationFrame;

public class MyNotificationListenerService extends NotificationListenerService {
    private static final String LOG_TAG = "NotificationListener";
    @Nullable private ConnectionManager connectionManager;
    private boolean isListening=false;

    private NotificationFrame getUsefulData(StatusBarNotification notification){
        CharSequence title =notification.getNotification().extras.getCharSequence("android.title");
        CharSequence text =notification.getNotification().extras.getCharSequence("android.text");
        CharSequence appName = getAppName(notification.getPackageName());
        if(title == null){title="unknown title";}
        if(text == null){text="unknown text";}
        if(appName == null){appName="unknown app";}
        return new NotificationFrame(title,text,appName);
    }

    private void sendNotification(StatusBarNotification sbn){
        if(connectionManager !=null){
            connectionManager.sendNotification(getUsefulData(sbn));
        }else{
            Log.d(LOG_TAG,"ConnectionManager is null, but a new notification is posted");
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if(sbn.isOngoing()){return;}
        Log.v(LOG_TAG,"Just get posted: "+sbn.getNotification().toString());
        sendNotification(sbn);
        super.onNotificationPosted(sbn);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(LOG_TAG,"NotificationListener started");
        return super.onStartCommand(intent, flags, startId);
    }

    //From: https://stackoverflow.com/questions/5841161/get-application-name-from-package-name/29513147
    private String getAppName(String packageName){
        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo( packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    }

    @Override
    public void onListenerConnected() {
        Log.v(LOG_TAG,"NotificationListener connected");
        isListening=true;
        super.onListenerConnected();

        for(StatusBarNotification notification : getActiveNotifications()){
            sendNotification(notification);
        }
    }

    @Override
    public void onListenerDisconnected() {
        Log.v(LOG_TAG,"NotificationListener disconnected");
        isListening=false;
        super.onListenerDisconnected();
    }

    private final IBinder binder = new MyNotificationListenerService.LocalBinder();
    public class LocalBinder extends Binder {
        public MyNotificationListenerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MyNotificationListenerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG,"NotificationListener bond");
        return binder;
    }

    public void addConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public boolean isListening(){
        return isListening;
    }
}
