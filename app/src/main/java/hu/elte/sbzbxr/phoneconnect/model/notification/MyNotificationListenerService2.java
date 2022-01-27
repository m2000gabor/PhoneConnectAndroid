package hu.elte.sbzbxr.phoneconnect.model.notification;

import android.app.job.JobScheduler;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;

public class MyNotificationListenerService2 extends NotificationListenerService {
    private static final String LOG_TAG = "NotificationListener";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.v(LOG_TAG,"Just get posted: "+sbn.getNotification().toString());
        super.onNotificationPosted(sbn);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG,"listener binded");

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
        Log.v(LOG_TAG,"listener connected");
        Toast.makeText(getApplicationContext(), "notiListener connected", Toast.LENGTH_SHORT).show();
        super.onListenerConnected();
        for(StatusBarNotification notification : getActiveNotifications()){
            Log.d(LOG_TAG,"Notification: " + notification.toString()+"\n");
            CharSequence title =notification.getNotification().extras.getCharSequence("android.title");
            CharSequence text =notification.getNotification().extras.getCharSequence("android.text");
            CharSequence packageName = getAppName(notification.getPackageName());
            if(title != null){Log.v(LOG_TAG,"Notification title is: "+ title);}else{Log.v(LOG_TAG,"Notification title is: null");}
            if(text != null){Log.v(LOG_TAG,"Notification text is: " + text);}else{Log.v(LOG_TAG,"Notification text is: null");}
            if(packageName != null){Log.v(LOG_TAG,"From app: " + packageName);}else{Log.v(LOG_TAG,"From app: null");}
            Log.v(LOG_TAG,"-------------------\n\n");
        }
    }

    @Override
    public void onListenerDisconnected() {
        Log.v(LOG_TAG,"listener disconnected");
        super.onListenerDisconnected();
    }
}
