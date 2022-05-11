package hu.elte.sbzbxr.phoneconnect.model.connection;

import static hu.elte.sbzbxr.phoneconnect.ui.PickLocationActivity.FILENAME_TO_CREATE;
import static hu.elte.sbzbxr.phoneconnect.ui.PickLocationActivity.FOLDERNAME_TO_CREATE;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import hu.elte.sbzbxr.phoneconnect.BuildConfig;
import hu.elte.sbzbxr.phoneconnect.controller.MainViewModel;
import hu.elte.sbzbxr.phoneconnect.model.actions.Action_FailMessage;
import hu.elte.sbzbxr.phoneconnect.model.actions.Action_FailedToConnect;
import hu.elte.sbzbxr.phoneconnect.model.actions.Action_RequestUiRefresh;
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
public class ConnectionManager {
    private static final String LOG_TAG = "ConnectionManager";
    private final AtomicBoolean isListening = new AtomicBoolean(false);
    private final AtomicBoolean isSending = new AtomicBoolean(false);
    private ConnectionLimiter limiter = ConnectionLimiter.noLimit();

    private Socket tcpSocket;
    private BufferedOutputStream tcpOutStream;

    private InputStream in;
    private final OutgoingBuffer outgoingBuffer=new OutgoingBuffer();
    private MainViewModel viewModel;
    private final FileOutputStreamProvider streamProvider = new FileOutputStreamProvider(this);
    private final Context context;

