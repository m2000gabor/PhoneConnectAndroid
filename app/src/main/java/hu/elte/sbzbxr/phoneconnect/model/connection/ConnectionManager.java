package hu.elte.sbzbxr.phoneconnect.model.connection;

import static hu.elte.sbzbxr.phoneconnect.ui.PickLocationActivity.FILENAME_TO_CREATE;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.elte.sbzbxr.phoneconnect.model.MyFileDescriptor;
import hu.elte.sbzbxr.phoneconnect.model.connection.buffer.OutgoingBuffer2;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.NetworkFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.NetworkFrameCreator;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.NotificationFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.PingFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.ScreenShotFrame;
import hu.elte.sbzbxr.phoneconnect.model.recording.ScreenShot;
import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;
import hu.elte.sbzbxr.phoneconnect.ui.PickLocationActivity;

/**
 * All connection related action starts from here.
 * Manages the different types of outgoing and ingoing requests.
 * Establish and destroy the connection with the Windows side server app.
 */
public class ConnectionManager extends Service {
    private static final String LOG_TAG = "ConnectionManager";
    private boolean isListening = false;
    private boolean isSending = false;
    private Socket socket;
    private PrintStream out;
    private InputStream in;
    private MainActivity view;//todo remove this
    private final OutgoingBuffer2 outgoingBuffer=new OutgoingBuffer2();

    private final IBinder binder = new LocalBinder();

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public ConnectionManager getService() {
            // Return this instance of LocalService so clients can call public methods
            return ConnectionManager.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //Empty ctor is required for a Service.
    public ConnectionManager() {}

    public void setActivity(MainActivity mainActivity){this.view=mainActivity;}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try{
            Uri uri = (Uri) intent.getParcelableExtra(PickLocationActivity.URI_OF_FILE);
            String filename = intent.getStringExtra(FILENAME_TO_CREATE);
            if(uri !=null){
                openOutputStream(filename,uri);
            }
        }catch (ClassCastException e){
            Log.d(LOG_TAG,"not a Uri");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    //
    //From: https://gist.github.com/teocci/0187ac32dcdbd57d8aaa89342be90f89

    /**
     * Asynchronously establish a tcp connection with the given server
     */
    public void connect(String ip, int port) {
        if (ip.equals("") || port == -1) {
            System.err.println("Invalid parameters");
            return;
        }
        // Connect to the server
        startAsyncTask(new ConnectionCreator(out,in,ip,port,this));
    }

    //From: https://www.simplifiedcoding.net/android-asynctask/
    //And: https://stackoverflow.com/questions/58767733/android-asynctask-api-deprecating-in-android-11-what-are-the-alternatives
    private void startAsyncTask(RunnableWithHandler runInBackground) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        runInBackground.setHandler(handler);

        executor.execute(runInBackground);
    }

    public void disconnect(){
        try {
            isListening =false;
            isSending =false;
            if(in==null || out==null || socket==null){return;}
            in.close();
            out.close();
            socket.close();
            view.afterDisconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Tests whether the connection is valid
    public void sendPing(){
        outgoingBuffer.forceInsert(new PingFrame("Hello server"));
    }

    /**
     * Invoked when a connection asynchronously established
     */
    void connectRequestFinished(boolean successful,Socket s, String ip, int port, InputStream i, PrintStream o){
        socket=s;
        in=i;
        out=o;
        if(successful){
            view.connectedTo(ip,port);
            if (out == null) {
                System.err.println("But its null!!");
            }
            System.out.println("Successful connection establishment");
            isListening =true;
            isSending=true;
            listen();
            startSendingThread();
        }else{
            view.showFailMessage("Could not establish the connection!");
        }
    }

    /**
     * Starts a new Thread, which listens the input side of the socket
     */
    private void listen(){
        new Thread(() -> {
            while (isListening) {
                try {
                    FrameType type = NetworkFrameCreator.getType(in);
                    if (type == FrameType.INVALID) {disconnect();}
                    switch (type) {
                        case PING:
                            pingRequestFinished(true, PingFrame.deserialize(in));
                            break;
                        case FILE:
                            fileArrived(FileFrame.deserialize(type,in));
                            break;
                        default:
                            throw new RuntimeException("Unhandled type");
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    disconnect();
                }
            }
        }).start();
    }

    private final FileOutputStreamProvider streamProvider = new FileOutputStreamProvider();
    private void fileArrived(FileFrame fileFrame) {
        String name = fileFrame.name;
        if(name==null) {
            Log.e(LOG_TAG,"This frame doesn't have a name");
            return;
        }
        OutputStream os = streamProvider.getOutputStream(this, name);
        if(fileFrame.getDataLength()==0){
            final String tmp = fileFrame.name;
            Log.d(LOG_TAG,"File arrived: "+tmp);
            view.runOnUiThread(()-> Toast.makeText(getApplicationContext(),"File arrived: "+tmp,Toast.LENGTH_SHORT).show());
            streamProvider.endOfFileStreaming(name);
        }else{
            writeThisFrame(os,fileFrame);
        }
    }

    private static void writeThisFrame(OutputStream os, FileFrame frame){
        try {
            os.write(frame.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void askForSaveLocation(String filename){
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), PickLocationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FILENAME_TO_CREATE,filename);
        startActivity(intent);
    }

    private void openOutputStream(String filename, Uri uri){
        try {
            OutputStream fileSavingOutputStream = getContentResolver().openOutputStream(uri,"w");
            streamProvider.onStreamCreated(filename,fileSavingOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("PickLocationActivity","No access to this location; cannot save the file");
        }
    }


    void pingRequestFinished(boolean successful, PingFrame pingFrame){
        if (pingFrame.invalid()){disconnect();}
        view.runOnUiThread(() -> {
            if(successful){
                view.successfulPing(pingFrame.name);
                System.out.println("Successful ping! \nReceived message: "+ pingFrame.name);
            }else{
                disconnect();
                view.showFailMessage("Ping failed! Disconnected!");
                view.afterDisconnect();
            }
        });
    }

    private void startSendingThread(){
        new Thread(() ->{
            while(isSending){
                NetworkFrame sendable = outgoingBuffer.take();
                if(sendable!=null){
                    FrameSender.send(out,sendable);
                }else{
                    Log.d(LOG_TAG,"Got null from buffer");
                }
            }
        }).start();
    }

    public void sendNotification(NotificationFrame n) {
        if(n != null){
            try{
                outgoingBuffer.forceInsert(n);
                Log.d(LOG_TAG,"Notification queued");
            }catch(IllegalStateException e){
                Log.e(LOG_TAG,"The notification queue is full. Cannot add latest notification.");
            }
        }else{
            Log.d(LOG_TAG,"Cannot send >>null<< notification");
        }
    }


    private final ExecutorService fileCutterExecutorService = Executors.newSingleThreadExecutor();
    public void sendFile(MyFileDescriptor myFileDescriptor){
        Log.d(LOG_TAG,"Would send the following file: "+ myFileDescriptor.filename);
        fileCutterExecutorService.submit(()->{
            FileCutter cutter = new FileCutter(myFileDescriptor,getContentResolver());
            while (!cutter.isEnd()){
                outgoingBuffer.forceInsert(cutter.current());
                cutter.next();
            }
        });
    }

    public void sendScreenShot(ScreenShot screenShot) {
        outgoingBuffer.forceInsert(new ScreenShotFrame(screenShot));
    }
}
