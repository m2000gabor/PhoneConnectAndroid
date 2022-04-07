package hu.elte.sbzbxr.phoneconnect.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.net.Socket;
import java.util.Collections;
import java.util.List;

import hu.elte.sbzbxr.phoneconnect.model.persistance.MyFileDescriptor;
import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionLimiter;
import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message.MessageFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message.MessageType;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message.PingMessageFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message.StartRestoreMessageFrame;
import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;

/**
 * Responsible to start and stop services from mainActivity. Changes the services' lifecycle state.
 * It's more likely to be a collection of the individual service manager classes, than
 * an ultimate responsible for all service work class.
 *
 */
public class ServiceController {
    private static final String LOG_TAG="ServiceController";
    private ConnectionManager connectionManager;
    private ScreenCaptureBuilder screenCaptureBuilder;
    boolean connectionManagerIsBound = false;
    private final MainViewModel viewModel;


    public ServiceController(MainViewModel viewModel) {
        this.viewModel=viewModel;
    }

    public void startRealScreenCapture(int resultCode, Intent data, MainActivity mainActivity){
        connectionManager.sendMessage(new MessageFrame(MessageType.START_OF_STREAM));
        if(screenCaptureBuilder==null){screenCaptureBuilder=new ScreenCaptureBuilder(mainActivity);}
        screenCaptureBuilder.startRealLive(resultCode,data);
    }
    public void startDemoScreenCapture(MainActivity mainActivity){
        if(screenCaptureBuilder==null){screenCaptureBuilder=new ScreenCaptureBuilder(mainActivity);}
        screenCaptureBuilder.startDemo();
    }
    public void stopScreenCapture(){
        if(screenCaptureBuilder != null) screenCaptureBuilder.stop();
        connectionManager.sendMessage(new MessageFrame(MessageType.END_OF_STREAM));
    }

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
        connectionManager.disconnect();
    }

    public void startNotificationListening(MainActivity mainActivity){NotificationManager.start(mainActivity);}
    public void stopNotificationListening(MainActivity mainActivity){NotificationManager.stop(mainActivity);}

    public void sendPing(){connectionManager.sendMessage(new PingMessageFrame("Hello server"));}
    public void askRestoreList(){connectionManager.sendMessage(new MessageFrame(MessageType.RESTORE_GET_AVAILABLE));}
    public void requestRestore(String restoreID){connectionManager.sendMessage(new StartRestoreMessageFrame(restoreID));}

    public void activityBindToConnectionManager(MainActivity mainActivity){
        Intent intent = new Intent(mainActivity, ConnectionManager.class);
        if(connectionManager==null){mainActivity.startService(intent);}//if this is the first call, we need to start the service
        if(!connectionManagerIsBound){
            mainActivity.bindService(intent, networkServiceConnection, Context.BIND_IMPORTANT);
        }
    }

    public void activityUnbindFromConnectionManager(MainActivity mainActivity){
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
            connectionManager.setViewModel(viewModel);
            connectionManagerIsBound = true;
            //startNotificationListening();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            connectionManagerIsBound = false;
        }
    };


    public void startFileTransfer(MyFileDescriptor myFileDescriptor) {
        //Log.d("To send",myFileDescriptor.filename);
        connectionManager.sendFiles(Collections.singletonList(myFileDescriptor), FrameType.FILE,null,0);
    }
    ///document/primary:Download/PhoneConnect/kb_jk_igazolas_december30.pdf
    ///external/images/media/112

    public void sendBackupFiles(List<MyFileDescriptor> myFileDescriptors, String backupId, Long folderSize) {
        //Log.d("To send",myFileDescriptor.filename);
        connectionManager.sendFiles(myFileDescriptors, FrameType.BACKUP_FILE,backupId,folderSize);
    }

    /**
     *
     * @return the socket if connected, null otherwise
     */
    public Socket isConnected(){
        if(connectionManager ==null){
            return null;
        }else{
         return connectionManager.getSocket();
        }
    }

    public void setNetworkLimit(ConnectionLimiter limiter){
        if(connectionManager!=null){
            connectionManager.setLimiter(limiter);
        }
    }
}