    public ConnectionManager(Context context) {
        this.context=context;
        if( BuildConfig.DEBUG ){
            try {
                Class.forName("dalvik.system.CloseGuard")
                        .getMethod("setEnabled", boolean.class)
                        .invoke(null, true);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
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
        startAsyncTask(new ConnectionCreator(tcpOutStream,in,ip,port,this));
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
            isListening.set(false);
            isSending.set(false);
            if(in!=null) in.close();
            try{
                if(tcpOutStream !=null) {tcpOutStream.close();}
            }catch (SocketException ignore){}
            if(tcpSocket !=null) {tcpSocket.close(); tcpSocket=null;}
            closeUdp();
            viewModel.postAction(new Action_NetworkStateDisconnected());
            clearOutgoingFileTransferQueue();
            outgoingBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
            viewModel.postAction(new Action_NetworkStateDisconnected());
        }
    }

    //Tests whether the connection is valid
    public void sendMessage(MessageFrame frame){
        try {
            outgoingBuffer.put(frame);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Invoked when a connection asynchronously established
     */
    void connectRequestFinished(boolean successful, Socket s, String ip, int port, InputStream i, BufferedOutputStream o){
        if(!successful){
            viewModel.postAction(new Action_FailedToConnect(ip+":"+port));
        }else{
            tcpSocket =s;
            in=i;
            tcpOutStream =o;
            viewModel.postAction(new Action_NetworkStateConnected(ip,port));
            if (tcpOutStream == null) {
                System.err.println("But its null!!");
            }
            System.out.println("Successful connection establishment");
            isListening.set(true);
            isSending.set(true);
            listen();
            startSendingThread();

            initUdp(ip);
        }
    }

    private DatagramSocket udpSocket;
    private InetAddress address;
    private static final int UDP_SERVER_PORT = 4445;
    private void initUdp(String ip) {
        try{
            udpSocket=new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            closeUdp();
        }
        address=new InetSocketAddress(ip,UDP_SERVER_PORT).getAddress();
    }

    private void closeUdp(){
        if(udpSocket!=null){ udpSocket.close();udpSocket=null;}
    }


    private void sendWithUdp(NetworkFrame frame) {
        try {
            UdpSender.send(limiter,udpSocket,address,UDP_SERVER_PORT,frame);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Failed to send segment");
        }
    }

    /**
     * Starts a new Thread, which listens the input side of the socket
     */
    private void listen(){
        new Thread(() -> {
            while (isListening.get()) {
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
        messageFrame.answerArrived();
        viewModel.postAction(new Action_PingArrived("Round-trip time: "+messageFrame.calculateLatency()+"ms"));
        System.out.println("Successful ping! \nReceived: "+ messageFrame.toString());
    }

    private void startSendingThread(){
        new Thread(() ->{
            while(isSending.get()){
                NetworkFrame sendable = outgoingBuffer.take();
                if(sendable!=null){
                    if(sendable.type==FrameType.SEGMENT){
                        sendWithUdp(sendable);
                    }else{
                        FrameSender.send(limiter, tcpOutStream,sendable);
                    }
                }else{
                    Log.d(LOG_TAG,"Got null from buffer");
                }
            }
            limiter.stop();
        }).start();

        startFileCutterThread();
    }

    public void sendNotification(NotificationFrame n) {
        if(n==null){Log.d(LOG_TAG,"Cannot send >>null<< notification");return;}
        if(viewModel.notificationFilter.toForward(n.appName)){
            try{
                outgoingBuffer.put(n);
                Log.d(LOG_TAG,"Notification queued");
            }catch(IllegalStateException | InterruptedException e){
                Log.e(LOG_TAG,"The notification queue is full. Cannot add latest notification.");
            }
        }else{
            Log.e(LOG_TAG,"This notification is filtered out. AppName: "+n.appName);
        }
    }


    private final ExecutorService fileCutterExecutorService = Executors.newSingleThreadExecutor();
    private Future<?> currentFileCutterTask=null;
    private final LinkedBlockingQueue<FileToSend> fileTransferSendingQueue = new LinkedBlockingQueue<>();
    public void sendFiles(List<MyFileDescriptor> files, FrameType fileType, String backupId, long folderSize){
        if(currentFileCutterTask==null || currentFileCutterTask.isCancelled()) startFileCutterThread();
        files.forEach(desc->{
            if(fileTransferSendingQueue.offer(new FileToSend(desc,fileType,backupId,folderSize))){
                viewModel.postAction(new Action_FilePieceSent(new FileFrame(
                        fileType,desc.filename,desc.size,
                        backupId,folderSize,new byte[0]
                )));
            }else{
                viewModel.postAction(new Action_FailMessage("Cannot send file!"));
            }
        });
        viewModel.postAction(new Action_RequestUiRefresh());
    }

    private void startFileCutterThread(){
        if(! (currentFileCutterTask==null || currentFileCutterTask.isCancelled())) {currentFileCutterTask.cancel(true);}
        currentFileCutterTask = fileCutterExecutorService.submit(()->{
            try {
            while(isSending.get()){
                    FileToSend fileToSend = null;
                    fileToSend = fileTransferSendingQueue.take();

                    Log.d(LOG_TAG,"Would send the following file: "+ fileToSend.descriptor.filename);
                    FileCutter cutter = FileCutterCreator.create(fileToSend.descriptor,getContext().getContentResolver(),fileToSend.fileType,fileToSend.backupId,fileToSend.folderSize);

                    //sending
                    while (!cutter.isEnd() && isSending.get()){
                        try {
                            outgoingBuffer.put(cutter.current());
                        } catch (InterruptedException e) {
                            System.err.println("FilePiece insertion interrupted");
                            viewModel.postAction(new Action_LastPieceOfFileSent(cutter.current()));
                            cutter.close();
                            throw e;
                        }

                        //notify ui that a piece of file sent
                        viewModel.postAction(new Action_FilePieceSent(cutter.current()));

                        cutter.next();
                    }
                    //notify ui that file sending completed
                    viewModel.postAction(new Action_LastPieceOfFileSent(cutter.current()));
                    cutter.close();
                }
            } catch (InterruptedException e) {
                System.err.println("FileCutting interrupted");
                outgoingBuffer.clear();
                return;
            }
            if(!isSending.get()){outgoingBuffer.clear();}
        });
    }

    public void clearOutgoingFileTransferQueue(){
        if(currentFileCutterTask==null) return;
        currentFileCutterTask.cancel(true);
        FileToSend fileToSend = fileTransferSendingQueue.poll();
        while(fileToSend!=null){
            viewModel.postAction(new Action_LastPieceOfFileSent(new FileFrame(
                    fileToSend.fileType,fileToSend.descriptor.filename,fileToSend.descriptor.size,
                    fileToSend.backupId,fileToSend.folderSize,new byte[0])));
            fileToSend = fileTransferSendingQueue.poll();
        }
    }

    private final ExecutorService compressToJPGExecutorService = Executors.newFixedThreadPool(4);
    public void sendScreenShot(ScreenShot screenShot) {
        ScreenShotFrame screenShotFrame = new ScreenShotFrame(screenShot);
        try {
            outgoingBuffer.put(screenShotFrame);
            compressToJPGExecutorService.submit(screenShotFrame::transform);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public Socket getTcpSocket() {
        return tcpSocket;
    }

    public void setLimiter(ConnectionLimiter l) {
        limiter.stop();
        limiter = l;
        limiter.start();
    }

    public void setViewModel(MainViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public MainViewModel getViewModel() {
        return viewModel;
    }

    public Context getContext() {
        return context;
    }
}
