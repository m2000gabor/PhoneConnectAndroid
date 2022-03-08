package hu.elte.sbzbxr.phoneconnect.model.connection;

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

public class MyFrameSender {
    private static final String LOG_TAG = "MyFrameSender";

    private MyFrameSender() {}

    public static void send(PrintStream out, Sendable sendable) {
        try {
            MyNetworkProtocolFrame outFrame = sendable.toFrame();
            out.write(outFrame.getAsBytes());
            out.flush();
            Log.i(LOG_TAG, outFrame.getName()+" ( "+sendable.getTypeName()+", "+ outFrame.getDataLength()+" bytes) successfully sent.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(LOG_TAG,"FileNotFound");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
