package hu.elte.sbzbxr.phoneconnect.model.connection;

import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.SocketException;

import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;

public class FileSender extends RunnableWithHandler{
    private static final String FILE_SENDER_LOG_TAG = "FILE_SENDER";
    private final PrintStream out;
    private final String path;

    public FileSender(PrintStream out, String path) {
        this.out = out;
        this.path = path;
    }

    @Override
    void runMain() {
        //Based on: https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets
        File fileToBeSent = new File(path);
        byte[] buffer  = new byte[(int)fileToBeSent.length()];
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToBeSent));
            int readBytes = bis.read(buffer);
            System.out.println("Sending " + path + "(" + buffer.length + " bytes)");
            System.out.println("Sending " + path + "(" + readBytes + " bytes)");

            MyNetworkProtocolFrame outFrame = new MyNetworkProtocolFrame(
                    MyNetworkProtocolFrame.FrameType.PROTOCOL_SEGMENT,
                    getFileNameFromPath(path),buffer);
            out.write(outFrame.getAsBytes());
            out.flush();
            Log.i(FILE_SENDER_LOG_TAG,"File successfully sent.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(FILE_SENDER_LOG_TAG,"FileNotFound");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    void onFinished() {
        Log.i(FILE_SENDER_LOG_TAG, "File sending ended");
        sendMessageToActivity(getFileNameFromPath(path));
    }

    private void sendMessageToActivity(String str) {
        /*
        Intent intent = new Intent(this.getClass(), MainActivity.class);
        intent.putExtra("filename", str);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
         */
    }

    private String getFileNameFromPath(String path){
        String[] splittedPath = path.split("/");
        return splittedPath[splittedPath.length-1];
    }
}
