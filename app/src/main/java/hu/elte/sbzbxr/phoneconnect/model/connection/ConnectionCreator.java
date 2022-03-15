package hu.elte.sbzbxr.phoneconnect.model.connection;

import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

//inspired: http://www.androidcoding.in/2020/05/12/work-manager_onetimeworkrequest/
public class ConnectionCreator extends RunnableWithHandler {
    private Socket socket;
    private PrintStream out;
    private InputStream in;
    private final String ip;
    private final int port;
    private final ConnectionManager connectionManager;
    private boolean result=false;


    public ConnectionCreator(PrintStream out, InputStream in, String ip, int port, ConnectionManager connectionManager) {
        this.out = out;
        this.in = in;
        this.ip = ip;
        this.port = port;
        this.connectionManager = connectionManager;
    }

    @Override
    public void runMain() {
        Log.d("oneTimeWorkRequest","Connecting to server");
        try{
            try {
                socket = new Socket(ip,port);
            } catch (UnknownHostException e) {
                System.err.println("Unknown host");
                e.printStackTrace();
            }

            // Create input and output streams to read from and write to the server
            out = new PrintStream(socket.getOutputStream());
            in = socket.getInputStream();

            // Follow the HTTP protocol of GET <path> HTTP/1.0 followed by an empty line
            //out.println( "GET " + path + " HTTP/1.0" );
            //out.println();
            result=true;
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    void onFinished() {
        connectionManager.connectRequestFinished(result,socket,ip,port,in,out);
    }
}