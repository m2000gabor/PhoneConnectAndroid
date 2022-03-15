package hu.elte.sbzbxr.phoneconnect.model.notification;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;

public class MyNotificationListenerService extends NotificationListenerService {
    private static final String LOG_TAG = "NotificationListener";
    private ConnectionManager connectionManager;

    private SendableNotification getUsefulData(StatusBarNotification notification){
        CharSequence title =notification.getNotification().extras.getCharSequence("android.title");
        CharSequence text =notification.getNotification().extras.getCharSequence("android.text");
        CharSequence appName = getAppName(notification.getPackageName());
        if(title == null){title="unknown title";}
        if(text == null){text="unknown text";}
        if(appName == null){appName="unknown app";}
        return new SendableNotification(title,text,appName);
    }

    private void sendNotification(StatusBarNotification sbn){
        if(connectionManager!=null){
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

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG,"NotificationListener bond");
        return super.onBind(intent);
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
        super.onListenerConnected();
        //Toast.makeText(getApplicationContext(), "NotificationListener connected", Toast.LENGTH_SHORT).show();
        if(!mBound){
            Intent i = new Intent(this, ConnectionManager.class);
            bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }

        for(StatusBarNotification notification : getActiveNotifications()){
            sendNotification(notification);
        }
    }

    @Override
    public void onListenerDisconnected() {
        Log.v(LOG_TAG,"NotificationListener disconnected");
        super.onListenerDisconnected();
    }


    boolean mBound=false;
    private final ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            ConnectionManager.LocalBinder binder = (ConnectionManager.LocalBinder) service;
            connectionManager = binder.getService();
            mBound = true;
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e("ScreenCapture", "onServiceDisconnected");
            mBound = false;
        }
    };
}