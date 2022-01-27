package hu.elte.sbzbxr.phoneconnect.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.io.File;

import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;
import hu.elte.sbzbxr.phoneconnect.model.notification.MyNotificationListenerService2;
import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;

public class ServiceController {
    private static final String LOG_TAG="ServiceController";
    private final MainActivity mainActivity;
    private ConnectionManager connectionManager;
    private ScreenCaptureBuilder screenCaptureBuilder;
    boolean connectionManagerIsBound = false;
    boolean notificationListenerIsBound = false;

    public ServiceController(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void startScreenCapture(int resultCode, Intent data){initScreenCapture();screenCaptureBuilder.start(resultCode,data);}
    public void stopScreenCapture(){screenCaptureBuilder.stop();}

    public void connectToServer(String ip, int port){connectionManager.connect(ip,port);}
    public void disconnectFromServer(){screenCaptureBuilder.stop();connectionManager.disconnect();}

    public void startNotificationListening(){
        if(notificationListenerIsBound){
            Log.d(LOG_TAG,"Request rebind");
            MyNotificationListenerService2.requestRebind(myNotiService);
        }

        //From:https://stackoverflow.com/questions/33566799/notificationlistenerservice-not-connecting-to-notification-manager
        String notificationListenerString = Settings.Secure.getString(mainActivity.getContentResolver(),"enabled_notification_listeners");
        //Check notifications access permission
        if (notificationListenerString == null || !notificationListenerString.contains(mainActivity.getPackageName())) {
            //The notification access has not acquired yet!
            Log.d(LOG_TAG, "no access");
            requestNotificationListeningPermission();
        }
        else {Log.d(LOG_TAG, "has access");}//Your application has access to the notifications

        if(!notificationListenerIsBound){
            Intent intent = new Intent(mainActivity, MyNotificationListenerService2.class);
            boolean b = mainActivity.bindService(intent, notificationServiceConnection, Context.BIND_AUTO_CREATE);
            Log.d(LOG_TAG,"Asked to bind and bindService returned: "+ b);
        }
    }

    private void requestNotificationListeningPermission() {
        Intent requestIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        requestIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainActivity.startActivity(requestIntent);
    }

    public void stopNotificationListening(){
        if(notificationListenerIsBound){
            mainActivity.unbindService(notificationServiceConnection);
            notificationListenerIsBound = false;
        }
    }

    public void sendPing(){connectionManager.sendPing();}

    @Deprecated
    public void sendOneSegment(){
        File fileToBeSent = new File(mainActivity.getApplicationContext().getFilesDir(),"PhoneC_14 Jan 2022 15_07_24__part1.mp4");
        connectionManager.sendFile(fileToBeSent.getPath());
    }

    private void initScreenCapture(){
        if(screenCaptureBuilder==null){screenCaptureBuilder=new ScreenCaptureBuilder(mainActivity);}
    }


    public void activityBindToConnectionManager(){
        if(!connectionManagerIsBound){
            Intent intent = new Intent(mainActivity, ConnectionManager.class);
            mainActivity.bindService(intent, networkServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void activityUnbindFromConnectionManager(){
        if(connectionManagerIsBound && connectionManager!=null){
            mainActivity.unbindService(networkServiceConnection);
            connectionManagerIsBound = false;
        }
    }

    private final ServiceConnection networkServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ConnectionManager.LocalBinder binder = (ConnectionManager.LocalBinder) service;
            connectionManager = binder.getService();
            connectionManager.setActivity(mainActivity);
            connectionManagerIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            connectionManagerIsBound = false;
        }
    };

    private ComponentName myNotiService;
    private final ServiceConnection notificationServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,IBinder service) {
            notificationListenerIsBound = true;
            myNotiService=className;
            Log.d(LOG_TAG,"NotiListener connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            notificationListenerIsBound = false;
        }

        @Override
        public void onNullBinding(ComponentName name) {
            Log.d(LOG_TAG,"Null binding");
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.d(LOG_TAG,"Binding died");
        }
    };
}
