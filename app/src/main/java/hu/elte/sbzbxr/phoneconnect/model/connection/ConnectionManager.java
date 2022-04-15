package hu.elte.sbzbxr.phoneconnect.model.connection;

import static hu.elte.sbzbxr.phoneconnect.ui.PickLocationActivity.FILENAME_TO_CREATE;
import static hu.elte.sbzbxr.phoneconnect.ui.PickLocationActivity.FOLDERNAME_TO_CREATE;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.elte.sbzbxr.phoneconnect.controller.MainViewModel;
import hu.elte.sbzbxr.phoneconnect.model.actions.Action_FailedToConnect;
import hu.elte.sbzbxr.phoneconnect.model.persistance.FileInFolderDescriptor;
import hu.elte.sbzbxr.phoneconnect.model.persistance.MyFileDescriptor;
import hu.elte.sbzbxr.phoneconnect.model.actions.arrived.Action_FilePieceArrived;
import hu.elte.sbzbxr.phoneconnect.model.actions.arrived.Action_LastPieceOfFileArrived;
import hu.elte.sbzbxr.phoneconnect.model.actions.arrived.Action_PingArrived;
import hu.elte.sbzbxr.phoneconnect.model.actions.arrived.Action_RestoreListAvailable;
import hu.elte.sbzbxr.phoneconnect.model.actions.networkstate.Action_NetworkStateConnected;
import hu.elte.sbzbxr.phoneconnect.model.actions.networkstate.Action_NetworkStateDisconnected;
import hu.elte.sbzbxr.phoneconnect.model.actions.sent.Action_FilePieceSent;
import hu.elte.sbzbxr.phoneconnect.model.actions.sent.Action_LastPieceOfFileSent;
import hu.elte.sbzbxr.phoneconnect.model.connection.buffer.OutgoingBuffer;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.FileCutter;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.BackupFileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.NetworkFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.NetworkFrameCreator;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.NotificationFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message.MessageFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message.MessageType;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message.PingMessageFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message.RestorePostMessageFrame;
import hu.elte.sbzbxr.phoneconnect.model.recording.ScreenShot;
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
    private ConnectionLimiter limiter = ConnectionLimiter.noLimit();
    private Socket socket;
    private BufferedOutputStream out;
    private InputStream in;
    private final OutgoingBuffer outgoingBuffer=new OutgoingBuffer();
    private MainViewModel viewModel;
    private final FileOutputStreamProvider streamProvider = new FileOutputStreamProvider(this);
    private final Context context;

    public ConnectionManager(Context context) {
        this.context=context;
    }

    public void folderChosen(Intent intent, int flags, int startId) {
        try{
            Uri uri = intent.getParcelableExtra(PickLocationActivity.URI_OF_FILE);
            if(uri !=null){
                String filename = intent.getStringExtra(FILENAME_TO_CREATE);
                String folderName = intent.getStringExtra(FOLDERNAME_TO_CREATE);
                streamProvider.createStream(new FileInFolderDescriptor(filename,folderName),uri);
            }
        }catch (ClassCastException e){
            Log.d(LOG_TAG,"not a Uri");
        }
    }

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
            if(in!=null) in.close();
            if(out!=null) out.close();
            if(socket!=null) socket.close();
            viewModel.postAction(new Action_NetworkStateDisconnected());
            outgoingBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Tests whether the connection is valid
    public void sendMessage(MessageFrame frame){
        outgoingBuffer.forceInsert(frame);
    }

    /**
     * Invoked when a connection asynchronously established
     */
    void connectRequestFinished(boolean successful, Socket s, String ip, int port, InputStream i, BufferedOutputStream o){
        if(!successful){
            viewModel.postAction(new Action_FailedToConnect(ip+":"+port));
        }else{
            socket=s;
            in=i;
            out=o;
            viewModel.postAction(new Action_NetworkStateConnected(ip,port));
            if (out == null) {
                System.err.println("But its null!!");
            }
            System.out.println("Successful connection establishment");
            isListening =true;
            isSending=true;
            listen();
            startSendingThread();
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
                        case INTERNAL_MESSAGE:
                            messageArrived(in);
                            break;
                        case RESTORE_FILE:
                            fileArrived(BackupFileFrame.deserialize(type,in));
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

    private void fileArrived(FileFrame fileFrame) {
        if(fileFrame.filename==null) {
            Log.e(LOG_TAG,"This frame doesn't have a name");
            return;
        }
        final FileInFolderDescriptor desc = new FileInFolderDescriptor(fileFrame.filename,fileFrame.folderName);
        if(fileFrame.getDataLength()==0){
            Log.d(LOG_TAG,"File arrived: "+desc.toString());
            viewModel.postAction(new Action_LastPieceOfFileArrived(fileFrame));
            streamProvider.endOfFileStreaming(desc);
        }else{
            OutputStream os = streamProvider.getOutputStream(desc);
            saveFrame(os,fileFrame);
            viewModel.postAction(new Action_FilePieceArrived(fileFrame));
        }
    }

    private static void saveFrame(OutputStream os, FileFrame frame){
        try {
            os.write(frame.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void messageArrived(InputStream in) throws IOException{
        MessageFrame messageFrame = MessageFrame.deserialize(in);
        MessageType type = messageFrame.messageType;
        switch (type){
            case PING: pingArrived(PingMessageFrame.deserialize(in)); break;
            case RESTORE_POST_AVAILABLE: restoreFolderListArrived(RestorePostMessageFrame.deserialize(in)); break;
            default: System.err.println("Unknown messageType"); break;
        }
    }

    private void restoreFolderListArrived(RestorePostMessageFrame messageFrame){
        viewModel.postAction(new Action_RestoreListAvailable(messageFrame.getBackups()));
        System.out.println("Available restores arrived! ");
    }

    private void pingArrived(PingMessageFrame messageFrame){
        viewModel.postAction(new Action_PingArrived(messageFrame.message));
        System.out.println("Successful ping! \nReceived message: "+ messageFrame.message);
    }

    private void startSendingThread(){
        new Thread(() ->{
            while(isSending){
                NetworkFrame sendable = outgoingBuffer.take();
                if(sendable!=null){
                    FrameSender.send(limiter,out,sendable);
                }else{
                    Log.d(LOG_TAG,"Got null from buffer");
                }
            }
            limiter.stop();
        }).start();
    }

    public void sendNotification(NotificationFrame n) {
        if(n==null){Log.d(LOG_TAG,"Cannot send >>null<< notification");return;}
        if(viewModel.notificationFilter.toForward(n.appName)){
            try{
                outgoingBuffer.forceInsert(n);
                Log.d(LOG_TAG,"Notification queued");
            }catch(IllegalStateException e){
                Log.e(LOG_TAG,"The notification queue is full. Cannot add latest notification.");
            }
        }else{
            Log.e(LOG_TAG,"This notification is filtered out. AppName: "+n.appName);
        }
    }


    private final ExecutorService fileCutterExecutorService = Executors.newSingleThreadExecutor();
    public void sendFiles(List<MyFileDescriptor> files, FrameType fileType, String backupId, long folderSize){
        fileCutterExecutorService.submit(()->{
            for(MyFileDescriptor myFileDescriptor : files){
                Log.d(LOG_TAG,"Would send the following file: "+ myFileDescriptor.filename);
                FileCutter cutter = FileCutterCreator.create(myFileDescriptor,getContext().getContentResolver(),fileType, backupId,folderSize);

                //sending
                while (!cutter.isEnd()){
                    outgoingBuffer.forceInsert(cutter.current());

                    //notify ui that a piece of file sent
                    viewModel.postAction(new Action_FilePieceSent(cutter.current()));

                    cutter.next();
                }

                //notify ui that file sending completed
                viewModel.postAction(new Action_LastPieceOfFileSent(cutter.current()));
            }
        });
    }

    private final ExecutorService compressToJPGExecutorService = Executors.newFixedThreadPool(4);
    public void sendScreenShot(ScreenShot screenShot) {
        ScreenShotFrame screenShotFrame = new ScreenShotFrame(screenShot);
        outgoingBuffer.forceInsert(screenShotFrame);
        compressToJPGExecutorService.submit(screenShotFrame::transform);
    }

    public Socket getSocket() {
        return socket;
    }

    public void setLimiter(ConnectionLimiter l) {
        limiter.stop();
        limiter = l;
        limiter.start();
    }

    public void setViewModel(MainViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public Context getContext() {
        return context;
    }
}
