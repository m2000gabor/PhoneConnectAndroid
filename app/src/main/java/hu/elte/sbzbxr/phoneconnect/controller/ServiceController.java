package hu.elte.sbzbxr.phoneconnect.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.io.File;

import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;
import hu.elte.sbzbxr.phoneconnect.model.recording.ScreenCapture;
import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;

public class ServiceController {
    private final MainActivity mainActivity;
    private ConnectionManager connectionManager;
    private ScreenCaptureBuilder screenCaptureBuilder;
    boolean mBound = false;

    public ServiceController(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void startScreenCapture(int resultCode, Intent data){initScreenCapture();screenCaptureBuilder.start(resultCode,data);}
    public void stopScreenCapture(){screenCaptureBuilder.stop();}

    public void connectToServer(String ip, int port){connectionManager.connect(ip,port);}
    public void disconnectFromServer(){screenCaptureBuilder.stop();connectionManager.disconnect();}

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
        if(!mBound){
            Intent intent = new Intent(mainActivity, ConnectionManager.class);
            mainActivity.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    public void activityUnbindFromConnectionManager(){
        if(mBound && connectionManager!=null){
            mainActivity.unbindService(connection);
            mBound = false;
        }
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ConnectionManager.LocalBinder binder = (ConnectionManager.LocalBinder) service;
            connectionManager = binder.getService();
            connectionManager.setActivity(mainActivity);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
