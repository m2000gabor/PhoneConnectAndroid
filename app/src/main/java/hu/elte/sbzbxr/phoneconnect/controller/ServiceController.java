package hu.elte.sbzbxr.phoneconnect.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.io.File;

import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;
import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;

/**
 * Responsible to start and stop services from mainActivity. Changes the services' lifecycle state.
 * It's more likely to be a collection of the individual service manager classes, than
 * an ultimate responsible for all service work class.
 *
 * //todo transform this class to a viewModel, to be independent form the activity lifecycle?
 */
public class ServiceController {
    private static final String LOG_TAG="ServiceController";
    private final MainActivity mainActivity;
    private ConnectionManager connectionManager;
    private ScreenCaptureBuilder screenCaptureBuilder;
    boolean connectionManagerIsBound = false;


    public ServiceController(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void startScreenCapture(int resultCode, Intent data){initScreenCapture();screenCaptureBuilder.start(resultCode,data);}
    public void stopScreenCapture(){screenCaptureBuilder.stop();}

    public boolean connectToServer(String ip, int port){
        boolean valid =validate_ip_port();
        if(valid){connectionManager.connect(ip,port);}
        return valid;
    }

    //todo implement
    private boolean validate_ip_port() {
        return true;
    }

    public void disconnectFromServer(){
        if(screenCaptureBuilder!=null){screenCaptureBuilder.stop();}
        connectionManager.disconnect();}

    private void startNotificationListening(){NotificationManager.start(mainActivity);}
    private void stopNotificationListening(){NotificationManager.stop(mainActivity);}

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
        Intent intent = new Intent(mainActivity, ConnectionManager.class);
        if(connectionManager==null){mainActivity.startService(intent);}//if this is the first call, we need to start the service
        if(!connectionManagerIsBound){
            mainActivity.bindService(intent, networkServiceConnection, Context.BIND_IMPORTANT);
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
            startNotificationListening();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            connectionManagerIsBound = false;
            stopNotificationListening();
        }
    };
}
