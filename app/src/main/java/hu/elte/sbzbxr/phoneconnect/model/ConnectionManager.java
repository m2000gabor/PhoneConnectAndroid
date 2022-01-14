package hu.elte.sbzbxr.phoneconnect.model;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.elte.sbzbxr.phoneconnect.MainActivity;

public class ConnectionManager {

    private InetSocketAddress serverAddress;
    private Socket socket;
    private PrintStream out;
    private BufferedReader in;
    private final MainActivity view;

    public ConnectionManager(MainActivity mainActivity) {
        this.view = mainActivity;
    }

    //Establish an http tcp connection
    //From: https://gist.github.com/teocci/0187ac32dcdbd57d8aaa89342be90f89
    public void connect(String ip, int port) {
        if (ip.equals("") || port == -1) {
            System.err.println("Invalid parameters");
            return;
        }

        //serverAddress = new InetSocketAddress(ip, port);
        //System.out.println("Let's connect to this: " + serverAddress.toString());


        // Connect to the server

        startAsyncTask(new ConnectionCreator2(socket,out,in,ip,port,this));
        //new ConnectionCreator(socket,out,in,ip,port,this).execute();
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
            if(in==null || out==null || socket==null){return;}
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Tests whether the connection is valid
    public void ping(){
        startAsyncTask(new Pinger(out,in,this));
    }

    //start a new Activity or what??
    public boolean startStreaming(){
        //ScreenCapture screenCapture= new ScreenCapture();
        return false;
    }

    void connectRequestFinished(boolean successful, String ip, int port, BufferedReader i, PrintStream o){
        in=i;
        out=o;
        if(successful){
            view.showConnectedUI(ip,port);
            if (out == null) {
                System.err.println("But its null!!");
            }
        }else{
            view.showFailMessage("Could not establish the connection!");
        }
    }

    void pingRequestFinished(boolean successful, String read){
        if(successful){
            view.successfulPing(read);
        }else{
            disconnect();
            view.showFailMessage("Ping failed! Disconnected!");
            view.afterDisconnect();
        }
    }

}
