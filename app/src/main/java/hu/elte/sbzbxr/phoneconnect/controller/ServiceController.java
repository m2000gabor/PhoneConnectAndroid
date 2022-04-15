package hu.elte.sbzbxr.phoneconnect.controller;

import static hu.elte.sbzbxr.phoneconnect.ui.PickLocationActivity.CHOOSE_FOLDER;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import hu.elte.sbzbxr.phoneconnect.model.persistance.MyFileDescriptor;
import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionLimiter;
import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message.MessageFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message.MessageType;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message.PingMessageFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message.StartRestoreMessageFrame;
import hu.elte.sbzbxr.phoneconnect.ui.ConnectedFragmentUIData;
import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;

/**
 * Responsible to start and stop services from mainActivity. Changes the services' lifecycle state.
 * It's more likely to be a collection of the individual service manager classes, than
 * an ultimate responsible for all service work class.
 */
public class ServiceController extends Service {
    private static final String LOG_TAG = "ServiceController";
    private final ConnectionManager connectionManager = new ConnectionManager(this);
    private final ScreenCaptureManager screenCaptureManager = new ScreenCaptureManager(this);
    private final NotificationManager notificationManager = new NotificationManager(this);

    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public ServiceController getService() {
            // Return this instance of LocalService so clients can call public methods
            return ServiceController.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private Notification notification;
    @Override
    public void onCreate() {
        super.onCreate();
        //For handler and looper and multi-thread: https://developer.android.com/guide/components/services
        createNotificationChannel();
        Intent intent1 = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, 0);

        notification = new NotificationCompat.Builder(this, "ServiceController")
                .setContentTitle("Phone Connect")
                .setContentText("Running...")
                .setContentIntent(pendingIntent).build();


        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel("ServiceController", "Service controller",
                android.app.NotificationManager.IMPORTANCE_HIGH);
        android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "serviceController started");
        if(Objects.equals(intent.getAction(), CHOOSE_FOLDER)){
            connectionManager.folderChosen(intent, flags, startId);
        }
        return START_STICKY; // If we get killed, after returning from here, restart
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "serviceController destroyed");
    }

    public void refreshData(MainViewModel viewModel) {
        connectionManager.setViewModel(viewModel);
        viewModel.refreshData(this);
    }

    public void startRealScreenCapture(int resultCode, Intent data, MainActivity mainActivity) {
        connectionManager.sendMessage(new MessageFrame(MessageType.START_OF_STREAM));
        screenCaptureManager.startRealLive(mainActivity, resultCode, data);
    }

    public void startDemoScreenCapture(MainActivity mainActivity) {
        screenCaptureManager.startDemo(this,mainActivity);
    }

    public void stopScreenCapture() {
        screenCaptureManager.stop();
        connectionManager.sendMessage(new MessageFrame(MessageType.END_OF_STREAM));
    }

    public boolean connectToServer(String ip, int port) {
        boolean valid = validate_ip_port();
        if (valid) {
            connectionManager.connect(ip, port);
        }
        return valid;
    }

    //todo implement
    private boolean validate_ip_port() {
        return true;
    }

    public void disconnectFromServer() {
        screenCaptureManager.stop();
        connectionManager.disconnect();
    }

    public void startNotificationListening(MainActivity mainActivity) {
        notificationManager.start(this);
    }

    public void stopNotificationListening(MainActivity mainActivity) {
        notificationManager.stop(this);
    }

    public void sendPing() {
        connectionManager.sendMessage(new PingMessageFrame("Hello server"));
    }

    public void askRestoreList() {
        connectionManager.sendMessage(new MessageFrame(MessageType.RESTORE_GET_AVAILABLE));
    }

    public void requestRestore(String restoreID) {
        connectionManager.sendMessage(new StartRestoreMessageFrame(restoreID));
    }

    public void startFileTransfer(MyFileDescriptor myFileDescriptor) {
        connectionManager.sendFiles(Collections.singletonList(myFileDescriptor), FrameType.FILE, null, 0);
    }
    ///document/primary:Download/PhoneConnect/kb_jk_igazolas_december30.pdf
    ///external/images/media/112

    public void sendBackupFiles(List<MyFileDescriptor> myFileDescriptors, String backupId, Long folderSize) {
        //Log.d("To send",myFileDescriptor.filename);
        connectionManager.sendFiles(myFileDescriptors, FrameType.BACKUP_FILE, backupId, folderSize);
    }

    /**
     * @return the socket if connected, null otherwise
     */
    public Socket isConnected() {
        return connectionManager.getSocket();
    }

    public void setNetworkLimit(ConnectionLimiter limiter) {
        connectionManager.setLimiter(limiter);
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public ScreenCaptureManager getScreenCaptureManager() {
        return screenCaptureManager;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public Notification getNotification() {
        return notification;
    }

    public ConnectedFragmentUIData getConnectedUIData(){
        Socket s = isConnected();
        String ip = null;
        String port = null;
        if(s!=null){
            ip=s.getInetAddress().getHostAddress();
            port = String.valueOf(s.getPort());
        }
        return new ConnectedFragmentUIData(
                ip,
                port,
                screenCaptureManager.isRunning(),
                notificationManager.isListening(),
                false,
                false
        );
    }
}
