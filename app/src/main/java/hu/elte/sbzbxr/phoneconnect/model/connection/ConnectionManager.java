package hu.elte.sbzbxr.phoneconnect.model.connection;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.elte.sbzbxr.phoneconnect.model.recording.ScreenShot;
import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;

public class ConnectionManager extends Service {
    private boolean isListening = false;
    private boolean isSending = false;
    private Socket socket;
    private PrintStream out;
    private InputStream in;
    private MainActivity view;//todo remove this
    private final ArrayBlockingQueue<ScreenShot> screenShotQueue = new ArrayBlockingQueue<ScreenShot>(10);

    private final IBinder binder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
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

    public ConnectionManager() {}
    public void setActivity(MainActivity mainActivity){this.view=mainActivity;}

    //Establish an http tcp connection
    //From: https://gist.github.com/teocci/0187ac32dcdbd57d8aaa89342be90f89
    public void connect(String ip, int port) {
        if (ip.equals("") || port == -1) {
            System.err.println("Invalid parameters");
            return;
        }
        // Connect to the server
        startAsyncTask(new ConnectionCreator2(out,in,ip,port,this));
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Tests whether the connection is valid
    public void sendPing(){
        startAsyncTask(new PingSender(out, this));
    }

    void connectRequestFinished(boolean successful,Socket s, String ip, int port, InputStream i, PrintStream o){
        socket=s;
        in=i;
        out=o;
        if(successful){
            view.showConnectedUI(ip,port);
            if (out == null) {
                System.err.println("But its null!!");
            }
            System.out.println("Successful connection establishment");
            isListening =true;
            isSending=true;
        }else{
            view.showFailMessage("Could not establish the connection!");
        }

        listen();
        startSendingThread();
    }

    private void listen(){
        new Thread(() -> {
            while (isListening) {
                try {
                    MyNetworkProtocolFrame frame = MyNetworkProtocolFrame.inputStreamToFrame(in);
                    switch (frame.getType()) {
                        case PROTOCOL_PING: pingRequestFinished(true,frame );break;
                        default: throw new RuntimeException("Unhandled type");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    disconnect();
                }
            }
        }).start();
    }

    void pingRequestFinished(boolean successful, MyNetworkProtocolFrame frame){
        view.runOnUiThread(() -> {
            if(successful){
                view.successfulPing(new String(frame.getData()));
                System.out.println("Successful ping! \nReceived message: "+new String(frame.getData()));
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
                try {
                    ScreenShot screenShot = screenShotQueue.take();
                    //bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
                    //String s = sendingQueue.take();
                    //FileSender2.send(out,s);
                    BitMapSender.send(out,screenShot);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Deprecated
    public void sendFile(String path){
        startAsyncTask(new FileSender(out,path));
    }

    public void sendScreenShot(ScreenShot screenShot) {
        if(!screenShotQueue.offer(screenShot)){
            screenShotQueue.clear();
            screenShotQueue.offer(screenShot);
            System.err.println("Bitmap queue overflown -> cleared");
        }
    }
}
